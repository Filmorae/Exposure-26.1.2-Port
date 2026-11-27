package io.github.mortuusars.exposure.client.capture.task;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.mortuusars.exposure.client.capture.CaptureShader;
import io.github.mortuusars.exposure.client.capture.ScreenshotReadback;
import io.github.mortuusars.exposure.client.image.Image;
import io.github.mortuusars.exposure.client.image.WrappedNativeImage;
import io.github.mortuusars.exposure.util.cycles.task.Result;
import io.github.mortuusars.exposure.util.cycles.task.Task;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

/** A request for one fully rendered world frame, owned by the render thread. */
public final class WorldFrameCaptureTask extends Task<Result<Image>> {
    private static @Nullable WorldFrameCaptureTask pending;
    private static @Nullable RenderTarget previewForFrame;

    private final CompletableFuture<Result<Image>> future = new CompletableFuture<>();
    private int framesToWait;
    private boolean preservePlayerView;
    private Runnable onSubmitted = () -> {};
    private @Nullable RenderTarget playerView;

    public WorldFrameCaptureTask(int framesToWait) {
        this.framesToWait = framesToWait;
    }

    public void prepare(Runnable onSubmitted, boolean preservePlayerView) {
        this.onSubmitted = onSubmitted;
        this.preservePlayerView = preservePlayerView;
        if (preservePlayerView) {
            // A remote camera needs a world frame at its new viewpoint before its terrain is ready.
            framesToWait = Math.max(framesToWait, 1);
        }
    }

    @Override
    public CompletableFuture<Result<Image>> execute() {
        RenderSystem.assertOnRenderThread();
        if (isStarted()) {
            return future;
        }
        setStarted();
        discardCancelledRequest();
        if (pending != null) {
            future.completeExceptionally(new IllegalStateException("A world frame is already requested."));
            return future;
        }

        if (preservePlayerView) {
            playerView = copyColor(Minecraft.getInstance().getMainRenderTarget(), "Exposure player view");
        }
        pending = this;
        return future;
    }

    public static void onFrameStarted() {
        discardCancelledRequest();
        previewForFrame = pending != null ? pending.playerView : null;
    }

    /** The world renderer and its callers' post effects have finished, and GUI draws are still pending. */
    public static void onWorldRendered() {
        WorldFrameCaptureTask task = pending;
        if (task == null || task.future.isDone() || task.framesToWait-- > 0) {
            return;
        }

        pending = null;
        task.capture(Minecraft.getInstance().getMainRenderTarget());
    }

    /** A tripod frame can show a different camera without replacing the player's presented view. */
    public static RenderTarget presentationTarget(RenderTarget main) {
        return previewForFrame != null ? previewForFrame : main;
    }

    public static void onFrameFinished() {
        RenderTarget preview = previewForFrame;
        previewForFrame = null;
        if (preview != null && (pending == null || preview != pending.playerView)) {
            preview.destroyBuffers();
        }
        discardCancelledRequest();
    }

    private static void discardCancelledRequest() {
        WorldFrameCaptureTask task = pending;
        if (task != null && task.future.isDone()) {
            pending = null;
            if (task.playerView != null && task.playerView != previewForFrame) {
                task.playerView.destroyBuffers();
            }
        }
    }

    private void capture(RenderTarget main) {
        RenderTarget filterTarget = null;
        CompletableFuture<NativeImage> readback;
        try {
            if (CaptureShader.hasShader()) {
                filterTarget = copyColor(main, "Exposure photograph filter");
                CaptureShader.process(filterTarget);
            }
            readback = ScreenshotReadback.capture(filterTarget != null ? filterTarget : main);
        } catch (RuntimeException exception) {
            if (filterTarget != null) {
                filterTarget.destroyBuffers();
            }
            future.completeExceptionally(exception);
            return;
        }

        RenderTarget ownedTarget = filterTarget;
        try {
            if (!readback.isCompletedExceptionally()) {
                onSubmitted.run();
            }
        } catch (RuntimeException exception) {
            future.completeExceptionally(exception);
        } finally {
            readback.whenComplete((nativeImage, exception) -> {
                if (ownedTarget != null) {
                    ownedTarget.destroyBuffers();
                }
                if (exception != null) {
                    future.completeExceptionally(exception);
                    return;
                }
                WrappedNativeImage image = new WrappedNativeImage(nativeImage);
                if (!future.complete(Result.success(image))) {
                    image.close();
                }
            });
        }
    }

    private static RenderTarget copyColor(RenderTarget source, String label) {
        RenderTarget copy = new TextureTarget(label, source.width, source.height, false);
        try {
            RenderSystem.getDevice().createCommandEncoder().copyTextureToTexture(
                    source.getColorTexture(), copy.getColorTexture(),
                    0, 0, 0, 0, 0, source.width, source.height);
            return copy;
        } catch (RuntimeException exception) {
            copy.destroyBuffers();
            throw exception;
        }
    }
}

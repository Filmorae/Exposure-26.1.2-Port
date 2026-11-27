package io.github.mortuusars.exposure.mixin.client;

import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.client.capture.Capture;
import io.github.mortuusars.exposure.client.capture.task.WorldFrameCaptureTask;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.client.util.Shader;
import io.github.mortuusars.exposure.event.ClientEvents;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GameRenderer.class, priority = 500)
public abstract class GameRendererMixin {
    @Inject(method = "update", at = @At("HEAD"))
    private void exposure$beforeCameraUpdate(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
        var minecraft = Minecrft.get();
        if (renderLevel && minecraft.level != null && minecraft.isGameLoadFinished()
                && !minecraft.getWindow().isMinimized()) {
            ExposureClient.cycles().tick();
        }
        WorldFrameCaptureTask.onFrameStarted();
    }

    @Inject(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/GameRenderer;renderLevel(Lnet/minecraft/client/DeltaTracker;)V",
            shift = At.Shift.AFTER))
    private void exposure$afterWorldRender(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
        WorldFrameCaptureTask.onWorldRendered();
    }

    @Inject(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/fog/FogRenderer;endFrame()V"))
    private void exposure$renderViewfinder(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
        Shader.processForGameRenderer();
    }

    @Inject(method = "resetData", at = @At(value = "RETURN"))
    void onResetData(CallbackInfo ci) {
        ClientEvents.resetRenderData();
    }

    @Inject(method = "resize", at = @At("HEAD"))
    void exposure$cancelCaptureOnResize(int width, int height, CallbackInfo ci) {
        Capture.cancelAll();
    }
}

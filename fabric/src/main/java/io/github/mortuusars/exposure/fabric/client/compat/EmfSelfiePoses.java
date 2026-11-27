package io.github.mortuusars.exposure.fabric.client.compat;

import io.github.mortuusars.exposure.client.animation.CameraModelPoses;
import io.github.mortuusars.exposure.client.animation.CameraPoseRenderState;
import io.github.mortuusars.exposure.client.animation.ExposureHumanoidRenderState;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.HumanoidArm;
import traben.entity_model_features.EMFAnimationApi;

/** Reapplies the camera arm after EMF has animated the model used by both the body and the held-item layer. */
public final class EmfSelfiePoses extends EMFAnimationApi.EMFAnimationHook {
    private EmfSelfiePoses() {}

    public static void register() throws Exception {
        EMFAnimationApi.registerAnimationHook(new EmfSelfiePoses());
    }

    @Override
    public void onAnimationEnd(AnimationContext context, boolean wasCancelledByHook) {
        var state = context.activeState();
        if (wasCancelledByHook || context.error() != null || state == null || state.isFirstPersonHand()
                || !(state.vanillaState() instanceof AvatarRenderState avatar)) {
            return;
        }

        CameraPoseRenderState pose = ((ExposureHumanoidRenderState) avatar).exposure$getCameraPose();
        if (pose.pose() != CameraPoseRenderState.Pose.SELFIE || pose.cameraItemId() == null) {
            return;
        }

        var parts = context.animatingModelRoot().getAllVanillaPartsByNameEMF();
        ModelPart head = parts.get("head");
        ModelPart cameraArm = parts.get(pose.arm() == HumanoidArm.LEFT ? "left_arm" : "right_arm");
        if (head != null && cameraArm != null) {
            CameraModelPoses.get(pose.cameraItemId()).applySelfie(head, cameraArm, pose.arm(), pose.cameraEntity());
        }
    }
}

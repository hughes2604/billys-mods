package uk.billysmods.mixin;

import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import uk.billysmods.FlightClientState;

/** Adds a very small local-only view shake during boosted Viltrumite flight. */
@Mixin(Camera.class)
public abstract class CameraShakeMixin {
    @Shadow
    protected abstract void setRotation(float yaw, float pitch);

    @Inject(method = "setup", at = @At("TAIL"))
    private void billysMods$shakeWhileBoosting(
            BlockGetter level,
            Entity focusedEntity,
            boolean thirdPerson,
            boolean inverseView,
            float partialTick,
            CallbackInfo callbackInfo
    ) {
        if (!(focusedEntity instanceof Player player)
                || !FlightClientState.isBoosting(player.getId())) {
            return;
        }

        float phase = (player.tickCount + partialTick) * 2.4F;
        float yawShake = (float) Math.sin(phase) * 0.12F;
        float pitchShake = (float) Math.cos(phase * 1.31F) * 0.09F;
        Camera camera = (Camera) (Object) this;
        this.setRotation(camera.getYRot() + yawShake, camera.getXRot() + pitchShake);
    }
}

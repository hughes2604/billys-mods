package uk.billysmods.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import uk.billysmods.ViltrumiteGenome;

/** Suppresses ordinary walking exhaustion and footstep events while flight is active. */
@Mixin(Player.class)
public abstract class FlightMovementMixin {
    @Inject(method = "causeFoodExhaustion", at = @At("HEAD"), cancellable = true)
    private void billysMods$preventFlightExhaustion(float exhaustion, CallbackInfo callbackInfo) {
        if (ViltrumiteGenome.isFlightActive((Player) (Object) this)) {
            callbackInfo.cancel();
        }
    }

    @Inject(method = "playStepSound", at = @At("HEAD"), cancellable = true)
    private void billysMods$suppressFlightFootsteps(BlockPos position, BlockState state, CallbackInfo callbackInfo) {
        if (ViltrumiteGenome.isFlightActive((Player) (Object) this)) {
            callbackInfo.cancel();
        }
    }
}

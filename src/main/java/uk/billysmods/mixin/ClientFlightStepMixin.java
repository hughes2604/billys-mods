package uk.billysmods.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import uk.billysmods.FlightClientState;

/** Stops local footstep playback while a player is in custom flight. */
@Mixin(Player.class)
public abstract class ClientFlightStepMixin {
    @Inject(method = "playStepSound", at = @At("HEAD"), cancellable = true)
    private void billysMods$suppressLocalFlightFootsteps(BlockPos position, BlockState state, CallbackInfo callbackInfo) {
        if (FlightClientState.isFlying(((Player) (Object) this).getId())) {
            callbackInfo.cancel();
        }
    }
}

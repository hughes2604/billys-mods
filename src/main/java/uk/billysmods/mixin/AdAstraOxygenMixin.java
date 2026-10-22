package uk.billysmods.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import uk.billysmods.ViltrumiteGenome;

/**
 * Optional Ad Astra hook: its oxygen tick is skipped for players carrying the genome.
 * The pseudo mixin keeps Billy's Mods loadable when Ad Astra is not installed.
 */
@Pseudo
@Mixin(targets = "earth.terrarium.adastra.common.systems.OxygenApiImpl", remap = false)
public abstract class AdAstraOxygenMixin {
    @Inject(method = "entityTick", at = @At("HEAD"), cancellable = true, remap = false)
    private void billysMods$genomeNeedsNoOxygen(ServerLevel level, LivingEntity entity, CallbackInfo ci) {
        if (entity instanceof Player player && ViltrumiteGenome.hasGenome(player)) {
            ci.cancel();
        }
    }
}

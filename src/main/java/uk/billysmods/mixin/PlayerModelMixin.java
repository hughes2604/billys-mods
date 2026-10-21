package uk.billysmods.mixin;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerModel.class)
public abstract class PlayerModelMixin {
    @Shadow @Final public ModelPart body;
    @Shadow @Final public ModelPart rightArm;
    @Shadow @Final public ModelPart leftArm;
    @Shadow @Final public ModelPart rightLeg;
    @Shadow @Final public ModelPart leftLeg;

    @Inject(method = "setupAnim", at = @At("TAIL"))
    private void billysMods$applyViltrumiteFlightPose(AvatarRenderState state, CallbackInfo callbackInfo) {
        if (!state.isFallFlying || state.chestEquipment.is(Items.ELYTRA)) {
            return;
        }

        // Minecraft 1.21.10 renders players from AvatarRenderState rather than the live Player entity.
        float bank = Math.max(-0.48F, Math.min(0.48F, state.flyingYRot * 0.018F));
        this.body.zRot = bank;
        this.rightArm.zRot -= bank * 0.35F;
        this.leftArm.zRot -= bank * 0.35F;
        this.rightLeg.zRot = bank * 0.20F;
        this.leftLeg.zRot = bank * 0.20F;
    }
}

package uk.billysmods.mixin;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import uk.billysmods.FlightClientState;

@Mixin(PlayerModel.class)
public abstract class PlayerModelMixin {
    @Shadow @Final public ModelPart body;
    @Shadow @Final public ModelPart rightArm;
    @Shadow @Final public ModelPart leftArm;
    @Shadow @Final public ModelPart rightLeg;
    @Shadow @Final public ModelPart leftLeg;

    @Inject(method = "setupAnim", at = @At("TAIL"))
    private void billysMods$applyViltrumiteFlightPose(LivingEntity entity, float limbSwing, float limbSwingAmount,
                                                       float ageInTicks, float netHeadYaw, float headPitch,
                                                       CallbackInfo ci) {
        if (!FlightClientState.isFlying(entity.getId())) return;
        if (!entity.isFallFlying() || entity.getItemBySlot(EquipmentSlot.CHEST).is(Items.ELYTRA)) {
            rightLeg.xRot = 0.04F;
            leftLeg.xRot = 0.04F;
            rightLeg.yRot = 0.0F;
            leftLeg.yRot = 0.0F;
            rightLeg.zRot = 0.015F;
            leftLeg.zRot = -0.015F;
            return;
        }
        body.zRot = 0.0F;
        rightArm.zRot = -0.08F;
        leftArm.zRot = 0.08F;
        rightLeg.zRot = 0.0F;
        leftLeg.zRot = 0.0F;
    }
}

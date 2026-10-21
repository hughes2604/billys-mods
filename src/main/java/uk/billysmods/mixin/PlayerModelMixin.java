package uk.billysmods.mixin;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerModel.class)
public abstract class PlayerModelMixin<T extends LivingEntity> {
    @Shadow @Final public ModelPart body;
    @Shadow @Final public ModelPart rightArm;
    @Shadow @Final public ModelPart leftArm;
    @Shadow @Final public ModelPart rightLeg;
    @Shadow @Final public ModelPart leftLeg;

    @Inject(method = "setupAnim", at = @At("TAIL"))
    private void billysMods$applyViltrumiteFlightPose(
            T entity,
            float limbSwing,
            float limbSwingAmount,
            float ageInTicks,
            float netHeadYaw,
            float headPitch,
            CallbackInfo callbackInfo
    ) {
        if (!(entity instanceof Player player)
                || !player.isFallFlying()
                || player.getItemBySlot(EquipmentSlot.CHEST).is(Items.ELYTRA)) {
            return;
        }

        // Keep Minecraft's horizontal gliding silhouette and add a restrained bank on turns.
        float bodyYawDifference = player.yBodyRot - player.getYRot();
        while (bodyYawDifference > 180.0F) bodyYawDifference -= 360.0F;
        while (bodyYawDifference < -180.0F) bodyYawDifference += 360.0F;
        float bank = Math.max(-0.48F, Math.min(0.48F, bodyYawDifference * 0.018F));
        this.body.zRot = bank;
        this.rightArm.zRot -= bank * 0.35F;
        this.leftArm.zRot -= bank * 0.35F;
        this.rightLeg.zRot = bank * 0.20F;
        this.leftLeg.zRot = bank * 0.20F;
    }
}

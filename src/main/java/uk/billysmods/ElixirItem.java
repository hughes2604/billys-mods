package uk.billysmods;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.level.Level;

public final class ElixirItem extends PotionItem {
    public static final Holder<MobEffect> IMMORTALITY = Registry.registerForHolder(
            BuiltInRegistries.MOB_EFFECT,
            ResourceLocation.fromNamespaceAndPath(BillysMods.MOD_ID, "immortality"),
            new MobEffect(MobEffectCategory.BENEFICIAL, 0xD4AF37) { }
    );

    private final boolean grantsImmortality;

    public ElixirItem(Properties properties, boolean grantsImmortality) {
        super(properties);
        this.grantsImmortality = grantsImmortality;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity user) {
        ItemStack result = super.finishUsingItem(stack, level, user);
        if (!level.isClientSide() && user instanceof Player player) {
            if (grantsImmortality) {
                player.addEffect(new MobEffectInstance(IMMORTALITY, MobEffectInstance.INFINITE_DURATION, 0, false, false, true));
                player.displayClientMessage(Component.literal("YOU ARE NOW IMMORTAL").withStyle(ChatFormatting.GOLD), false);
            } else {
                player.removeEffect(IMMORTALITY);
                player.displayClientMessage(Component.literal("YOU ARE MORTAL").withStyle(ChatFormatting.RED), false);
            }
            level.playSound(null, player.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 0.8F, grantsImmortality ? 1.0F : 0.75F);
        }
        return result;
    }
}

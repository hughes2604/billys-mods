package uk.billysmods;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import org.joml.Vector3f;

public final class ElixirItem extends PotionItem {
    public static final Holder<MobEffect> IMMORTALITY = Registry.registerForHolder(
            BuiltInRegistries.MOB_EFFECT,
            ResourceLocation.fromNamespaceAndPath(BillysMods.MOD_ID, "immortality"),
            new MobEffect(MobEffectCategory.BENEFICIAL, 0xD4AF37) { }
    );

    private static final int LIFE_PARTICLE_COLOR = 0xFFD966;
    private static final int MORTALITY_PARTICLE_COLOR = 0x8B0000;
    private static final int SHIELD_PARTICLE_COUNT = 24;
    private final boolean grantsImmortality;

    public ElixirItem(Properties properties, boolean grantsImmortality) {
        super(properties);
        this.grantsImmortality = grantsImmortality;
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.literal(grantsImmortality ? "Elixir of Immortality" : "Elixir of Mortality");
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.literal(grantsImmortality ? "Immortality" : "Mortality")
                .withStyle(grantsImmortality ? ChatFormatting.GOLD : ChatFormatting.RED));
    }

    @Override
    public net.minecraft.world.InteractionResultHolder<ItemStack> use(
            Level level, Player player, net.minecraft.world.InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.hasEffect(IMMORTALITY) == grantsImmortality) {
            if (!level.isClientSide()) {
                player.displayClientMessage(Component.literal(grantsImmortality
                                ? "YOU ARE ALREADY IMMORTAL" : "YOU ARE ALREADY MORTAL")
                        .withStyle(grantsImmortality ? ChatFormatting.GOLD : ChatFormatting.RED), true);
            }
            return net.minecraft.world.InteractionResultHolder.fail(stack);
        }
        return super.use(level, player, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity user) {
        ItemStack result = super.finishUsingItem(stack, level, user);
        if (!level.isClientSide() && user instanceof Player player) {
            if (grantsImmortality) {
                player.addEffect(new MobEffectInstance(IMMORTALITY, MobEffectInstance.INFINITE_DURATION, 0, false, false, true));
                level.playSound(null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.8F, 1.0F);
                player.displayClientMessage(Component.literal("YOU ARE NOW IMMORTAL").withStyle(ChatFormatting.GOLD), false);
                emitShieldParticles((ServerLevel) level, player, LIFE_PARTICLE_COLOR, 0.018D);
            } else {
                player.removeEffect(IMMORTALITY);
                level.playSound(null, player.blockPosition(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 0.8F, 1.0F);
                player.displayClientMessage(Component.literal("YOU ARE MORTAL").withStyle(ChatFormatting.RED), false);
                emitShieldParticles((ServerLevel) level, player, MORTALITY_PARTICLE_COLOR, -0.018D);
            }
        }
        return result;
    }

    private static void emitShieldParticles(ServerLevel level, Player player, int color, double drift) {
        DustParticleOptions particles = new DustParticleOptions(new Vector3f(
                ((color >> 16) & 255) / 255.0F, ((color >> 8) & 255) / 255.0F, (color & 255) / 255.0F), 0.7F);
        double height = player.getBbHeight();
        for (int ring = 0; ring < 3; ring++) {
            double y = player.getY() + height * (0.18D + ring * 0.30D);
            double radius = 0.34D + (ring == 1 ? 0.03D : 0.0D);
            for (int point = 0; point < SHIELD_PARTICLE_COUNT / 3; point++) {
                double angle = Math.PI * 2.0D * point / (SHIELD_PARTICLE_COUNT / 3) + ring * 0.18D;
                level.sendParticles(particles, player.getX() + Math.cos(angle) * radius, y,
                        player.getZ() + Math.sin(angle) * radius, 0, 0.0D, drift, 0.0D, 0.0D);
            }
        }
    }
}

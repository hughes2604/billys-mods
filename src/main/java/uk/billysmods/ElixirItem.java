package uk.billysmods;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

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
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        // Keep right-click drinking reliable, including for custom PotionItem instances.
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity user) {
        ItemStack result = super.finishUsingItem(stack, level, user);
        if (!level.isClientSide() && user instanceof Player player) {
            if (grantsImmortality) {
                player.addEffect(new MobEffectInstance(IMMORTALITY, MobEffectInstance.INFINITE_DURATION, 0, false, false, true));
                player.displayClientMessage(Component.literal("YOU ARE NOW IMMORTAL").withStyle(ChatFormatting.GOLD), false);
                emitShieldParticles((ServerLevel) level, player, LIFE_PARTICLE_COLOR, 0.018D);
            } else {
                player.removeEffect(IMMORTALITY);
                player.displayClientMessage(Component.literal("YOU ARE MORTAL").withStyle(ChatFormatting.RED), false);
                emitShieldParticles((ServerLevel) level, player, MORTALITY_PARTICLE_COLOR, -0.018D);
            }
        }
        return result;
    }

    private static void emitShieldParticles(ServerLevel level, Player player, int color, double verticalDrift) {
        DustParticleOptions particles = new DustParticleOptions(color, 0.7F);
        double centerX = player.getX();
        double centerZ = player.getZ();
        double height = player.getBbHeight();

        // Three lightly spaced rings form a short, close-fitting energy shell.
        for (int ring = 0; ring < 3; ring++) {
            double y = player.getY() + height * (0.18D + ring * 0.30D);
            double radius = 0.34D + (ring == 1 ? 0.03D : 0.0D);
            for (int point = 0; point < SHIELD_PARTICLE_COUNT / 3; point++) {
                double angle = (Math.PI * 2.0D * point / (SHIELD_PARTICLE_COUNT / 3)) + (ring * 0.18D);
                double x = centerX + Math.cos(angle) * radius;
                double z = centerZ + Math.sin(angle) * radius;
                // A zero count sends one particle with a small directional drift.
                level.sendParticles(particles, x, y, z, 0, 0.0D, verticalDrift, 0.0D, 0.0D);
            }
        }
    }
}

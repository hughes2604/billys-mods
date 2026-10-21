package uk.billysmods;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.SequencedSet;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BillysMods implements ModInitializer {
    public static final String MOD_ID = "billys-mods";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final ResourceKey<Item> ELIXIR_OF_LIFE_KEY = ResourceKey.create(Registries.ITEM, id("elixir_of_life"));
    private static final ResourceKey<Item> ELIXIR_OF_MORTALITY_KEY = ResourceKey.create(Registries.ITEM, id("elixir_of_mortality"));

    public static final Item ELIXIR_OF_LIFE = Registry.register(
            BuiltInRegistries.ITEM,
            id("elixir_of_life"),
            new ElixirItem(new Item.Properties()
                    .setId(ELIXIR_OF_LIFE_KEY)
                    .stacksTo(1)
                    .usingConvertsTo(Items.GLASS_BOTTLE)
                    .component(DataComponents.ITEM_NAME, Component.literal("Elixir of Immortality"))
                    .component(DataComponents.TOOLTIP_DISPLAY, elixirTooltipDisplay())
                    .component(DataComponents.CONSUMABLE, Consumables.DEFAULT_DRINK)
                    .component(DataComponents.POTION_CONTENTS, new PotionContents(Optional.empty(), Optional.of(0xFFD966), List.of(), Optional.empty())), true)
    );
    public static final Item ELIXIR_OF_MORTALITY = Registry.register(
            BuiltInRegistries.ITEM,
            id("elixir_of_mortality"),
            new ElixirItem(new Item.Properties()
                    .setId(ELIXIR_OF_MORTALITY_KEY)
                    .stacksTo(1)
                    .usingConvertsTo(Items.GLASS_BOTTLE)
                    .component(DataComponents.ITEM_NAME, Component.literal("Elixir of Mortality"))
                    .component(DataComponents.TOOLTIP_DISPLAY, elixirTooltipDisplay())
                    .component(DataComponents.CONSUMABLE, Consumables.DEFAULT_DRINK)
                    .component(DataComponents.POTION_CONTENTS, new PotionContents(Optional.empty(), Optional.of(0x8B0000), List.of(), Optional.empty())), false)
    );

    public static final ResourceKey<CreativeModeTab> ITEM_GROUP_KEY = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB, id("billys_mods")
    );
    public static final CreativeModeTab ITEM_GROUP = Registry.register(
            BuiltInRegistries.CREATIVE_MODE_TAB,
            ITEM_GROUP_KEY,
            FabricItemGroup.builder()
                    .icon(() -> ELIXIR_OF_LIFE.getDefaultInstance())
                    .title(Component.translatable("itemGroup.billys-mods.billys_mods"))
                    .build()
    );

    private static TooltipDisplay elixirTooltipDisplay() {
        SequencedSet<DataComponentType<?>> hiddenComponents = new LinkedHashSet<>();
        hiddenComponents.add(DataComponents.POTION_CONTENTS);
        return new TooltipDisplay(false, hiddenComponents);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        ItemGroupEvents.modifyEntriesEvent(ITEM_GROUP_KEY).register(entries -> {
            entries.accept(ELIXIR_OF_LIFE);
            entries.accept(ELIXIR_OF_MORTALITY);
        });
        ServerPlayerEvents.ALLOW_DEATH.register((player, damageSource, damageAmount) -> {
            if (player.hasEffect(ElixirItem.IMMORTALITY)) {
                advanceImmortalityDebuffs(player);
                if (damageSource.is(DamageTypes.OUT_OF_WORLD)) {
                    rescueFromVoid(player);
                }
                player.setHealth(1.0F);
                return false;
            }
            return true;
        });
        LOGGER.info("Billy's Mods loaded");
    }

    private static void rescueFromVoid(net.minecraft.server.level.ServerPlayer player) {
        net.minecraft.server.level.ServerLevel level = player.serverLevel();
        BlockPos safePosition = findSafeSurface(level, net.minecraft.core.BlockPos.containing(player.getX(), player.getY(), player.getZ()).getX(),
                net.minecraft.core.BlockPos.containing(player.getX(), player.getY(), player.getZ()).getZ());

        // The End's central island (and dimension spawn areas) provide a fallback if the
        // player fell through a location with no terrain beneath it.
        if (safePosition == null) {
            safePosition = findSafeSurface(level, 0, 0);
        }
        if (safePosition == null) {
            safePosition = findNearbySafeSurface(level, 0, 0);
        }
        if (safePosition != null) {
            player.teleportTo(level, safePosition.getX() + 0.5D, safePosition.getY(), safePosition.getZ() + 0.5D,
                    Set.of(), player.getYRot(), player.getXRot(), true);
            player.setDeltaMovement(Vec3.ZERO);
            player.hurtMarked = true;
        }
    }

    private static BlockPos findSafeSurface(net.minecraft.server.level.ServerLevel level, int originX, int originZ) {
        for (int radius = 0; radius <= 8; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) {
                        continue;
                    }
                    int x = originX + dx;
                    int z = originZ + dz;
                    if (!level.getWorldBorder().isWithinBounds(x, z)) {
                        continue;
                    }
                    int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                    if (isSafeLanding(level, x, y, z)) {
                        return new BlockPos(x, y, z);
                    }
                }
            }
        }
        return null;
    }

    private static boolean isSafeLanding(net.minecraft.server.level.ServerLevel level, int x, int y, int z) {
        if (y <= level.getMinY() || y + 1 >= level.getMaxY()) {
            return false;
        }
        BlockPos floor = new BlockPos(x, y - 1, z);
        BlockPos feet = new BlockPos(x, y, z);
        BlockPos head = new BlockPos(x, y + 1, z);
        BlockState floorState = level.getBlockState(floor);
        return floorState.blocksMotion()
                && level.getFluidState(floor).isEmpty()
                && level.getBlockState(feet).isAir()
                && level.getBlockState(head).isAir();
    }

    private static BlockPos findNearbySafeSurface(net.minecraft.server.level.ServerLevel level, int originX, int originZ) {
        // Bounded last resort for unusual custom dimensions with no safe column at the origin.
        for (int radius = 0; radius <= 32; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) {
                        continue;
                    }
                    int x = originX + dx;
                    int z = originZ + dz;
                    if (!level.getWorldBorder().isWithinBounds(x, z)) {
                        continue;
                    }
                    int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                    if (isSafeLanding(level, x, y, z)) {
                        return new BlockPos(x, y, z);
                    }
                }
            }
        }
        return null;
    }

    private static void advanceImmortalityDebuffs(net.minecraft.server.level.ServerPlayer player) {
        // Advance each debuff by one level at most; replacing the instances resets both timers.
        player.addEffect(nextDebuff(player, MobEffects.SLOWNESS));
        player.addEffect(nextDebuff(player, MobEffects.MINING_FATIGUE));
    }

    private static MobEffectInstance nextDebuff(
            net.minecraft.world.entity.LivingEntity player,
            Holder<MobEffect> effect
    ) {
        MobEffectInstance current = player.getEffect(effect);
        int nextAmplifier = current == null ? 0 : Math.min(2, current.getAmplifier() + 1);
        return new MobEffectInstance(effect, 3 * 60 * 20, nextAmplifier, false, true, true);
    }
}

package uk.billysmods;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
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
            new ElixirItem(new Item.Properties().setId(ELIXIR_OF_LIFE_KEY).stacksTo(1).craftRemainder(Items.GLASS_BOTTLE), true)
    );
    public static final Item ELIXIR_OF_MORTALITY = Registry.register(
            BuiltInRegistries.ITEM,
            id("elixir_of_mortality"),
            new ElixirItem(new Item.Properties().setId(ELIXIR_OF_MORTALITY_KEY).stacksTo(1).craftRemainder(Items.GLASS_BOTTLE), false)
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
                player.setHealth(1.0F);
                return false;
            }
            return true;
        });
        LOGGER.info("Billy's Mods loaded");
    }
}

package uk.billysmods;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

public final class ViltrumiteGenomeItem extends Item {
    public ViltrumiteGenomeItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            Item.TooltipContext context,
            TooltipDisplay display,
            Consumer<Component> textConsumer,
            TooltipFlag type
    ) {
        textConsumer.accept(Component.translatable("item.billys-mods.viltrumite_genome.description")
                .withStyle(ChatFormatting.GRAY));
        textConsumer.accept(Component.translatable("item.billys-mods.viltrumite_genome.flight_help")
                .withStyle(ChatFormatting.AQUA));
    }
}

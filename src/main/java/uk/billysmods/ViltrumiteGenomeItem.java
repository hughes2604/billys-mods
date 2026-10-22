package uk.billysmods;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public final class ViltrumiteGenomeItem extends Item {
    public ViltrumiteGenomeItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.translatable("item.billys-mods.viltrumite_genome.description").withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable("item.billys-mods.viltrumite_genome.flight_help").withStyle(ChatFormatting.AQUA));
    }
}

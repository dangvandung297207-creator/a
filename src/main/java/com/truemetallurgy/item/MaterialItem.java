package com.truemetallurgy.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/** Simple metallurgy item (bloom, crushed ore, guard, ...) with a tooltip kind. */
public class MaterialItem extends Item {
    private final String kind;

    public MaterialItem(String kind) {
        super(new Properties());
        this.kind = kind;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, lines, flag);
        lines.add(Component.translatable("tooltip.true_metallurgy." + kind).withStyle(s -> s.withColor(0x9A8A6A).withItalic(true)));
    }
}

package com.truemetallurgy.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/** Legendary design. Consumed at assembly to unlock a one-of-a-kind piece. */
public class BlueprintItem extends Item {
    private final String designId;

    public BlueprintItem(String designId) {
        super(new Properties().stacksTo(1));
        this.designId = designId;
    }

    public String getDesignId() {
        return designId;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, lines, flag);
        lines.add(Component.translatable("tooltip.true_metallurgy.blueprint", designId)
            .withStyle(ChatFormatting.GOLD));
        lines.add(Component.translatable("tooltip.true_metallurgy.blueprint_hint")
            .withStyle(s -> s.withColor(0x9A8A6A).withItalic(true)));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}

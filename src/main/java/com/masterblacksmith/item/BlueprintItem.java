package com.masterblacksmith.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** A legendary blueprint. Required to push a piece into LEGENDARY. */
public class BlueprintItem extends Item {
    private final String blueprintId;

    public BlueprintItem(String blueprintId) {
        super(new Properties().stacksTo(1));
        this.blueprintId = blueprintId;
    }

    public String getBlueprintId() { return blueprintId; }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, level, lines, flag);
        lines.add(Component.translatable("blueprint.masterblacksmith." + blueprintId)
                .withStyle(ChatFormatting.DARK_PURPLE));
        lines.add(Component.translatable("tooltip.masterblacksmith.blueprint_hint")
                .withStyle(ChatFormatting.GRAY));
    }

    @Override
    public boolean isFoil(ItemStack stack) { return true; }
}

package com.masterblacksmith.item;

import com.masterblacksmith.material.QuenchLiquids;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** A bucket (or vial) of quenching medium for the barrel. */
public class QuenchBucketItem extends Item {
    private final String liquidId;

    public QuenchBucketItem(String liquidId) {
        super(new Properties().stacksTo(1));
        this.liquidId = liquidId;
    }

    public String getLiquidId() { return liquidId; }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, level, lines, flag);
        var q = QuenchLiquids.get(liquidId);
        lines.add(Component.translatable("tooltip.masterblacksmith.quench_profile").withStyle(ChatFormatting.DARK_GRAY));
        lines.add(Component.literal(String.format("H x%.2f  T x%.2f  S x%.2f  D x%.2f",
                q.getHardnessMod(), q.getToughnessMod(), q.getSharpnessMod(), q.getDurabilityMod()))
                .withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable("tooltip.masterblacksmith.cooling",
                String.format("%.0f/s", q.getCoolRate())).withStyle(ChatFormatting.GRAY));
    }
}

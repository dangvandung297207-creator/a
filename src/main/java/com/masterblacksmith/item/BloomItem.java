package com.masterblacksmith.item;

import com.masterblacksmith.forging.ForgingData;
import com.masterblacksmith.forging.HeatZone;
import com.masterblacksmith.forging.HeatingHelper;
import com.masterblacksmith.material.MetalMaterials;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Spongy consolidated bloom: heat it, hammer it, make it a billet. */
public class BloomItem extends Item {
    private final String metalId;

    public BloomItem(String metalId) {
        super(new Properties());
        this.metalId = metalId;
    }

    public String getMetalId() { return metalId; }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return ForgingData.getTemp(stack) > 300;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.min(13, Math.round(ForgingData.getTemp(stack) / 1600F * 13F));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        int glow = HeatingHelper.glowColorForTemp(ForgingData.getTemp(stack));
        return glow == 0 ? 0xFF5500 : glow;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, level, lines, flag);
        float temp = ForgingData.getTemp(stack);
        lines.add(MetalMaterials.get(metalId).getDisplayName().copy().withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable("tooltip.masterblacksmith.temperature",
                String.format("%.0f", temp),
                HeatZone.zoneFor(MetalMaterials.get(metalId), temp).getDisplayName())
                .withStyle(ChatFormatting.GOLD));
        lines.add(Component.translatable("tooltip.masterblacksmith.bloom_hint").withStyle(ChatFormatting.DARK_GRAY));
    }
}

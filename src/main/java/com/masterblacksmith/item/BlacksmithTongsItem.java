package com.masterblacksmith.item;

import com.masterblacksmith.forging.ForgingData;
import com.masterblacksmith.forging.HeatZone;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Tongs carry one hot workpiece in their NBT. Hot steel must travel by tongs:
 * forge -&gt; anvil -&gt; quench -&gt; (reheat) -&gt; anvil.
 */
public class BlacksmithTongsItem extends Item {
    public BlacksmithTongsItem() {
        super(new Properties().durability(300).stacksTo(1));
    }

    public static ItemStack getCarried(ItemStack tongs) {
        CompoundTag root = tongs.getTag();
        if (root == null || !root.contains("Carried")) return ItemStack.EMPTY;
        return ItemStack.of(root.getCompound("Carried"));
    }

    public static void setCarried(ItemStack tongs, ItemStack carried) {
        CompoundTag root = tongs.getOrCreateTag();
        if (carried.isEmpty()) root.remove("Carried");
        else root.put("Carried", carried.save(new CompoundTag()));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, level, lines, flag);
        ItemStack carried = getCarried(stack);
        if (carried.isEmpty()) {
            lines.add(Component.translatable("tooltip.masterblacksmith.tongs_empty").withStyle(ChatFormatting.GRAY));
        } else {
            float temp = ForgingData.getTemp(carried);
            lines.add(Component.translatable("tooltip.masterblacksmith.carrying",
                    carried.getHoverName()).withStyle(ChatFormatting.GOLD));
            lines.add(Component.translatable("tooltip.masterblacksmith.temperature",
                    String.format("%.0f", temp),
                    HeatZone.zoneFor(ForgingData.getMetal(carried), temp).getDisplayName())
                    .withStyle(ChatFormatting.GRAY));
        }
    }
}

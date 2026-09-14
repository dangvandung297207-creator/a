package com.masterblacksmith.item;

import com.masterblacksmith.material.HandleMaterials;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Haft material: grip, balance, recovery and weight. */
public class HandleItem extends Item {
    private final String handleId;

    public HandleItem(String handleId) {
        super(new Properties());
        this.handleId = handleId;
    }

    public String getHandleId() { return handleId; }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, level, lines, flag);
        var h = HandleMaterials.get(handleId);
        lines.add(Component.translatable("tooltip.masterblacksmith.grip",
                String.format("%.0f%%", h.getGrip() * 100)).withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable("tooltip.masterblacksmith.handle_weight",
                String.format("%.2fkg", h.getWeightKg())).withStyle(ChatFormatting.GRAY));
        if (h.getSpeedBonus() != 0) {
            lines.add(Component.translatable("tooltip.masterblacksmith.recovery",
                    String.format("%+.2f", h.getSpeedBonus())).withStyle(ChatFormatting.GRAY));
        }
    }
}

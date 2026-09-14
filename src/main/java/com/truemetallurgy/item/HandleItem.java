package com.truemetallurgy.item;

import com.truemetallurgy.metallurgy.HandleMaterial;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/** Grip/handle. Material changes weight, durability and recovery. */
public class HandleItem extends Item {
    private final HandleMaterial material;

    public HandleItem(HandleMaterial material) {
        super(new Properties().stacksTo(16));
        this.material = material;
    }

    public HandleMaterial getMaterial() {
        return material;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, lines, flag);
        lines.add(Component.translatable("tooltip.true_metallurgy.handle_stats",
            String.format("%.2f", material.weightMult),
            String.format("%.2f", material.durabilityMult),
            String.format("%.2f", material.recoveryMult)).withStyle(ChatFormatting.GRAY));
    }
}

package com.truemetallurgy.item;

import com.truemetallurgy.components.BilletData;
import com.truemetallurgy.forging.HotMetal;
import com.truemetallurgy.metallurgy.Heat;
import com.truemetallurgy.metallurgy.Material;
import com.truemetallurgy.registry.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * A metal billet. Carries live temperature + purity in a data component,
 * cools in the inventory, glows via model overrides + glint while hot.
 */
public class BilletItem extends Item {
    private final Material material;

    public BilletItem(Material material) {
        super(new Properties().stacksTo(16));
        this.material = material;
    }

    public Material getMaterial() {
        return material;
    }

    public static BilletData dataOf(ItemStack stack) {
        BilletData data = stack.get(ModDataComponents.BILLET.get());
        if (data != null) return data;
        if (stack.getItem() instanceof BilletItem billet) {
            return new BilletData(billet.material.id(), Heat.ROOM_TEMP, billet.material.purity(), 0);
        }
        return BilletData.EMPTY;
    }

    public static ItemStack heated(ItemStack stack, int temp) {
        BilletData data = dataOf(stack);
        stack.set(ModDataComponents.BILLET.get(), data.withTemp(temp));
        return stack;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (!level.isClientSide && entity.tickCount % 20 == 0) {
            HotMetal.inventoryCool(stack);
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return HotMetal.temperatureOf(stack) >= Heat.FORGE_MIN;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, lines, flag);
        BilletData data = dataOf(stack);
        Heat.Zone zone = Heat.Zone.fromTemp(data.temperature());
        lines.add(Component.translatable("tooltip.true_metallurgy.temperature", data.temperature(),
            Component.translatable("heat.true_metallurgy." + zone.name().toLowerCase()))
            .withStyle(zone == Heat.Zone.COLD ? ChatFormatting.GRAY : ChatFormatting.GOLD));
        lines.add(Component.translatable("tooltip.true_metallurgy.purity", String.format("%.1f", data.purity()))
            .withStyle(ChatFormatting.DARK_GRAY));
        if (flag.isAdvanced() && data.reheats() > 0) {
            lines.add(Component.translatable("tooltip.true_metallurgy.reheats", data.reheats())
                .withStyle(ChatFormatting.DARK_RED));
        }
    }
}

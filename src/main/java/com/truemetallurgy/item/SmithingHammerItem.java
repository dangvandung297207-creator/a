package com.truemetallurgy.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/** A forging hammer. Tier sets tolerance, strength, rhythm and durability. */
public class SmithingHammerItem extends PickaxeItem {
    private final HammerTier tier;

    public SmithingHammerItem(HammerTier tier) {
        super(tier.vanilla, new Properties()
            .durability(tier.durability)
            .attributes(PickaxeItem.createAttributes(tier.vanilla, tier.attackDamage, tier.attackSpeed)));
        this.tier = tier;
    }

    public HammerTier getTier() {
        return tier;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, lines, flag);
        lines.add(Component.translatable("tooltip.true_metallurgy.hammer_stats",
            String.format("%.1f", tier.tolerance), String.format("%.1f", tier.strength),
            String.format("%.1f", tier.weightKg)).withStyle(s -> s.withColor(0x9A8A6A)));
    }
}

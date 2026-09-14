package com.masterblacksmith.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** A real tool with weight, efficiency and its own voice per tier. */
public class SmithingHammerItem extends Item {
    private final HammerTier tier;
    private final Multimap<Attribute, AttributeModifier> attributes;

    public SmithingHammerItem(HammerTier tier) {
        super(new Properties().durability(tier.getUses()).stacksTo(1));
        this.tier = tier;
        ImmutableMultimap.Builder<Attribute, AttributeModifier> b = ImmutableMultimap.builder();
        b.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Tool modifier",
                tier.attackDamage(), AttributeModifier.Operation.ADDITION));
        b.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Tool modifier",
                tier.attackSpeed(), AttributeModifier.Operation.ADDITION));
        this.attributes = b.build();
    }

    public HammerTier getTier() { return tier; }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        return slot == EquipmentSlot.MAINHAND ? attributes : super.getAttributeModifiers(slot, stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, level, lines, flag);
        lines.add(Component.translatable("tooltip.masterblacksmith.forging_efficiency",
                String.format("%.0f%%", tier.efficiency() * 100)).withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable("tooltip.masterblacksmith.precision",
                String.format("%.0f%%", tier.precision() * 100)).withStyle(ChatFormatting.GRAY));
    }

    @Override
    public boolean isEnchantable(ItemStack stack) { return true; }

    @Override
    public int getEnchantmentValue() { return 12; }
}

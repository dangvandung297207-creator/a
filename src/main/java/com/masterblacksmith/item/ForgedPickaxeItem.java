package com.masterblacksmith.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.masterblacksmith.forging.ForgedStats;
import com.masterblacksmith.forging.ItemIdentity;
import com.masterblacksmith.material.MetalMaterials;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** A finished pickaxe with identity-driven stats. */
public class ForgedPickaxeItem extends PickaxeItem {
    public ForgedPickaxeItem() {
        super(ForgedTier.INSTANCE, 1, -2.8F, new Properties());
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        if (slot == EquipmentSlot.MAINHAND && ItemIdentity.hasIdentity(stack)) {
            ImmutableMultimap.Builder<Attribute, AttributeModifier> b = ImmutableMultimap.builder();
            b.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier",
                    ForgedStats.attackDamage(stack, "pickaxe"), AttributeModifier.Operation.ADDITION));
            b.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier",
                    ForgedStats.attackSpeed(stack, "pickaxe", -2.8), AttributeModifier.Operation.ADDITION));
            return b.build();
        }
        return super.getAttributeModifiers(slot, stack);
    }

    public int getMaxDamage(ItemStack stack) {
        if (ItemIdentity.hasIdentity(stack)) return ForgedStats.maxDurability(stack);
        return ForgedTier.INSTANCE.getUses();
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        float base = super.getDestroySpeed(stack, state);
        if (base <= 1F || !ItemIdentity.hasIdentity(stack)) return base;
        var d = ItemIdentity.read(stack);
        return base * (0.6F + MetalMaterials.get(d.metal()).getHardness() * 0.7F + (d.score() / 100F) * 0.4F);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, level, lines, flag);
        if (ItemIdentity.hasIdentity(stack)) {
            lines.add(Component.translatable("tooltip.masterblacksmith.forged_damage",
                    String.format("%.1f", ForgedStats.attackDamage(stack, "pickaxe")))
                    .withStyle(ChatFormatting.DARK_GREEN));
            lines.addAll(ItemIdentity.lore(stack));
        }
    }
}

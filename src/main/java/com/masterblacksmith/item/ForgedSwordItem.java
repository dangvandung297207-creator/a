package com.masterblacksmith.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.masterblacksmith.forging.ForgedStats;
import com.masterblacksmith.forging.ItemIdentity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** A finished sword whose soul lives in its NBT identity. */
public class ForgedSwordItem extends SwordItem {
    private final String weaponType;
    private final double baseSpeed;

    public ForgedSwordItem() {
        this("sword", 3, -2.4F);
    }

    protected ForgedSwordItem(String weaponType, int damage, float speed) {
        super(ForgedTier.INSTANCE, damage, speed, new Properties());
        this.weaponType = weaponType;
        this.baseSpeed = speed;
    }

    public String getWeaponType() { return weaponType; }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        if (slot == EquipmentSlot.MAINHAND && ItemIdentity.hasIdentity(stack)) {
            ImmutableMultimap.Builder<Attribute, AttributeModifier> b = ImmutableMultimap.builder();
            b.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier",
                    ForgedStats.attackDamage(stack, weaponType), AttributeModifier.Operation.ADDITION));
            b.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier",
                    ForgedStats.attackSpeed(stack, weaponType, baseSpeed), AttributeModifier.Operation.ADDITION));
            return b.build();
        }
        return super.getAttributeModifiers(slot, stack);
    }

    /** Dynamic durability via the Forge hook when present; harmless otherwise. */
    public int getMaxDamage(ItemStack stack) {
        if (ItemIdentity.hasIdentity(stack)) return ForgedStats.maxDurability(stack);
        return ForgedTier.INSTANCE.getUses();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, level, lines, flag);
        if (ItemIdentity.hasIdentity(stack)) {
            lines.add(Component.translatable("tooltip.masterblacksmith.forged_damage",
                    String.format("%.1f", ForgedStats.attackDamage(stack, weaponType)))
                    .withStyle(ChatFormatting.DARK_GREEN));
            lines.addAll(ItemIdentity.lore(stack));
        }
    }
}

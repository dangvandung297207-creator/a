package com.masterblacksmith.item;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

/** Static shell behind forged plate; per-piece story lives in NBT identity. */
public enum ForgedArmorMaterial implements ArmorMaterial {
    FORGED;

    @Override
    public int getDurabilityForType(ArmorItem.Type type) {
        return switch (type) {
            case HELMET -> 520;
            case CHESTPLATE -> 720;
            case LEGGINGS -> 640;
            case BOOTS -> 480;
        };
    }

    @Override
    public int getDefenseForType(ArmorItem.Type type) {
        return switch (type) {
            case HELMET -> 3;
            case CHESTPLATE -> 8;
            case LEGGINGS -> 6;
            case BOOTS -> 3;
        };
    }

    @Override public int getEnchantmentValue() { return 15; }
    @Override public SoundEvent getEquipSound() { return SoundEvents.ARMOR_EQUIP_IRON; }
    @Override public Ingredient getRepairIngredient() { return Ingredient.EMPTY; }
    @Override public String getName() { return "masterblacksmith:forged"; }
    @Override public float getToughness() { return 2.5F; }
    @Override public float getKnockbackResistance() { return 0.1F; }
}

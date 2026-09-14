package com.masterblacksmith.item;

import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * Static tier behind forged tools. Real damage, speed and durability come
 * from the NBT identity (see ForgedStats); the tier only guarantees
 * top-level harvest behaviour.
 */
public class ForgedTier implements Tier {
    public static final ForgedTier INSTANCE = new ForgedTier();

    private ForgedTier() {}

    @Override public int getUses() { return 3000; }
    @Override public float getSpeed() { return 9.0F; }
    @Override public float getAttackDamageBonus() { return 0F; }
    @Override public int getLevel() { return 4; }
    @Override public int getEnchantmentValue() { return 18; }
    @Override public Ingredient getRepairIngredient() { return Ingredient.EMPTY; }
}

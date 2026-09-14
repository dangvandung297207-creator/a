package com.truemetallurgy.item;

import net.minecraft.world.item.Tiers;

/** Smithing hammer progression. Forging tools first, weapons a distant second. */
public enum HammerTier {
    PRIMITIVE(Tiers.STONE, 180, 0.9F, 1.0F, 3.0F, -2.9F, 12, 1.2F),
    COPPER(Tiers.IRON, 260, 1.0F, 1.1F, 4.0F, -2.9F, 11, 1.6F),
    IRON(Tiers.IRON, 420, 1.1F, 1.2F, 5.0F, -3.0F, 10, 2.0F),
    STEEL(Tiers.DIAMOND, 680, 1.25F, 1.35F, 6.0F, -3.0F, 9, 2.4F),
    HARDENED(Tiers.DIAMOND, 950, 1.4F, 1.5F, 7.0F, -3.1F, 8, 2.8F),
    MASTERWORK(Tiers.NETHERITE, 1500, 1.6F, 1.7F, 8.0F, -3.1F, 7, 3.2F);

    /** Vanilla tier used for mining level / enchantability baseline. */
    public final Tiers vanilla;
    public final int durability;
    /** Forging tolerance multiplier (widens hit accuracy windows). */
    public final float tolerance;
    /** Progress granted per effective strike. */
    public final float strength;
    public final float attackDamage;
    public final float attackSpeed;
    /** Cooldown between strikes in ticks (rhythm control). */
    public final int strikeCooldown;
    public final float weightKg;

    HammerTier(Tiers vanilla, int durability, float tolerance, float strength,
            float attackDamage, float attackSpeed, int strikeCooldown, float weightKg) {
        this.vanilla = vanilla;
        this.durability = durability;
        this.tolerance = tolerance;
        this.strength = strength;
        this.attackDamage = attackDamage;
        this.attackSpeed = attackSpeed;
        this.strikeCooldown = strikeCooldown;
        this.weightKg = weightKg;
    }

    public String langKey() {
        return "hammer.true_metallurgy." + name().toLowerCase();
    }
}

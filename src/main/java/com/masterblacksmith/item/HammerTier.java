package com.masterblacksmith.item;

import com.masterblacksmith.ModSounds;
import net.minecraft.sounds.SoundEvent;

/** Copper -&gt; Iron -&gt; Steel -&gt; Hardened -&gt; Masterwork hammer progression. */
public enum HammerTier {
    COPPER("copper", 180, 5.0F, -3.1, 0.85F, 0.90F),
    IRON("iron", 400, 6.0F, -3.05, 1.00F, 1.00F),
    STEEL("steel", 900, 7.0F, -3.0, 1.12F, 1.10F),
    HARDENED("hardened", 1800, 8.0F, -2.95, 1.22F, 1.20F),
    MASTERWORK("masterwork", 4000, 9.0F, -2.85, 1.35F, 1.35F);

    private final String key;
    private final int uses;
    private final float attackDamage;
    private final double attackSpeed;
    private final float efficiency;
    private final float precision;

    HammerTier(String key, int uses, float attackDamage, double attackSpeed, float efficiency, float precision) {
        this.key = key;
        this.uses = uses;
        this.attackDamage = attackDamage;
        this.attackSpeed = attackSpeed;
        this.efficiency = efficiency;
        this.precision = precision;
    }

    public String getKey() { return key; }
    public int getUses() { return uses; }
    public float attackDamage() { return attackDamage; }
    public double attackSpeed() { return attackSpeed; }
    public float efficiency() { return efficiency; }
    public float precision() { return precision; }

    public SoundEvent hitSound() {
        return switch (this) {
            case COPPER, IRON -> ModSounds.HAMMER_IRON.get();
            case STEEL, HARDENED -> ModSounds.HAMMER_STEEL.get();
            case MASTERWORK -> ModSounds.HAMMER_MASTER.get();
        };
    }
}

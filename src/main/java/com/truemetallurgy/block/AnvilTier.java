package com.truemetallurgy.block;

/** Anvil progression. Higher tiers forgive more and shape harder metals. */
public enum AnvilTier {
    BASIC(1.0F, 0.40F, false),
    IRON(1.2F, 0.65F, false),
    STEEL(1.35F, 0.90F, false),
    MASTER(1.6F, 1.01F, true);

    /** Forging tolerance multiplier (hit accuracy window). */
    public final float tolerance;
    /** Maximum material difficulty this anvil can shape. */
    public final float maxDifficulty;
    /** Master anvil grants a small legendary-potential bonus. */
    public final boolean legendaryBonus;

    AnvilTier(float tolerance, float maxDifficulty, boolean legendaryBonus) {
        this.tolerance = tolerance;
        this.maxDifficulty = maxDifficulty;
        this.legendaryBonus = legendaryBonus;
    }

    public boolean canShape(float difficulty) {
        return difficulty <= maxDifficulty;
    }
}

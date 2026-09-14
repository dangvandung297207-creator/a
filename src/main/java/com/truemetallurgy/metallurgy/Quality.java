package com.truemetallurgy.metallurgy;

import com.truemetallurgy.util.TMUtil;

/** Craftsmanship score (0-100) and quality tiers. Skill-based, never random. */
public final class Quality {
    private Quality() {}

    public enum Tier {
        FLAWED(0, 39), STANDARD(40, 69), MASTERWORK(70, 89), LEGENDARY(90, 100);

        public final int min;
        public final int max;

        Tier(int min, int max) { this.min = min; this.max = max; }

        public static Tier fromScore(int score) {
            for (Tier t : values()) {
                if (score >= t.min && score <= t.max) return t;
            }
            return FLAWED;
        }

        public String langKey() {
            return "quality.true_metallurgy." + name().toLowerCase();
        }
    }

    /** Durability multiplier applied to the material base. */
    public static float durabilityMult(int score) {
        return switch (Tier.fromScore(score)) {
            case FLAWED -> 0.55F + score * 0.004F;
            case STANDARD -> 0.85F + (score - 40) * 0.005F;
            case MASTERWORK -> 1.25F;
            case LEGENDARY -> 1.55F;
        };
    }

    /** Primary performance (damage) multiplier. */
    public static float performanceMult(int score) {
        return switch (Tier.fromScore(score)) {
            case FLAWED -> 0.7F + score * 0.004F;
            case STANDARD -> 0.92F + (score - 40) * 0.003F;
            case MASTERWORK -> 1.15F;
            case LEGENDARY -> 1.32F;
        };
    }

    public static int clampScore(float score) {
        return TMUtil.clamp(Math.round(score), 0, 100);
    }
}

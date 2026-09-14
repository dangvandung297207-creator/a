package com.masterblacksmith.forging;

/** Edge-angle trade-off: keener edges cut deeper but chip faster. */
public final class GrindingProfile {
    public static final float MIN_ANGLE = 15F;
    public static final float MAX_ANGLE = 35F;

    private GrindingProfile() {}

    public static float sharpnessMult(float angle) {
        float t = (clamp(angle) - MIN_ANGLE) / (MAX_ANGLE - MIN_ANGLE);
        return 1.35F + (0.85F - 1.35F) * t;
    }

    public static float durabilityMult(float angle) {
        float t = (clamp(angle) - MIN_ANGLE) / (MAX_ANGLE - MIN_ANGLE);
        return 0.75F + (1.25F - 0.75F) * t;
    }

    public static float recommended(String weaponType) {
        return switch (weaponType) {
            case "sword" -> 20F;
            case "axe" -> 28F;
            case "pickaxe" -> 32F;
            case "spear" -> 18F;
            default -> 25F;
        };
    }

    public static float grindQuality(float angle, String weaponType, float wheelWear01) {
        float q = 100F - Math.abs(angle - recommended(weaponType)) * 2.0F - wheelWear01 * 30F;
        return Math.max(0F, Math.min(100F, q));
    }

    private static float clamp(float angle) {
        return Math.max(MIN_ANGLE, Math.min(MAX_ANGLE, angle));
    }
}

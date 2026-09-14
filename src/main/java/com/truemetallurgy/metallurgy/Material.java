package com.truemetallurgy.metallurgy;

/**
 * Immutable metallurgical definition. Gameplay reads these values directly:
 * nothing here is a recolor - hardness, toughness, sharpness, forging
 * difficulty and the forging window all change how a metal behaves.
 *
 * @param id               registry id, e.g. "steel"
 * @param hardness         raises damage / edge retention, raises difficulty
 * @param toughness        raises durability, resists cracking
 * @param sharpness        multiplies edge damage potential
 * @param flexibility      reduces crack chance on bad hits
 * @param durability       base durability pool at standard quality
 * @param heatResistance   slows overheating damage and oxidation
 * @param purity           default purity of fresh stock (0-100)
 * @param density          weight in kg per finished blade unit
 * @param forgingDifficulty 0 (trivial) to 1 (master-only)
 * @param meltingTemp      degrees C; never heat past this
 * @param forgeMin         forging window lower bound (C)
 * @param forgeMax         forging window upper bound (C)
 * @param sweetSpot        ideal forging temperature (C)
 * @param baseDamage       base attack damage for swords at standard quality
 * @param baseSpeed        base mining/dig speed
 */
public record Material(
    String id,
    float hardness,
    float toughness,
    float sharpness,
    float flexibility,
    int durability,
    float heatResistance,
    float purity,
    float density,
    float forgingDifficulty,
    int meltingTemp,
    int forgeMin,
    int forgeMax,
    int sweetSpot,
    float baseDamage,
    float baseSpeed
) {
    /** How close a temperature is to the sweet spot, 1.0 = perfect. */
    public float heatAccuracy(int tempCelsius) {
        if (tempCelsius < forgeMin || tempCelsius > forgeMax) return 0.0F;
        int half = Math.max(1, (forgeMax - forgeMin) / 2);
        return 1.0F - Math.min(1.0F, Math.abs(tempCelsius - sweetSpot) / (float) half);
    }
}

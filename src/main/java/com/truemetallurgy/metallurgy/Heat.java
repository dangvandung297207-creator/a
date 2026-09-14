package com.truemetallurgy.metallurgy;

/**
 * Temperature model. Degrees Celsius everywhere.
 *
 * <p>Zones: 0-300 cold, 300-700 warm, 700-1000 forging range,
 * 1000-1200 high heat, 1200+ overheated. Each material additionally
 * defines its own precise forging window inside those zones.
 */
public final class Heat {
    private Heat() {}

    public static final int ROOM_TEMP = 20;
    public static final int WARM_MIN = 300;
    public static final int FORGE_MIN = 700;
    public static final int HIGH_MIN = 1000;
    public static final int OVERHEAT_MIN = 1200;

    /** Forge block heat state, mirrored in the blockstate for light + visuals. */
    public enum ForgeState {
        COLD, WARM, HOT, FORGING, OVERHEATED, MOLTEN;

        public static ForgeState fromTemp(int temp) {
            if (temp < 200) return COLD;
            if (temp < 600) return WARM;
            if (temp < 850) return HOT;
            if (temp < 1150) return FORGING;
            if (temp < 1400) return OVERHEATED;
            return MOLTEN;
        }
    }

    /** Workpiece temperature zone. */
    public enum Zone {
        COLD, WARM, FORGING, HIGH_HEAT, OVERHEATED;

        public static Zone fromTemp(int temp) {
            if (temp < WARM_MIN) return COLD;
            if (temp < FORGE_MIN) return WARM;
            if (temp < HIGH_MIN) return FORGING;
            if (temp < OVERHEAT_MIN) return HIGH_HEAT;
            return OVERHEATED;
        }
    }

    /** Glow intensity 0-1 for rendering hot metal. */
    public static float glow(int temp) {
        if (temp < WARM_MIN) return 0.0F;
        if (temp >= OVERHEAT_MIN) return 1.0F;
        return (temp - WARM_MIN) / (float) (OVERHEAT_MIN - WARM_MIN);
    }

    /** Heat predicate 0-1 for item model overrides (5 visual stages). */
    public static float modelHeat(int temp) {
        if (temp < 250) return 0.0F;
        if (temp < 650) return 0.25F;
        if (temp < 950) return 0.5F;
        if (temp < 1150) return 0.75F;
        return 1.0F;
    }
}

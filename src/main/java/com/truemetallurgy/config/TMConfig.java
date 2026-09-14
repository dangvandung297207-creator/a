package com.truemetallurgy.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Server + client configuration. Holds the global particle budget and
 * difficulty/craftsmanship tuning so server owners can keep the workshop
 * performant without touching gameplay logic.
 */
public final class TMConfig {
    private TMConfig() {}

    private static final ModConfigSpec.Builder SERVER = new ModConfigSpec.Builder();
    private static final ModConfigSpec.Builder CLIENT = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue MAX_PARTICLES_PER_TICK = SERVER
        .comment("Global particle budget per tick for all True Metallurgy effects.")
        .defineInRange("performance.maxParticlesPerTick", 160, 0, 2000);
    public static final ModConfigSpec.IntValue FORGE_TICK_INTERVAL = SERVER
        .comment("Forge temperature simulation interval in ticks. Higher = cheaper, coarser simulation.")
        .defineInRange("performance.forgeTickInterval", 4, 1, 40);
    public static final ModConfigSpec.DoubleValue FORGING_TOLERANCE_MULT = SERVER
        .comment("Multiplier for hammer/anvil forging tolerance. Higher = more forgiving strikes.")
        .defineInRange("gameplay.forgingToleranceMult", 1.0, 0.25, 3.0);
    public static final ModConfigSpec.DoubleValue HEATING_RATE_MULT = SERVER
        .comment("Multiplier for forge heating speed.")
        .defineInRange("gameplay.heatingRateMult", 1.0, 0.25, 4.0);
    public static final ModConfigSpec.BooleanValue HOT_METAL_BURNS = SERVER
        .comment("Whether handling hot metal without tongs burns the player.")
        .define("gameplay.hotMetalBurns", true);

    public static final ModConfigSpec.BooleanValue ENABLE_CAMERA_SHAKE = CLIENT
        .comment("Subtle camera kick on hammer impacts.")
        .define("visual.cameraShake", true);
    public static final ModConfigSpec.BooleanValue ENABLE_HEAT_SHIMMER = CLIENT
        .comment("Extra heat glow particles on hot metal.")
        .define("visual.heatShimmer", true);

    public static final ModConfigSpec SERVER_SPEC = SERVER.build();
    public static final ModConfigSpec CLIENT_SPEC = CLIENT.build();

    public static int particleBudget() {
        try {
            return MAX_PARTICLES_PER_TICK.get();
        } catch (IllegalStateException notLoaded) {
            return 160;
        }
    }
}

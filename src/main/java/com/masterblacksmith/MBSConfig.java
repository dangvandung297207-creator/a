package com.masterblacksmith;

import net.minecraftforge.common.ForgeConfigSpec;

/** Server-authoritative tuning: performance caps and craftsmanship balance. */
public class MBSConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.IntValue MAX_PARTICLES_PER_EFFECT;
    public static final ForgeConfigSpec.IntValue TEMP_TICK_INTERVAL;
    public static final ForgeConfigSpec.BooleanValue ENABLE_DYNAMIC_LIGHT;
    public static final ForgeConfigSpec.BooleanValue ENABLE_HEAT_SHIMMER;
    public static final ForgeConfigSpec.DoubleValue REHEAT_QUALITY_PENALTY;
    public static final ForgeConfigSpec.DoubleValue OVERWORK_QUALITY_PENALTY;
    public static final ForgeConfigSpec.IntValue LEGENDARY_THRESHOLD;
    public static final ForgeConfigSpec.IntValue MASTERWORK_THRESHOLD;
    public static final ForgeConfigSpec.BooleanValue REQUIRE_BLUEPRINT_FOR_LEGENDARY;
    public static final ForgeConfigSpec.BooleanValue STARFALL_REQUIRES_MASTER_FORGE;
    public static final ForgeConfigSpec.DoubleValue BILLET_COOLING_RATE;
    public static final ForgeConfigSpec.DoubleValue OXIDATION_LOSS_CHANCE;

    static {
        BUILDER.push("performance");
        MAX_PARTICLES_PER_EFFECT = BUILDER.comment("Hard cap of particles spawned per single effect (strike, quench, ...).")
                .defineInRange("maxParticlesPerEffect", 24, 4, 128);
        TEMP_TICK_INTERVAL = BUILDER.comment("Forge/anvil temperature simulation step in ticks.")
                .defineInRange("tempTickInterval", 2, 1, 20);
        ENABLE_DYNAMIC_LIGHT = BUILDER.comment("Hot metal and forge states emit dynamic light.")
                .define("enableDynamicLight", true);
        ENABLE_HEAT_SHIMMER = BUILDER.comment("Spawn subtle heat-shimmer particles above hot metal.")
                .define("enableHeatShimmer", true);
        BUILDER.pop();

        BUILDER.push("balance");
        REHEAT_QUALITY_PENALTY = BUILDER.comment("Craftsmanship points lost per reheat cycle.")
                .defineInRange("reheatQualityPenalty", 2.5, 0.0, 10.0);
        OVERWORK_QUALITY_PENALTY = BUILDER.comment("Points lost per extra strike past a stage requirement.")
                .defineInRange("overworkQualityPenalty", 0.6, 0.0, 5.0);
        MASTERWORK_THRESHOLD = BUILDER.defineInRange("masterworkThreshold", 70, 50, 89);
        LEGENDARY_THRESHOLD = BUILDER.defineInRange("legendaryThreshold", 90, 70, 100);
        REQUIRE_BLUEPRINT_FOR_LEGENDARY = BUILDER.comment("Legendary tier needs a matching blueprint in the assembly table.")
                .define("requireBlueprintForLegendary", true);
        STARFALL_REQUIRES_MASTER_FORGE = BUILDER.comment("Starfall steel can only reach forging heat in a master forge.")
                .define("starfallRequiresMasterForge", true);
        BILLET_COOLING_RATE = BUILDER.comment("Degrees C lost per second by a billet on the anvil.")
                .defineInRange("billetCoolingRate", 14.0, 1.0, 60.0);
        OXIDATION_LOSS_CHANCE = BUILDER.comment("Chance per overheated strike to lose purity to scale/oxidation.")
                .defineInRange("oxidationLossChance", 0.35, 0.0, 1.0);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}

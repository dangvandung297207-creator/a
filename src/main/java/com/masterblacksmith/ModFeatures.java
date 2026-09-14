package com.masterblacksmith;

import com.masterblacksmith.worldgen.RuinedSmithyFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** World features (ruined smithies; ores are vanilla ore features via JSON). */
public class ModFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(ForgeRegistries.FEATURES, MasterBlacksmith.MOD_ID);

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> RUINED_SMITHY =
            FEATURES.register("ruined_smithy", () -> new RuinedSmithyFeature(NoneFeatureConfiguration.CODEC));
}

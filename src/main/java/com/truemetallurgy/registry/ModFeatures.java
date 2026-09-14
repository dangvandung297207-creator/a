package com.truemetallurgy.registry;

import com.truemetallurgy.TrueMetallurgy;
import com.truemetallurgy.worldgen.AbandonedForgeFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** World generation features (placed via data-driven configured/placed features). */
public final class ModFeatures {
    private ModFeatures() {}

    public static final DeferredRegister<Feature<?>> FEATURES =
        DeferredRegister.create(Registries.FEATURE, TrueMetallurgy.MOD_ID);

    public static final DeferredHolder<Feature<?>, AbandonedForgeFeature> ABANDONED_FORGE =
        FEATURES.register("abandoned_forge", AbandonedForgeFeature::new);
}

package com.truemetallurgy.registry;

import com.truemetallurgy.TrueMetallurgy;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Custom workshop particles. All other effects reuse vanilla particles. */
public final class ModParticles {
    private ModParticles() {}

    public static final DeferredRegister<ParticleType<?>> PARTICLES =
        DeferredRegister.create(Registries.PARTICLE_TYPE, TrueMetallurgy.MOD_ID);

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SPARK =
        PARTICLES.register("spark", () -> new SimpleParticleType(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> EMBER =
        PARTICLES.register("ember", () -> new SimpleParticleType(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> STEAM =
        PARTICLES.register("steam", () -> new SimpleParticleType(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> METAL_DUST =
        PARTICLES.register("metal_dust", () -> new SimpleParticleType(true));
}

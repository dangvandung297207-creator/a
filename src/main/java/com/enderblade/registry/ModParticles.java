package com.enderblade.registry;

import com.enderblade.EnderBladeMod;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModParticles {

    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, EnderBladeMod.MOD_ID);

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> VOID_SPARK =
            PARTICLE_TYPES.register("void_spark", () -> new SimpleParticleType(false));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> RIFT_DUST =
            PARTICLE_TYPES.register("rift_dust", () -> new SimpleParticleType(false));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> AFTERIMAGE_MIST =
            PARTICLE_TYPES.register("afterimage_mist", () -> new SimpleParticleType(false));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> MARK_RUNE =
            PARTICLE_TYPES.register("mark_rune", () -> new SimpleParticleType(false));

    private ModParticles() {
    }
}

package com.masterblacksmith;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Custom forge particles. All spawning is capped (see ParticleUtil + MBSConfig). */
public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, MasterBlacksmith.MOD_ID);

    public static final RegistryObject<SimpleParticleType> EMBER =
            PARTICLES.register("ember", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> FORGE_SPARK =
            PARTICLES.register("forge_spark", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> QUENCH_STEAM =
            PARTICLES.register("quench_steam", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> GRIND_DUST =
            PARTICLES.register("grind_dust", () -> new SimpleParticleType(true));
}

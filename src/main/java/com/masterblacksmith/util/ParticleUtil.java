package com.masterblacksmith.util;

import com.masterblacksmith.MBSConfig;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

/**
 * All mod particle spawning funnels through here so one config value caps
 * every effect. A forge room stays smooth no matter how hot it gets.
 */
public final class ParticleUtil {
    private ParticleUtil() {}

    public static int cap(int wanted) {
        return Math.min(wanted, MBSConfig.MAX_PARTICLES_PER_EFFECT.get());
    }

    /** Client-side puff (no-op on a dedicated server level). */
    public static void spawnCapped(Level level, ParticleOptions particle,
                                   double x, double y, double z, int count) {
        if (!level.isClientSide) return;
        int n = cap(count);
        for (int i = 0; i < n; i++) {
            double vx = (level.random.nextDouble() - 0.5) * 0.06;
            double vy = level.random.nextDouble() * 0.08;
            double vz = (level.random.nextDouble() - 0.5) * 0.06;
            level.addParticle(particle, x, y, z, vx, vy, vz);
        }
    }

    /** Server-side burst replicated to nearby clients. */
    public static void burst(ServerLevel level, ParticleOptions particle,
                             double x, double y, double z, int count,
                             double spread, double speed) {
        level.sendParticles(particle, x, y, z, cap(count), spread, spread, spread, speed);
    }

    public static void burst(ServerLevel level, ParticleOptions particle,
                             double x, double y, double z, int count) {
        burst(level, particle, x, y, z, count, 0.25, 0.15);
    }
}

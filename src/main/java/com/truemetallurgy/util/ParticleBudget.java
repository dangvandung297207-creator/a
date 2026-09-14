package com.truemetallurgy.util;

import com.truemetallurgy.config.TMConfig;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;

/**
 * Global per-tick particle budget. Every server-driven effect goes through
 * here so a full workshop can never flood the network.
 */
public final class ParticleBudget {
    private ParticleBudget() {}

    private static long lastTick = -1;
    private static int used = 0;

    public static synchronized void puff(ServerLevel level, ParticleOptions particle,
            double x, double y, double z, int count,
            double dx, double dy, double dz, double speed) {
        long now = level.getGameTime();
        if (now != lastTick) {
            lastTick = now;
            used = 0;
        }
        int budget = TMConfig.particleBudget();
        int remaining = Math.max(0, budget - used);
        int send = Math.min(count, Math.min(remaining, 24));
        if (send <= 0) return;
        used += send;
        level.sendParticles(particle, x, y, z, send, dx, dy, dz, speed);
    }
}

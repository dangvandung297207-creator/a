package com.masterblacksmith.forging;

/** Temperature math: heating curves, cooling curves and glow colours. */
public final class HeatingHelper {
    private HeatingHelper() {}

    /** Move current toward target by at most rate*dt. Temperatures in C. */
    public static float approach(float current, float target, float ratePerSec, float dtSec) {
        float step = ratePerSec * dtSec;
        if (Math.abs(target - current) <= step) return target;
        return current + Math.signum(target - current) * step;
    }

    /** Newtonian-ish cooling toward ambient. */
    public static float cool(float temp, float ambient, float ratePerSec, float dtSec) {
        if (temp <= ambient) return ambient;
        float k = Math.min(1F, ratePerSec * dtSec / Math.max(1F, temp - ambient + 200F) * 8F);
        return Math.max(ambient, temp - (temp - ambient) * k - ratePerSec * dtSec * 0.05F);
    }

    /** Glow overlay colour for hot metal. Cold metal returns 0 (no glow). */
    public static int glowColorForTemp(float temp) {
        if (temp < 400) return 0;
        if (temp < 550) return 0x5A1408;
        if (temp < 700) return 0xA8230A;
        if (temp < 850) return 0xE8541A;
        if (temp < 1000) return 0xFF7A1A;
        if (temp < 1150) return 0xFFAA2A;
        if (temp < 1300) return 0xFFE27A;
        return 0xFFF6D8;
    }

    /** 0..1 emissive intensity used by light level and particles. */
    public static float glowIntensity(float temp) {
        if (temp < 400) return 0F;
        return Math.min(1F, (temp - 400F) / 900F);
    }

    /** Light level 0..15 emitted by metal at this temperature. */
    public static int lightForTemp(float temp) {
        return Math.min(15, (int) (glowIntensity(temp) * 12F));
    }
}

package com.masterblacksmith.forging;

import com.masterblacksmith.MBSConfig;
import net.minecraft.world.item.ItemStack;

/**
 * Craftsmanship scoring. Weighted from real smithing inputs: heating accuracy,
 * hammer accuracy, reheats, quench, grind and assembly. Skill is repeatable.
 */
public final class QualityCalculator {
    private QualityCalculator() {}

    /** Component (forging-phase) score 0..100, stored when the blank completes. */
    public static float componentScore(ItemStack work) {
        float strikeAvg = ForgingData.avgStrikeQuality(work);
        float heatAvg = ForgingData.avgHeatQuality(work);
        float purityBonus = (ForgingData.getPurity(work) - 85F) * 0.5F;
        float reheatPenalty = (float) (ForgingData.getReheats(work) * MBSConfig.REHEAT_QUALITY_PENALTY.get());
        return clamp(0.55F * strikeAvg + 0.35F * heatAvg + purityBonus - reheatPenalty);
    }

    /** Final assembled score 0..100. */
    public static int finalScore(float forgingQ, float quenchQ, float grindQ, float assemblyQ,
                                 boolean hasBlueprint, int anvilCap) {
        float score = 0.40F * forgingQ + 0.15F * quenchQ + 0.15F * grindQ + 0.30F * assemblyQ;
        if (hasBlueprint) score += 4F;
        score = Math.min(score, anvilCap);
        if (!hasBlueprint && MBSConfig.REQUIRE_BLUEPRINT_FOR_LEGENDARY.get()) {
            score = Math.min(score, MBSConfig.LEGENDARY_THRESHOLD.get() - 1);
        }
        return Math.round(clamp(score));
    }

    /** Quench quality from the temperature window and the medium. */
    public static float quenchQuality(float quenchTemp, float sweetMin, float forgeMax) {
        if (quenchTemp >= sweetMin - 100 && quenchTemp <= forgeMax) return 95F;
        if (quenchTemp > forgeMax) return 62F;
        if (quenchTemp >= 400) return 70F;
        return 40F;
    }

    /** Assembly quality from parts, handle and care. */
    public static float assemblyQuality(int partsBonus, float handleGrip, boolean fullSet) {
        float q = 62F + partsBonus + handleGrip * 18F;
        if (fullSet) q += 6F;
        return clamp(q);
    }

    private static float clamp(float v) {
        return Math.max(0F, Math.min(100F, v));
    }
}

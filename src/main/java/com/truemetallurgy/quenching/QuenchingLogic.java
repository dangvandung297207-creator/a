package com.truemetallurgy.quenching;

import com.truemetallurgy.components.ForgedComponentData;
import com.truemetallurgy.item.ForgedComponentItem;
import com.truemetallurgy.metallurgy.Material;
import com.truemetallurgy.metallurgy.Materials;
import com.truemetallurgy.metallurgy.QuenchMedium;
import com.truemetallurgy.registry.ModDataComponents;
import net.minecraft.world.item.ItemStack;

/**
 * Server-side quenching math. The barrel owns the liquid; this class owns the
 * metallurgy: temperature requirements, score effects and data transitions.
 */
public final class QuenchingLogic {
    private QuenchingLogic() {}

    /** Minimum workpiece temperature for a real quench. */
    public static int requiredTemp(Material material) {
        return Math.max(650, material.forgeMin() - 100);
    }

    public static boolean canQuench(ItemStack stack) {
        if (!(stack.getItem() instanceof ForgedComponentItem)) return false;
        ForgedComponentData data = ForgedComponentItem.dataOf(stack);
        if (!data.isFinished() || data.quenched()) return false;
        Material mat = Materials.get(data.materialId());
        return data.temperature() >= requiredTemp(mat);
    }

    /** Failure reason key, or null when quenching is allowed. */
    public static String failureReason(ItemStack stack) {
        if (!(stack.getItem() instanceof ForgedComponentItem)) return "message.true_metallurgy.quench_not_component";
        ForgedComponentData data = ForgedComponentItem.dataOf(stack);
        if (!data.isFinished()) return "message.true_metallurgy.quench_unfinished";
        if (data.quenched()) return "message.true_metallurgy.quench_already";
        Material mat = Materials.get(data.materialId());
        if (data.temperature() < requiredTemp(mat)) return "message.true_metallurgy.quench_too_cold";
        return null;
    }

    /** Apply a quench in place. Returns the new score. */
    public static float apply(ItemStack stack, QuenchMedium medium) {
        ForgedComponentData data = ForgedComponentItem.dataOf(stack);
        Material mat = Materials.get(data.materialId());
        float accuracy = mat.heatAccuracy(data.temperature());
        float score = data.score() + medium.qualityBonus() + (accuracy > 0.5F ? 2.0F : -1.0F);
        score = Math.max(0.0F, Math.min(100.0F, score));
        int restTemp = medium.waterLike() ? 60 : 120;
        ForgedComponentData next = new ForgedComponentData(
            data.materialId(), data.kind(), data.stage(), data.stageProgress(),
            data.strikes(), data.perfects(), data.goods(), data.misses(), data.bads(),
            data.reheats(), data.purity(), score, restTemp,
            true, medium.id(), data.grindAngle(), data.grindQuality(),
            data.crafterName(), data.crafterId());
        stack.set(ModDataComponents.COMPONENT.get(), next);
        return score;
    }
}

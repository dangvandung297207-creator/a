package com.truemetallurgy.grinding;

import com.truemetallurgy.components.ForgedComponentData;
import com.truemetallurgy.item.ForgedComponentItem;
import com.truemetallurgy.metallurgy.GrindAngle;
import com.truemetallurgy.registry.ModDataComponents;
import net.minecraft.world.item.ItemStack;

/** Server-side grinding math: edge angles trade sharpness for durability. */
public final class GrindingLogic {
    private GrindingLogic() {}

    public static boolean canGrind(ItemStack stack) {
        if (!(stack.getItem() instanceof ForgedComponentItem)) return false;
        return ForgedComponentItem.dataOf(stack).isFinished();
    }

    public static String failureReason(ItemStack stack) {
        if (!(stack.getItem() instanceof ForgedComponentItem)) return "message.true_metallurgy.grind_not_component";
        if (!ForgedComponentItem.dataOf(stack).isFinished()) return "message.true_metallurgy.grind_unfinished";
        return null;
    }

    /**
     * Apply a grinding pass. Wheel wear (0-100) lowers edge quality; grinding
     * before quenching is possible but penalized.
     */
    public static float apply(ItemStack stack, GrindAngle angle, int wheelWear) {
        ForgedComponentData data = ForgedComponentItem.dataOf(stack);
        float edgeQuality = Math.max(20.0F, 100.0F - wheelWear * 0.4F);
        float score = data.score();
        if (data.quenched()) {
            score += 2.0F;
        } else {
            score -= 3.0F;
        }
        if (data.grindAngle() >= 0 && data.grindAngle() != angle.degrees) {
            score -= 2.0F;
            edgeQuality = Math.min(edgeQuality, data.grindQuality() - 5.0F);
        }
        score = Math.max(0.0F, Math.min(100.0F, score));
        ForgedComponentData next = new ForgedComponentData(
            data.materialId(), data.kind(), data.stage(), data.stageProgress(),
            data.strikes(), data.perfects(), data.goods(), data.misses(), data.bads(),
            data.reheats(), data.purity(), score, data.temperature(),
            data.quenched(), data.quenchId(), angle.degrees, Math.max(0.0F, edgeQuality),
            data.crafterName(), data.crafterId());
        stack.set(ModDataComponents.COMPONENT.get(), next);
        return score;
    }
}

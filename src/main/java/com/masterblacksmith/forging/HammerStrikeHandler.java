package com.masterblacksmith.forging;

import com.masterblacksmith.MBSConfig;
import com.masterblacksmith.item.HammerTier;
import com.masterblacksmith.material.MetalMaterial;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Scores one hammer strike from aim, force, temperature and material response.
 * Skilled smiths produce good steel reliably: nothing here is pure random.
 */
public final class HammerStrikeHandler {
    public record Outcome(StrikeResult result, float score, boolean stageAdvanced,
                          boolean templateComplete, boolean ruined, Component message) {}

    private HammerStrikeHandler() {}

    public static Outcome strike(Player player, ItemStack work, HammerTier hammer,
                                 double anvilTolerance, float aim01, float force01) {
        // A raw billet must be committed to a template before shaping.
        if (work.getItem() instanceof com.masterblacksmith.item.BilletItem
                && ForgingData.peek(work).getString("Template").isEmpty()) {
            return new Outcome(StrikeResult.MISS, 5F, false, false, false,
                    Component.translatable("message.masterblacksmith.choose_template"));
        }
        MetalMaterial metal = ForgingData.getMetal(work);
        float temp = ForgingData.getTemp(work);
        HeatZone zone = HeatZone.zoneFor(metal, temp);

        // Cold steel: hammering is ineffective and may crack the work.
        if (zone == HeatZone.COLD || zone == HeatZone.WARM) {
            float purityLoss = force01 > 0.7F ? 2.0F : 0.5F;
            ForgingData.setPurity(work, ForgingData.getPurity(work) - purityLoss);
            ForgingData.recordStrike(work, 12F, HeatZone.heatScore(metal, temp));
            ForgingData.addHistory(work, "strike.cold");
            StrikeResult r = zone == HeatZone.COLD && force01 > 0.75F ? StrikeResult.BAD : StrikeResult.MISS;
            return new Outcome(r, 12F, false, false, false,
                    Component.translatable("message.masterblacksmith.too_cold"));
        }

        // Molten: the billet is ruined into slag.
        if (zone == HeatZone.MOLTEN) {
            return new Outcome(StrikeResult.BAD, 0F, false, false, true,
                    Component.translatable("message.masterblacksmith.burned"));
        }

        boolean overheated = zone == HeatZone.OVERHEAT;

        // Aim: distance from the sweet spot of the timing bar / hit position.
        double tolerance = 0.30 * anvilTolerance * hammer.precision();
        double dist = Math.abs(aim01 - 0.5);
        float accuracy = (float) Math.max(0.0, 1.0 - dist / Math.max(0.05, tolerance));

        // Temperature response.
        float heatScore = HeatZone.heatScore(metal, temp);
        float heatFactor = heatScore / 100F;

        // Force: controlled blows beat wild swinging.
        float forceFactor;
        if (force01 < 0.25F) forceFactor = 0.55F;
        else if (force01 <= 0.85F) forceFactor = 1.0F;
        else forceFactor = 0.78F;

        float score = 100F * (0.52F * accuracy + 0.30F * heatFactor + 0.18F * forceFactor)
                * (0.85F + 0.15F * metal.getWorkability()) * hammer.efficiency();

        // Overworking: extra strikes past the stage requirement cost quality.
        int[] need = stageNeed(work);
        int strikes = ForgingData.getStrikes(work);
        if (strikes >= need[0]) {
            score -= (float) (MBSConfig.OVERWORK_QUALITY_PENALTY.get() * 4.0 * (strikes - need[0] + 1));
        }
        score = Math.max(0F, Math.min(100F, score));

        StrikeResult result = score >= 88 ? StrikeResult.PERFECT
                : score >= 60 ? StrikeResult.GOOD
                : score >= 35 ? StrikeResult.MISS : StrikeResult.BAD;

        // Overheat oxidation eats purity and can pit the surface.
        if (overheated) {
            double lossChance = MBSConfig.OXIDATION_LOSS_CHANCE.get();
            if (player.getRandom().nextDouble() < lossChance) {
                ForgingData.setPurity(work, ForgingData.getPurity(work) - (1F + player.getRandom().nextFloat() * 2F));
                ForgingData.addHistory(work, "strike.scaled");
                score = Math.max(0F, score - 8F);
            } else {
                ForgingData.addHistory(work, "strike.hot");
            }
        }

        ForgingData.recordStrike(work, score, heatScore);

        boolean advanced = false;
        boolean complete = false;
        if (result != StrikeResult.BAD && ForgingData.getStrikes(work) >= need[0]) {
            if (need[1] == 1) {
                complete = true;
            } else {
                ForgingData.setStage(work, ForgingData.getStage(work) + 1);
                ForgingData.setStrikes(work, 0);
                advanced = true;
            }
        }
        return new Outcome(result, score, advanced, complete, false,
                Component.translatable("message.masterblacksmith." + result.name().toLowerCase()));
    }

    /** Returns {requiredStrikesForCurrentStage, isLastStage}. */
    private static int[] stageNeed(ItemStack work) {
        String templateId = ForgingData.getTemplateId(work);
        if (templateId == null || templateId.isEmpty()) {
            return new int[]{6, 1}; // bloom consolidation
        }
        ForgingTemplate template = ForgingTemplates.get(templateId);
        int stage = Math.min(ForgingData.getStage(work), template.stageCount() - 1);
        int last = stage >= template.stageCount() - 1 ? 1 : 0;
        return new int[]{template.getStages().get(stage).requiredStrikes(), last};
    }
}

package com.truemetallurgy.forging;

import com.truemetallurgy.components.BilletData;
import com.truemetallurgy.components.ForgedComponentData;
import com.truemetallurgy.config.TMConfig;
import com.truemetallurgy.item.BilletItem;
import com.truemetallurgy.item.ComponentKind;
import com.truemetallurgy.item.HammerTier;
import com.truemetallurgy.block.AnvilTier;
import com.truemetallurgy.metallurgy.ForgingShape;
import com.truemetallurgy.metallurgy.Material;
import com.truemetallurgy.metallurgy.Materials;
import com.truemetallurgy.registry.ModDataComponents;
import com.truemetallurgy.registry.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * Server-authoritative forging math. Every strike is graded from position,
 * timing, force, temperature and material response. No random quality: a
 * skilled smith reliably produces masterwork.
 */
public final class ForgingLogic {
    private ForgingLogic() {}

    /** Base hit window radius on the anvil face (0-0.5 scale). */
    private static final float BASE_WINDOW = 0.11F;

    public static ForgedComponentData withTemp(ForgedComponentData data, int temp) {
        return new ForgedComponentData(
            data.materialId(), data.kind(), data.stage(), data.stageProgress(),
            data.strikes(), data.perfects(), data.goods(), data.misses(), data.bads(),
            data.reheats(), data.purity(), data.score(), temp,
            data.quenched(), data.quenchId(), data.grindAngle(), data.grindQuality(),
            data.crafterName(), data.crafterId());
    }

    /** Material id for a bloom item stack, or null when not a bloom. */
    public static String bloomMaterial(ItemStack stack) {
        if (stack.is(ModItems.IRON_BLOOM.get())) return "iron";
        if (stack.is(ModItems.COPPER_BLOOM.get())) return "copper";
        if (stack.is(ModItems.STEEL_BLOOM.get())) return "steel";
        if (stack.is(ModItems.HARDENED_BLOOM.get())) return "hardened_steel";
        return null;
    }

    /** Ensure a workpiece carries live forging progress; returns the material. */
    public static Material ensureProgress(ItemStack stack, String kind, ServerPlayer crafter) {
        String materialId;
        float purity;
        BilletData billet = stack.get(ModDataComponents.BILLET.get());
        if (stack.getItem() instanceof BilletItem billetItem) {
            materialId = billetItem.getMaterial().id();
            purity = billet != null ? billet.purity() : billetItem.getMaterial().purity();
            if (billet == null) {
                stack.set(ModDataComponents.BILLET.get(),
                    new BilletData(materialId, com.truemetallurgy.metallurgy.Heat.ROOM_TEMP, purity, 0));
            }
        } else {
            String bloom = bloomMaterial(stack);
            materialId = bloom != null ? bloom : "iron";
            Material mat = Materials.get(materialId);
            purity = billet != null ? billet.purity() : mat.purity();
            if (billet == null) {
                stack.set(ModDataComponents.BILLET.get(),
                    new BilletData(materialId, com.truemetallurgy.metallurgy.Heat.ROOM_TEMP, purity, 0));
            }
        }
        ForgedComponentData comp = stack.get(ModDataComponents.COMPONENT.get());
        if (comp == null) {
            Material mat = Materials.get(materialId);
            float start = 52.0F + purity * 0.12F;
            comp = new ForgedComponentData(materialId, kind, 0, 0, 0, 0, 0, 0, 0,
                billet != null ? billet.reheats() : 0, purity, start,
                billet != null ? billet.temperature() : 20, false, "none", -1, 0.0F,
                crafter.getGameProfile().getName(), crafter.getStringUUID());
            stack.set(ModDataComponents.COMPONENT.get(), comp);
        }
        return Materials.get(materialId);
    }

    /** Grade one strike. Pure function of skill + state. */
    public static StrikeResult evaluate(int temp, Material material, ForgedComponentData comp,
            ForgingShape shape, float targetX, float targetZ, float hitX, float hitZ,
            long ticksSinceLast, HammerTier hammer, AnvilTier anvil) {
        float toleranceMult;
        try {
            toleranceMult = TMConfig.FORGING_TOLERANCE_MULT.get().floatValue();
        } catch (IllegalStateException notLoaded) {
            toleranceMult = 1.0F;
        }
        float window = BASE_WINDOW * hammer.tolerance * anvil.tolerance * toleranceMult
            / (1.0F + material.forgingDifficulty());
        float dist = (float) Math.hypot(hitX - targetX, hitZ - targetZ);

        StrikeResult.Grade grade;
        if (dist <= window * 0.45F) grade = StrikeResult.Grade.PERFECT;
        else if (dist <= window) grade = StrikeResult.Grade.GOOD;
        else if (dist <= window * 2.2F) grade = StrikeResult.Grade.MISS;
        else grade = StrikeResult.Grade.BAD;

        boolean tooCold = temp < material.forgeMin() - 50;
        boolean tooHot = temp > material.forgeMax() + 50;
        if (tooCold && (grade == StrikeResult.Grade.PERFECT || grade == StrikeResult.Grade.GOOD)) {
            grade = StrikeResult.Grade.MISS;
        }
        if (tooHot && grade == StrikeResult.Grade.PERFECT) {
            grade = StrikeResult.Grade.GOOD;
        }

        boolean rushed = ticksSinceLast < 7;
        if (rushed && grade == StrikeResult.Grade.PERFECT) {
            grade = StrikeResult.Grade.GOOD;
        }

        float accuracy = material.heatAccuracy(temp);
        float progress;
        float scoreDelta;
        switch (grade) {
            case PERFECT -> {
                progress = hammer.strength * (0.7F + 0.6F * accuracy);
                scoreDelta = 3.0F * (0.4F + 0.6F * accuracy);
            }
            case GOOD -> {
                progress = hammer.strength * (0.45F + 0.45F * accuracy);
                scoreDelta = 1.5F * (0.4F + 0.6F * accuracy);
            }
            case MISS -> {
                progress = tooCold ? 0.0F : hammer.strength * 0.15F;
                scoreDelta = -4.0F;
            }
            default -> {
                progress = 0.0F;
                scoreDelta = -8.0F;
            }
        }
        if (rushed) scoreDelta -= 1.0F;
        if (ticksSinceLast > 120) scoreDelta -= 0.5F;

        // Overworking: extra strikes past the stage requirement hurt.
        int stage = Math.min(comp.stage(), shape.stages().size() - 1);
        int required = shape.stages().get(Math.max(0, stage)).strikesRequired();
        if (comp.stageProgress() >= required + 2) {
            scoreDelta -= 1.0F;
        }

        float purityDelta = 0.0F;
        boolean cracked = false;
        if (tooHot) {
            purityDelta -= 1.5F / material.heatResistance();
            scoreDelta -= 2.0F;
        }
        if (tooCold && grade == StrikeResult.Grade.BAD) {
            // Cold bad hit cracks the workpiece. Deterministic, avoidable.
            cracked = true;
            purityDelta -= 4.0F * (1.2F - Math.min(1.0F, material.flexibility() * 0.5F));
            scoreDelta -= 4.0F;
        }
        return new StrikeResult(grade, progress, scoreDelta, purityDelta, cracked, tooCold, tooHot, rushed, false);
    }

    /** Apply a resolved strike to the workpiece progress. Returns true when the shape finished. */
    public static boolean applyStrike(ItemStack stack, StrikeResult result, ForgingShape shape,
            ForgingSession session, boolean masterBonus) {
        ForgedComponentData comp = stack.get(ModDataComponents.COMPONENT.get());
        if (comp == null) return false;
        int stage = comp.stage();
        float whole = session.takeCarry(result.progress());
        int progress = comp.stageProgress() + (int) whole;
        int strikes = comp.strikes() + 1;
        int perfects = comp.perfects() + (result.grade() == StrikeResult.Grade.PERFECT ? 1 : 0);
        int goods = comp.goods() + (result.grade() == StrikeResult.Grade.GOOD ? 1 : 0);
        int misses = comp.misses() + (result.grade() == StrikeResult.Grade.MISS ? 1 : 0);
        int bads = comp.bads() + (result.grade() == StrikeResult.Grade.BAD ? 1 : 0);
        float score = comp.score() + result.scoreDelta();
        float purity = Math.max(0.0F, comp.purity() + result.purityDelta());

        int guard = 0;
        while (stage < shape.stages().size() && progress >= shape.stages().get(stage).strikesRequired() && guard++ < 8) {
            progress -= shape.stages().get(stage).strikesRequired();
            stage++;
        }
        boolean finished = stage >= shape.stages().size();
        if (finished) {
            stage = -1;
            progress = 0;
            if (masterBonus) score += 2.0F;
            // Efficiency bonus: fewer strikes than 130% of required rewards control.
            int required = shape.totalStrikes();
            if (strikes <= required * 1.3F) score += 3.0F;
        }
        score = Math.max(0.0F, Math.min(100.0F, score));
        ForgedComponentData next = new ForgedComponentData(
            comp.materialId(), comp.kind(), stage, progress, strikes, perfects, goods, misses, bads,
            comp.reheats(), purity, score, comp.temperature(),
            comp.quenched(), comp.quenchId(), comp.grindAngle(), comp.grindQuality(),
            comp.crafterName(), comp.crafterId());
        stack.set(ModDataComponents.COMPONENT.get(), next);
        return finished;
    }

    /** Mirror temperature between the billet and progress components. */
    public static void setWorkpieceTemp(ItemStack stack, int temp) {
        BilletData billet = stack.get(ModDataComponents.BILLET.get());
        if (billet != null) {
            stack.set(ModDataComponents.BILLET.get(), billet.withTemp(temp));
        }
        ForgedComponentData comp = stack.get(ModDataComponents.COMPONENT.get());
        if (comp != null) {
            stack.set(ModDataComponents.COMPONENT.get(), withTemp(comp, temp));
        }
    }

    /** Build the finished component item from a completed workpiece. */
    public static ItemStack finishComponent(ItemStack workpiece, ComponentKind kind) {
        ForgedComponentData comp = workpiece.get(ModDataComponents.COMPONENT.get());
        if (comp == null) comp = ForgedComponentData.EMPTY;
        ItemStack out = switch (kind) {
            case SWORD_BLADE -> new ItemStack(ModItems.SWORD_BLADE.get());
            case AXE_HEAD -> new ItemStack(ModItems.AXE_HEAD.get());
            case PICKAXE_HEAD -> new ItemStack(ModItems.PICKAXE_HEAD.get());
            case SPEAR_HEAD -> new ItemStack(ModItems.SPEAR_HEAD.get());
        };
        BilletData billet = workpiece.get(ModDataComponents.BILLET.get());
        int temp = billet != null ? billet.temperature() : comp.temperature();
        ForgedComponentData finished = new ForgedComponentData(
            comp.materialId(), kind.shapeId, -1, 0, comp.strikes(), comp.perfects(), comp.goods(),
            comp.misses(), comp.bads(), comp.reheats(), comp.purity(), comp.score(), temp,
            false, "none", -1, 0.0F, comp.crafterName(), comp.crafterId());
        out.set(ModDataComponents.COMPONENT.get(), finished);
        return out;
    }

    /** Build a fresh billet from a consolidated bloom. */
    public static ItemStack finishBillet(ItemStack bloomWorkpiece) {
        ForgedComponentData comp = bloomWorkpiece.get(ModDataComponents.COMPONENT.get());
        BilletData billet = bloomWorkpiece.get(ModDataComponents.BILLET.get());
        String materialId = comp != null ? comp.materialId()
            : (billet != null ? billet.materialId() : "iron");
        int temp = billet != null ? billet.temperature() : 20;
        float purity = comp != null ? comp.purity() : Materials.get(materialId).purity();
        int reheats = comp != null ? comp.reheats() : 0;
        ItemStack out = new ItemStack(ModItems.billetFor(materialId));
        out.set(ModDataComponents.BILLET.get(), new BilletData(materialId, temp, purity, reheats));
        return out;
    }
}

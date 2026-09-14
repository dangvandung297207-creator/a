package com.enderblade.client.model;

import com.enderblade.EnderBladeMod;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;

/**
 * Refined legendary Ender Blade geometry (parity with Three.js showcase).
 * Longer asymmetrical silhouette, void cracks, Eye-of-Ender core, separated parts.
 *
 * Parts: blade, blade_edge, void_cracks, guard, handle, pommel, ender_core, runes, emissive
 */
public class EnderBladeModel {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(EnderBladeMod.MOD_ID, "ender_blade"), "main");

    public static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(EnderBladeMod.MOD_ID, "textures/item/ender_blade_3d.png");

    private final ModelPart root;
    private final ModelPart enderCore;
    private final ModelPart voidCracks;
    private final ModelPart runes;
    private final ModelPart emissive;

    public EnderBladeModel(ModelPart root) {
        this.root = root.getChild("root");
        this.enderCore = this.root.getChild("ender_core");
        this.voidCracks = this.root.getChild("void_cracks");
        this.runes = this.root.getChild("runes");
        this.emissive = this.root.getChild("emissive");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition def = mesh.getRoot();
        PartDefinition root = def.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.ZERO);

        // ---- BLADE: longer, sharper, slight right-bias asymmetry ----
        root.addOrReplaceChild("blade", CubeListBuilder.create()
                // Tip needle
                .texOffs(0, 0).addBox(-0.18f, 19.6f, -0.12f, 0.45f, 1.1f, 0.24f)
                .texOffs(0, 0).addBox(-0.28f, 18.5f, -0.13f, 0.65f, 1.2f, 0.26f)
                .texOffs(0, 0).addBox(-0.40f, 17.2f, -0.14f, 0.90f, 1.4f, 0.28f)
                // Upper (asymmetric offset via wider right)
                .texOffs(0, 0).addBox(-0.55f, 15.4f, -0.16f, 1.25f, 1.9f, 0.32f)
                .texOffs(0, 0).addBox(-0.70f, 13.4f, -0.17f, 1.50f, 2.1f, 0.34f)
                .texOffs(0, 0).addBox(-0.82f, 11.2f, -0.18f, 1.70f, 2.3f, 0.36f)
                // Mid
                .texOffs(0, 0).addBox(-0.92f, 8.8f, -0.19f, 1.85f, 2.5f, 0.38f)
                .texOffs(0, 0).addBox(-0.98f, 6.4f, -0.20f, 1.95f, 2.5f, 0.40f)
                .texOffs(0, 0).addBox(-1.02f, 4.2f, -0.21f, 2.05f, 2.3f, 0.42f)
                // Lower toward guard
                .texOffs(0, 0).addBox(-1.08f, 2.2f, -0.22f, 2.15f, 2.1f, 0.44f)
                .texOffs(0, 0).addBox(-1.12f, 0.8f, -0.24f, 2.25f, 1.5f, 0.48f)
                .texOffs(0, 0).addBox(-1.16f, 0.1f, -0.25f, 2.32f, 0.8f, 0.50f)
                // Fuller / bevel groove
                .texOffs(16, 0).addBox(-0.22f, 2.0f, 0.20f, 0.44f, 15.5f, 0.10f)
                .texOffs(16, 0).addBox(-0.22f, 2.0f, -0.30f, 0.44f, 15.5f, 0.10f)
                // Tip fractures
                .texOffs(16, 0).addBox(0.35f, 18.0f, 0.12f, 0.30f, 0.55f, 0.14f)
                .texOffs(16, 0).addBox(-0.50f, 18.6f, -0.16f, 0.28f, 0.45f, 0.12f),
                PartPose.ZERO);

        // ---- BLADE EDGE: sharp rim ----
        root.addOrReplaceChild("blade_edge", CubeListBuilder.create()
                .texOffs(32, 0).addBox(-1.42f, 1.5f, -0.09f, 0.28f, 17.5f, 0.18f)
                .texOffs(32, 0).addBox(1.18f, 1.5f, -0.09f, 0.28f, 17.2f, 0.18f)
                .texOffs(32, 0).addBox(-0.35f, 20.4f, -0.09f, 0.80f, 0.55f, 0.18f)
                .texOffs(56, 0).addBox(-0.18f, 20.7f, -0.08f, 0.40f, 0.40f, 0.16f),
                PartPose.ZERO);

        // ---- VOID CRACKS: deep energy channels ----
        root.addOrReplaceChild("void_cracks", CubeListBuilder.create()
                // Front primary diagonal
                .texOffs(40, 0).addBox(-0.18f, 16.5f, 0.24f, 0.22f, 2.8f, 0.14f)
                .texOffs(40, 0).addBox(0.02f, 13.6f, 0.24f, 0.22f, 3.0f, 0.14f)
                .texOffs(40, 0).addBox(-0.20f, 10.4f, 0.24f, 0.24f, 3.2f, 0.14f)
                .texOffs(40, 0).addBox(0.08f, 7.2f, 0.24f, 0.22f, 3.0f, 0.14f)
                .texOffs(40, 0).addBox(-0.12f, 4.4f, 0.24f, 0.22f, 2.8f, 0.14f)
                .texOffs(40, 0).addBox(0.10f, 2.2f, 0.24f, 0.20f, 2.2f, 0.14f)
                // Secondary branch
                .texOffs(40, 0).addBox(0.30f, 14.5f, 0.24f, 0.18f, 2.6f, 0.12f)
                .texOffs(40, 0).addBox(0.38f, 11.0f, 0.24f, 0.18f, 2.8f, 0.12f)
                .texOffs(40, 0).addBox(0.28f, 7.5f, 0.24f, 0.18f, 2.6f, 0.12f)
                // Reverse face
                .texOffs(40, 8).addBox(-0.10f, 15.0f, -0.38f, 0.20f, 3.5f, 0.14f)
                .texOffs(40, 8).addBox(0.12f, 11.0f, -0.38f, 0.20f, 3.8f, 0.14f)
                .texOffs(40, 8).addBox(-0.15f, 7.0f, -0.38f, 0.22f, 3.5f, 0.14f)
                .texOffs(40, 8).addBox(0.08f, 3.5f, -0.38f, 0.20f, 3.2f, 0.14f)
                // Tip leak
                .texOffs(40, 0).addBox(-0.08f, 18.8f, 0.24f, 0.18f, 1.4f, 0.12f)
                .texOffs(40, 8).addBox(0.00f, 18.5f, -0.36f, 0.16f, 1.2f, 0.12f),
                PartPose.ZERO);

        // ---- GUARD: angular netherite + energy channels ----
        root.addOrReplaceChild("guard", CubeListBuilder.create()
                .texOffs(0, 32).addBox(-3.6f, -0.45f, -0.70f, 7.2f, 0.95f, 1.40f)
                .texOffs(0, 32).addBox(-1.1f, -0.15f, -0.85f, 2.2f, 1.20f, 1.70f)
                // Asym wings
                .texOffs(0, 32).addBox(-3.7f, 0.25f, -0.50f, 1.35f, 1.60f, 1.00f)
                .texOffs(0, 32).addBox(2.35f, 0.35f, -0.48f, 1.25f, 1.80f, 0.95f)
                .texOffs(0, 32).addBox(-4.15f, 1.25f, -0.40f, 0.85f, 1.30f, 0.80f)
                .texOffs(0, 32).addBox(3.25f, 1.45f, -0.38f, 0.80f, 1.35f, 0.75f)
                // Lower plate
                .texOffs(0, 32).addBox(-1.6f, -1.15f, -1.00f, 3.2f, 0.70f, 2.00f)
                .texOffs(0, 32).addBox(-0.8f, -1.35f, -1.10f, 1.6f, 0.40f, 2.20f)
                // Energy channels (emissive UV region)
                .texOffs(48, 32).addBox(-3.0f, -0.10f, 0.55f, 6.0f, 0.25f, 0.30f)
                // Eye motif
                .texOffs(48, 32).addBox(-0.75f, -0.10f, 0.85f, 1.5f, 1.5f, 0.55f)
                .texOffs(48, 40).addBox(-0.45f, 0.20f, 1.25f, 0.9f, 0.9f, 0.40f)
                .texOffs(48, 48).addBox(-0.25f, 0.40f, 1.50f, 0.5f, 0.5f, 0.25f)
                // Crystal accents
                .texOffs(48, 48).addBox(-2.5f, 0.45f, 0.55f, 0.55f, 0.75f, 0.50f)
                .texOffs(48, 48).addBox(1.95f, 0.55f, 0.55f, 0.50f, 0.70f, 0.50f),
                PartPose.ZERO);

        // ---- HANDLE: wrap + metal collars ----
        root.addOrReplaceChild("handle", CubeListBuilder.create()
                .texOffs(16, 48).addBox(-0.70f, -1.15f, -0.70f, 1.40f, 0.60f, 1.40f)
                .texOffs(0, 48).addBox(-0.50f, -2.30f, -0.50f, 1.00f, 1.15f, 1.00f)
                .texOffs(0, 48).addBox(-0.48f, -3.50f, -0.48f, 0.96f, 1.15f, 0.96f)
                .texOffs(0, 48).addBox(-0.50f, -4.70f, -0.50f, 1.00f, 1.15f, 1.00f)
                .texOffs(0, 48).addBox(-0.48f, -5.90f, -0.48f, 0.96f, 1.15f, 0.96f)
                .texOffs(0, 48).addBox(-0.50f, -7.10f, -0.50f, 1.00f, 1.15f, 1.00f)
                .texOffs(0, 48).addBox(-0.48f, -8.20f, -0.48f, 0.96f, 1.05f, 0.96f)
                .texOffs(16, 48).addBox(-0.65f, -8.70f, -0.65f, 1.30f, 0.50f, 1.30f)
                // wrap ridges
                .texOffs(16, 48).addBox(-0.55f, -2.55f, -0.55f, 1.10f, 0.22f, 1.10f)
                .texOffs(16, 48).addBox(-0.55f, -3.75f, -0.55f, 1.10f, 0.22f, 1.10f)
                .texOffs(16, 48).addBox(-0.55f, -4.95f, -0.55f, 1.10f, 0.22f, 1.10f)
                .texOffs(16, 48).addBox(-0.55f, -6.15f, -0.55f, 1.10f, 0.22f, 1.10f)
                .texOffs(16, 48).addBox(-0.55f, -7.35f, -0.55f, 1.10f, 0.22f, 1.10f),
                PartPose.ZERO);

        // ---- RUNES ----
        root.addOrReplaceChild("runes", CubeListBuilder.create()
                .texOffs(48, 16).addBox(-0.22f, -2.40f, 0.48f, 0.44f, 0.42f, 0.14f)
                .texOffs(48, 16).addBox(-0.28f, -2.30f, 0.50f, 0.56f, 0.14f, 0.12f)
                .texOffs(48, 16).addBox(-0.22f, -3.60f, 0.48f, 0.44f, 0.42f, 0.14f)
                .texOffs(48, 16).addBox(-0.22f, -4.80f, 0.48f, 0.44f, 0.42f, 0.14f)
                .texOffs(48, 16).addBox(-0.28f, -4.70f, 0.50f, 0.56f, 0.14f, 0.12f)
                .texOffs(48, 16).addBox(-0.22f, -6.00f, 0.48f, 0.44f, 0.42f, 0.14f)
                .texOffs(48, 16).addBox(-0.22f, -7.20f, 0.48f, 0.44f, 0.40f, 0.14f)
                .texOffs(48, 16).addBox(-0.22f, -8.20f, 0.48f, 0.44f, 0.35f, 0.14f)
                // side ticks
                .texOffs(48, 16).addBox(0.48f, -2.50f, -0.12f, 0.14f, 0.35f, 0.24f)
                .texOffs(48, 16).addBox(-0.62f, -2.50f, -0.12f, 0.14f, 0.35f, 0.24f)
                .texOffs(48, 16).addBox(0.48f, -4.90f, -0.12f, 0.14f, 0.35f, 0.24f)
                .texOffs(48, 16).addBox(-0.62f, -4.90f, -0.12f, 0.14f, 0.35f, 0.24f),
                PartPose.ZERO);

        // ---- POMMEL cage ----
        root.addOrReplaceChild("pommel", CubeListBuilder.create()
                .texOffs(32, 48).addBox(-0.85f, -9.20f, -0.85f, 1.70f, 0.75f, 1.70f)
                .texOffs(32, 48).addBox(-0.65f, -9.75f, -0.65f, 1.30f, 0.55f, 1.30f)
                // cage bars
                .texOffs(32, 48).addBox(-0.22f, -11.20f, -0.72f, 0.44f, 1.50f, 0.28f)
                .texOffs(32, 48).addBox(-0.22f, -11.20f, 0.44f, 0.44f, 1.50f, 0.28f)
                .texOffs(32, 48).addBox(-0.72f, -11.20f, -0.22f, 0.28f, 1.50f, 0.44f)
                .texOffs(32, 48).addBox(0.44f, -11.20f, -0.22f, 0.28f, 1.50f, 0.44f)
                // diagonal cage
                .texOffs(32, 48).addBox(-0.55f, -11.05f, -0.55f, 0.25f, 1.30f, 0.25f)
                .texOffs(32, 48).addBox(0.30f, -11.05f, 0.30f, 0.25f, 1.30f, 0.25f)
                .texOffs(32, 48).addBox(-0.58f, -11.55f, -0.58f, 1.16f, 0.35f, 1.16f)
                .texOffs(32, 48).addBox(-0.28f, -11.90f, -0.28f, 0.56f, 0.35f, 0.56f),
                PartPose.ZERO);

        // ---- ENDER CORE (Eye of Ender) ----
        root.addOrReplaceChild("ender_core", CubeListBuilder.create()
                .texOffs(48, 56).addBox(-0.58f, -0.58f, -0.58f, 1.16f, 1.16f, 1.16f)
                .texOffs(40, 56).addBox(-0.38f, -0.38f, -0.38f, 0.76f, 0.76f, 0.76f)
                .texOffs(56, 56).addBox(-0.20f, -0.20f, -0.20f, 0.40f, 0.40f, 0.40f),
                PartPose.offset(0f, -10.45f, 0f));

        // ---- EMISSIVE accents only ----
        root.addOrReplaceChild("emissive", CubeListBuilder.create()
                .texOffs(56, 0).addBox(-0.22f, 20.3f, -0.10f, 0.50f, 0.75f, 0.20f)
                .texOffs(56, 0).addBox(-4.20f, 1.90f, -0.25f, 0.50f, 0.50f, 0.50f)
                .texOffs(56, 0).addBox(3.40f, 2.10f, -0.25f, 0.50f, 0.50f, 0.50f)
                .texOffs(56, 0).addBox(0.40f, 18.2f, 0.18f, 0.22f, 0.35f, 0.16f)
                .texOffs(56, 0).addBox(-0.55f, 17.8f, -0.22f, 0.20f, 0.30f, 0.14f),
                PartPose.ZERO);

        return LayerDefinition.create(mesh, 64, 64);
    }

    public void setupAnim(float ageInTicks) {
        // Core slow rotation (Eye of Ender energy)
        enderCore.yRot = ageInTicks * 0.05f;
        enderCore.xRot = (float) Math.sin(ageInTicks * 0.03f) * 0.15f;
        enderCore.zRot = (float) Math.cos(ageInTicks * 0.025f) * 0.08f;

        // Subtle crack / rune pulse via scale micro-oscillation on groups
        float pulse = 1f + (float) Math.sin(ageInTicks * 0.12f) * 0.015f;
        voidCracks.xScale = pulse;
        voidCracks.yScale = pulse;
        float runePulse = 1f + (float) Math.sin(ageInTicks * 0.15f + 1.2f) * 0.02f;
        runes.xScale = runePulse;
        runes.yScale = runePulse;
        float em = 1f + (float) Math.sin(ageInTicks * 0.1f) * 0.03f;
        emissive.xScale = em;
        emissive.yScale = em;
    }

    /** Boost crack / emissive presence during attacks (scale punch). */
    public void setCombatBoost(float boost) {
        float s = 1f + boost * 0.04f;
        voidCracks.xScale = s;
        voidCracks.yScale = s;
        voidCracks.zScale = s;
        emissive.xScale = 1f + boost * 0.06f;
        emissive.yScale = 1f + boost * 0.06f;
    }

    public void renderToBuffer(PoseStack pose, VertexConsumer vc, int light, int overlay, int color) {
        root.render(pose, vc, light, overlay, color);
    }

    public ModelPart root() {
        return root;
    }

    public ModelPart enderCore() {
        return enderCore;
    }
}

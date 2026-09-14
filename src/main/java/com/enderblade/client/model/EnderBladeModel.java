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
 * Voxel-faithful Ender Blade geometry (from Three.js preview).
 * Rendered via ModelPart (no fragile Model base class overrides).
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

    public EnderBladeModel(ModelPart root) {
        this.root = root.getChild("root");
        this.enderCore = this.root.getChild("ender_core");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition def = mesh.getRoot();
        PartDefinition root = def.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.ZERO);

        root.addOrReplaceChild("blade", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-0.3f, 17.5f, -0.15f, 0.6f, 1.2f, 0.3f)
                .texOffs(0, 0).addBox(-0.5f, 16.2f, -0.16f, 1.0f, 1.5f, 0.32f)
                .texOffs(0, 0).addBox(-0.65f, 14.5f, -0.18f, 1.3f, 1.8f, 0.36f)
                .texOffs(0, 0).addBox(-0.8f, 12.2f, -0.2f, 1.6f, 2.4f, 0.4f)
                .texOffs(0, 0).addBox(-0.9f, 9.6f, -0.2f, 1.8f, 2.6f, 0.4f)
                .texOffs(0, 0).addBox(-0.95f, 7.0f, -0.22f, 1.9f, 2.6f, 0.44f)
                .texOffs(0, 0).addBox(-1.0f, 4.6f, -0.22f, 2.0f, 2.4f, 0.44f)
                .texOffs(0, 0).addBox(-1.05f, 2.4f, -0.24f, 2.1f, 2.2f, 0.48f)
                .texOffs(0, 0).addBox(-1.1f, 0.8f, -0.25f, 2.2f, 1.6f, 0.5f)
                .texOffs(16, 0).addBox(-0.25f, 2.0f, 0.18f, 0.5f, 14.0f, 0.12f)
                .texOffs(16, 0).addBox(-0.25f, 2.0f, -0.3f, 0.5f, 14.0f, 0.12f),
                PartPose.ZERO);

        root.addOrReplaceChild("blade_edge", CubeListBuilder.create()
                .texOffs(32, 0).addBox(-1.35f, 2.0f, -0.1f, 0.25f, 15.5f, 0.2f)
                .texOffs(32, 0).addBox(1.1f, 2.0f, -0.1f, 0.25f, 15.5f, 0.2f)
                .texOffs(32, 0).addBox(-0.4f, 18.5f, -0.1f, 0.8f, 0.6f, 0.2f),
                PartPose.ZERO);

        root.addOrReplaceChild("void_cracks", CubeListBuilder.create()
                .texOffs(40, 0).addBox(-0.15f, 14.0f, 0.22f, 0.2f, 3.0f, 0.15f)
                .texOffs(40, 0).addBox(0.05f, 11.0f, 0.22f, 0.2f, 3.2f, 0.15f)
                .texOffs(40, 0).addBox(-0.2f, 7.5f, 0.22f, 0.25f, 3.5f, 0.15f)
                .texOffs(40, 0).addBox(0.1f, 4.0f, 0.22f, 0.2f, 3.0f, 0.15f)
                .texOffs(40, 0).addBox(-0.1f, 12.0f, -0.35f, 0.2f, 4.0f, 0.15f)
                .texOffs(40, 0).addBox(0.15f, 6.0f, -0.35f, 0.2f, 4.0f, 0.15f),
                PartPose.ZERO);

        root.addOrReplaceChild("guard", CubeListBuilder.create()
                .texOffs(0, 32).addBox(-3.4f, -0.5f, -0.7f, 6.8f, 1.0f, 1.4f)
                .texOffs(0, 32).addBox(-1.0f, -0.2f, -0.85f, 2.0f, 1.2f, 1.7f)
                .texOffs(0, 32).addBox(-3.6f, 0.3f, -0.5f, 1.2f, 1.5f, 1.0f)
                .texOffs(0, 32).addBox(2.4f, 0.3f, -0.5f, 1.2f, 1.5f, 1.0f)
                .texOffs(0, 32).addBox(-4.0f, 1.2f, -0.4f, 0.8f, 1.2f, 0.8f)
                .texOffs(0, 32).addBox(3.2f, 1.2f, -0.4f, 0.8f, 1.2f, 0.8f)
                .texOffs(0, 32).addBox(-1.5f, -1.1f, -1.0f, 3.0f, 0.7f, 2.0f)
                .texOffs(48, 32).addBox(-0.7f, -0.1f, 0.85f, 1.4f, 1.4f, 0.5f)
                .texOffs(48, 40).addBox(-0.4f, 0.2f, 1.2f, 0.8f, 0.8f, 0.4f)
                .texOffs(48, 48).addBox(-2.4f, 0.4f, 0.6f, 0.5f, 0.7f, 0.5f)
                .texOffs(48, 48).addBox(1.9f, 0.4f, 0.6f, 0.5f, 0.7f, 0.5f)
                .texOffs(48, 48).addBox(-0.3f, 0.9f, -1.1f, 0.6f, 0.5f, 0.5f),
                PartPose.ZERO);

        root.addOrReplaceChild("handle", CubeListBuilder.create()
                .texOffs(0, 48).addBox(-0.5f, -2.2f, -0.5f, 1.0f, 1.2f, 1.0f)
                .texOffs(0, 48).addBox(-0.45f, -3.5f, -0.45f, 0.9f, 1.2f, 0.9f)
                .texOffs(0, 48).addBox(-0.5f, -4.8f, -0.5f, 1.0f, 1.2f, 1.0f)
                .texOffs(0, 48).addBox(-0.45f, -6.1f, -0.45f, 0.9f, 1.2f, 0.9f)
                .texOffs(0, 48).addBox(-0.5f, -7.4f, -0.5f, 1.0f, 1.2f, 1.0f)
                .texOffs(16, 48).addBox(-0.7f, -1.2f, -0.7f, 1.4f, 0.6f, 1.4f)
                .texOffs(16, 48).addBox(-0.65f, -8.2f, -0.65f, 1.3f, 0.55f, 1.3f)
                .texOffs(16, 48).addBox(-0.55f, -2.7f, -0.55f, 1.1f, 0.25f, 1.1f)
                .texOffs(16, 48).addBox(-0.55f, -4.0f, -0.55f, 1.1f, 0.25f, 1.1f)
                .texOffs(16, 48).addBox(-0.55f, -5.3f, -0.55f, 1.1f, 0.25f, 1.1f)
                .texOffs(16, 48).addBox(-0.55f, -6.6f, -0.55f, 1.1f, 0.25f, 1.1f),
                PartPose.ZERO);

        root.addOrReplaceChild("runes", CubeListBuilder.create()
                .texOffs(48, 16).addBox(-0.2f, -2.5f, 0.45f, 0.4f, 0.4f, 0.15f)
                .texOffs(48, 16).addBox(-0.2f, -3.8f, 0.45f, 0.4f, 0.4f, 0.15f)
                .texOffs(48, 16).addBox(-0.2f, -5.1f, 0.45f, 0.4f, 0.4f, 0.15f)
                .texOffs(48, 16).addBox(-0.2f, -6.4f, 0.45f, 0.4f, 0.4f, 0.15f)
                .texOffs(48, 16).addBox(-0.2f, -7.5f, 0.45f, 0.4f, 0.35f, 0.15f),
                PartPose.ZERO);

        root.addOrReplaceChild("pommel", CubeListBuilder.create()
                .texOffs(32, 48).addBox(-0.8f, -9.0f, -0.8f, 1.6f, 0.8f, 1.6f)
                .texOffs(32, 48).addBox(-0.6f, -9.6f, -0.6f, 1.2f, 0.6f, 1.2f)
                .texOffs(32, 48).addBox(-0.2f, -11.0f, -0.7f, 0.4f, 1.4f, 0.25f)
                .texOffs(32, 48).addBox(-0.2f, -11.0f, 0.45f, 0.4f, 1.4f, 0.25f)
                .texOffs(32, 48).addBox(-0.7f, -11.0f, -0.2f, 0.25f, 1.4f, 0.4f)
                .texOffs(32, 48).addBox(0.45f, -11.0f, -0.2f, 0.25f, 1.4f, 0.4f)
                .texOffs(32, 48).addBox(-0.55f, -11.4f, -0.55f, 1.1f, 0.35f, 1.1f),
                PartPose.ZERO);

        root.addOrReplaceChild("ender_core", CubeListBuilder.create()
                .texOffs(48, 56).addBox(-0.55f, -0.55f, -0.55f, 1.1f, 1.1f, 1.1f)
                .texOffs(40, 56).addBox(-0.35f, -0.35f, -0.35f, 0.7f, 0.7f, 0.7f),
                PartPose.offset(0f, -10.2f, 0f));

        root.addOrReplaceChild("emissive", CubeListBuilder.create()
                .texOffs(56, 0).addBox(-0.25f, 18.2f, -0.12f, 0.5f, 0.8f, 0.24f)
                .texOffs(56, 0).addBox(-4.0f, 1.8f, -0.25f, 0.5f, 0.5f, 0.5f)
                .texOffs(56, 0).addBox(3.5f, 1.8f, -0.25f, 0.5f, 0.5f, 0.5f),
                PartPose.ZERO);

        return LayerDefinition.create(mesh, 64, 64);
    }

    public void setupAnim(float ageInTicks) {
        enderCore.yRot = ageInTicks * 0.04f;
        enderCore.xRot = ageInTicks * 0.02f;
    }

    public void renderToBuffer(PoseStack pose, VertexConsumer vc, int light, int overlay, int color) {
        root.render(pose, vc, light, overlay, color);
    }

    public ModelPart root() {
        return root;
    }
}

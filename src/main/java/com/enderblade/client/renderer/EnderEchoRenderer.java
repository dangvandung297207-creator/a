package com.enderblade.client.renderer;

import com.enderblade.EnderBladeMod;
import com.enderblade.entity.EnderEchoProjectile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

/**
 * Custom void-orb projectile: dark shell, violet core, rotating rings.
 */
public class EnderEchoRenderer extends EntityRenderer<EnderEchoProjectile> {

    private static final ResourceLocation TEX =
            ResourceLocation.fromNamespaceAndPath(EnderBladeMod.MOD_ID, "textures/entity/ender_echo.png");

    public EnderEchoRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(EnderEchoProjectile entity, float yaw, float pt, PoseStack pose,
                       MultiBufferSource buffers, int light) {
        pose.pushPose();
        float age = entity.getAge() + pt;
        float pulse = 0.85f + 0.15f * Mth.sin(age * 0.4f);
        pose.scale(pulse, pulse, pulse);

        // Outer dark shell
        renderSphere(pose, buffers, 0.22f, 0xFF1A0830, light, false);
        // Void center
        renderSphere(pose, buffers, 0.10f, 0xFF050208, light, false);
        // Glowing core
        renderSphere(pose, buffers, 0.12f, 0xEEC070FF, 0xF000F0, true);

        // Rotating rings
        pose.mulPose(Axis.YP.rotation(age * 0.12f));
        renderRing(pose, buffers, 0.30f, 0.02f, 0xAAB45CFF);
        pose.mulPose(Axis.XP.rotation(age * 0.09f + 1.0f));
        renderRing(pose, buffers, 0.26f, 0.015f, 0x886B2AD1);

        pose.popPose();
        super.render(entity, yaw, pt, pose, buffers, light);
    }

    private void renderSphere(PoseStack pose, MultiBufferSource buffers, float r, int argb, int light, boolean additive) {
        RenderType type = additive
                ? RenderType.eyes(TEX)
                : RenderType.entityTranslucent(TEX);
        VertexConsumer vc = buffers.getBuffer(type);
        Matrix4f mat = pose.last().pose();
        // Low-poly icosphere approximation via billboarded quads stacked
        int segs = 8;
        for (int i = 0; i < segs; i++) {
            float a0 = (float) (i * Math.PI * 2 / segs);
            float a1 = (float) ((i + 1) * Math.PI * 2 / segs);
            for (int j = 0; j < segs / 2; j++) {
                float b0 = (float) (j * Math.PI / (segs / 2) - Math.PI / 2);
                float b1 = (float) ((j + 1) * Math.PI / (segs / 2) - Math.PI / 2);
                vert(vc, mat, sph(r, a0, b0), argb, light);
                vert(vc, mat, sph(r, a1, b0), argb, light);
                vert(vc, mat, sph(r, a1, b1), argb, light);
                vert(vc, mat, sph(r, a0, b1), argb, light);
            }
        }
    }

    private static float[] sph(float r, float a, float b) {
        return new float[]{
                r * Mth.cos(b) * Mth.cos(a),
                r * Mth.sin(b),
                r * Mth.cos(b) * Mth.sin(a)
        };
    }

    private void renderRing(PoseStack pose, MultiBufferSource buffers, float radius, float thick, int argb) {
        VertexConsumer vc = buffers.getBuffer(RenderType.eyes(TEX));
        Matrix4f mat = pose.last().pose();
        int segs = 16;
        for (int i = 0; i < segs; i++) {
            float a0 = (float) (i * Math.PI * 2 / segs);
            float a1 = (float) ((i + 1) * Math.PI * 2 / segs);
            float x0 = Mth.cos(a0) * radius, z0 = Mth.sin(a0) * radius;
            float x1 = Mth.cos(a1) * radius, z1 = Mth.sin(a1) * radius;
            vert(vc, mat, new float[]{x0, -thick, z0}, argb, 0xF000F0);
            vert(vc, mat, new float[]{x1, -thick, z1}, argb, 0xF000F0);
            vert(vc, mat, new float[]{x1, thick, z1}, argb, 0xF000F0);
            vert(vc, mat, new float[]{x0, thick, z0}, argb, 0xF000F0);
        }
    }

    private static void vert(VertexConsumer vc, Matrix4f mat, float[] p, int argb, int light) {
        int a = (argb >> 24) & 255, r = (argb >> 16) & 255, g = (argb >> 8) & 255, b = argb & 255;
        vc.addVertex(mat, p[0], p[1], p[2])
                .setColor(r, g, b, a)
                .setUv(0.5f, 0.5f)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(0, 1, 0);
    }

    @Override
    public ResourceLocation getTextureLocation(EnderEchoProjectile entity) {
        return TEX;
    }
}

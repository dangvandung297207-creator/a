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
 * Ender Echo — dark purple core, bright violet inner energy, rotating rings,
 * geometric fragments, short trailing ribbon. Not a simple glowing sphere.
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
        float pulse = 0.9f + 0.1f * Mth.sin(age * 0.35f);

        // Thin spatial trail ribbon behind velocity
        renderTrail(pose, buffers, entity, age);

        pose.scale(pulse, pulse, pulse);

        // Dark purple outer shell
        renderSphere(pose, buffers, 0.20f, 0xCC120820, light, false);
        // Black void core
        renderSphere(pose, buffers, 0.09f, 0xFF020006, light, false);
        // Bright violet inner energy
        renderSphere(pose, buffers, 0.11f, 0xE8E8B0FF, 0xF000F0, true);

        // Three rotating rings on different axes
        pose.pushPose();
        pose.mulPose(Axis.YP.rotation(age * 0.15f));
        renderRing(pose, buffers, 0.28f, 0.018f, 0xB0C070FF);
        pose.mulPose(Axis.XP.rotation(age * 0.11f + 0.8f));
        renderRing(pose, buffers, 0.24f, 0.014f, 0xA0B45CFF);
        pose.mulPose(Axis.ZP.rotation(age * 0.09f + 1.4f));
        renderRing(pose, buffers, 0.32f, 0.012f, 0x886B2AD1);
        pose.popPose();

        // Small geometric fragments orbiting
        for (int i = 0; i < 6; i++) {
            float a = age * 0.2f + i * ((float) Math.PI * 2f / 6f);
            float r = 0.22f + (i % 2) * 0.04f;
            float x = Mth.cos(a) * r;
            float y = Mth.sin(a * 1.3f) * 0.06f;
            float z = Mth.sin(a) * r;
            pose.pushPose();
            pose.translate(x, y, z);
            pose.mulPose(Axis.YP.rotation(a));
            renderBox(pose, buffers, 0.035f, 0xC0A050FF);
            pose.popPose();
        }

        pose.popPose();
        super.render(entity, yaw, pt, pose, buffers, light);
    }

    private void renderTrail(PoseStack pose, MultiBufferSource buffers, EnderEchoProjectile entity, float age) {
        var vel = entity.getDeltaMovement();
        if (vel.lengthSqr() < 1.0e-4) return;
        pose.pushPose();
        // Orient a thin ribbon opposite velocity
        float yaw = (float) Math.atan2(vel.x, vel.z);
        float pitch = (float) Math.atan2(vel.y, Math.sqrt(vel.x * vel.x + vel.z * vel.z));
        pose.mulPose(Axis.YP.rotation(yaw));
        pose.mulPose(Axis.XP.rotation(-pitch));
        VertexConsumer vc = buffers.getBuffer(RenderType.eyes(TEX));
        Matrix4f m = pose.last().pose();
        // fading ribbon quads behind
        for (int i = 0; i < 4; i++) {
            float z0 = -0.15f - i * 0.18f;
            float z1 = z0 - 0.18f;
            float w = 0.06f * (1f - i * 0.18f);
            int a = 140 - i * 28;
            int argb = (a << 24) | 0xB45CFF;
            v(vc, m, -w, 0, z0, argb);
            v(vc, m, w, 0, z0, argb);
            v(vc, m, w * 0.7f, 0, z1, argb);
            v(vc, m, -w * 0.7f, 0, z1, argb);
        }
        pose.popPose();
    }

    private void renderBox(PoseStack pose, MultiBufferSource buffers, float s, int argb) {
        VertexConsumer vc = buffers.getBuffer(RenderType.eyes(TEX));
        Matrix4f m = pose.last().pose();
        float h = s * 0.5f;
        v(vc, m, -h, -h, h, argb);
        v(vc, m, h, -h, h, argb);
        v(vc, m, h, h, h, argb);
        v(vc, m, -h, h, h, argb);
    }

    private void renderSphere(PoseStack pose, MultiBufferSource buffers, float r, int argb, int light, boolean additive) {
        RenderType type = additive ? RenderType.eyes(TEX) : RenderType.entityTranslucent(TEX);
        VertexConsumer vc = buffers.getBuffer(type);
        Matrix4f mat = pose.last().pose();
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
        int segs = 20;
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

    private static void v(VertexConsumer vc, Matrix4f m, float x, float y, float z, int argb) {
        int a = (argb >>> 24) & 255, r = (argb >>> 16) & 255, g = (argb >>> 8) & 255, b = argb & 255;
        vc.addVertex(m, x, y, z).setColor(r, g, b, a).setUv(0.5f, 0.5f)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0, 1, 0);
    }

    @Override
    public ResourceLocation getTextureLocation(EnderEchoProjectile entity) {
        return TEX;
    }
}

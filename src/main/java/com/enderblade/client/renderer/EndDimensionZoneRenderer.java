package com.enderblade.client.renderer;

import com.enderblade.EnderBladeMod;
import com.enderblade.entity.EndDimensionZoneEntity;
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
 * End Dimension ultimate domain — clear circular zone with rotating rings,
 * floating End fragments, void cracks, restrained atmosphere. Readable, not particle spam.
 */
public class EndDimensionZoneRenderer extends EntityRenderer<EndDimensionZoneEntity> {

    private static final ResourceLocation TEX =
            ResourceLocation.fromNamespaceAndPath(EnderBladeMod.MOD_ID, "textures/entity/end_zone.png");

    public EndDimensionZoneRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(EndDimensionZoneEntity entity, float yaw, float pt, PoseStack pose,
                       MultiBufferSource buffers, int light) {
        float progress = entity.getProgress();
        float expand = progress < 0.1f ? progress / 0.1f
                : progress > 0.82f ? Math.max(0f, 1f - (progress - 0.82f) / 0.18f)
                : 1f;
        expand = Mth.clamp(expand, 0f, 1f);
        float radius = (float) EndDimensionZoneEntity.RADIUS * expand;
        float t = entity.getZoneAge() + pt;
        boolean collapsing = progress > 0.82f;

        pose.pushPose();

        // Ground ring set
        pose.pushPose();
        pose.mulPose(Axis.XP.rotationDegrees(90));
        pose.mulPose(Axis.ZP.rotation(t * 0.025f));
        renderRing(pose, buffers, radius, 0.14f, 0xC0B45CFF);
        pose.mulPose(Axis.ZP.rotation(-t * 0.04f));
        renderRing(pose, buffers, radius * 0.72f, 0.09f, 0xA06B2AD1);
        pose.mulPose(Axis.ZP.rotation(t * 0.06f));
        renderRing(pose, buffers, radius * 0.4f, 0.06f, 0x88C070FF);
        // Ground crack spokes
        for (int i = 0; i < 6; i++) {
            float a = i * (Mth.PI / 3f) + t * 0.01f;
            renderSpoke(pose, buffers, radius * 0.85f, a, 0x90A050FF);
        }
        pose.popPose();

        // Floating End fragments (geometry, not particles)
        int frags = 10;
        for (int i = 0; i < frags; i++) {
            float ang = t * 0.03f + i * (Mth.TWO_PI / frags);
            float rad = radius * (0.35f + (i % 3) * 0.18f);
            if (collapsing) rad *= expand;
            float x = Mth.cos(ang) * rad;
            float z = Mth.sin(ang) * rad;
            float y = 0.4f + Mth.sin(t * 0.08f + i) * 0.35f + (i % 4) * 0.15f;
            pose.pushPose();
            pose.translate(x, y, z);
            pose.mulPose(Axis.YP.rotation(ang + t * 0.1f));
            pose.mulPose(Axis.XP.rotation(t * 0.05f + i));
            float s = 0.12f + (i % 3) * 0.04f;
            renderBox(pose, buffers, s, 0xB08040C0);
            pose.popPose();
        }

        // Orbiting Eye symbols
        for (int i = 0; i < 4; i++) {
            float ang = t * 0.035f + i * (Mth.PI * 0.5f);
            float x = Mth.cos(ang) * radius * 0.55f;
            float z = Mth.sin(ang) * radius * 0.55f;
            pose.pushPose();
            pose.translate(x, 1.15f + Mth.sin(t * 0.1f + i) * 0.25f, z);
            pose.mulPose(this.entityRenderDispatcher.cameraOrientation());
            renderEye(pose, buffers, 0.32f, 0xEEC070FF);
            pose.popPose();
        }

        // Rising energy columns (restrained)
        for (int i = 0; i < 4; i++) {
            float ang = i * (Mth.PI * 0.5f) + 0.4f;
            float x = Mth.cos(ang) * radius * 0.85f;
            float z = Mth.sin(ang) * radius * 0.85f;
            pose.pushPose();
            pose.translate(x, 0, z);
            float h = (1.8f + Mth.sin(t * 0.12f + i) * 0.3f) * expand;
            renderPillar(pose, buffers, 0.04f, h, 0x55B45CFF);
            pose.popPose();
        }

        // Central vertical rift
        pose.pushPose();
        float pillarH = 3.2f * expand * (collapsing ? expand : 1f);
        renderPillar(pose, buffers, 0.1f * expand, pillarH, 0x66A050FF);
        // dark core
        renderPillar(pose, buffers, 0.04f * expand, pillarH * 0.95f, 0xAA05020A);
        pose.popPose();

        // Occasional spatial flash ring (every ~1s visual pulse via sin)
        float flash = Math.max(0f, Mth.sin(t * 0.2f));
        if (flash > 0.92f && !collapsing) {
            pose.pushPose();
            pose.mulPose(Axis.XP.rotationDegrees(90));
            renderRing(pose, buffers, radius * 0.5f * flash, 0.05f, 0x60E0A0FF);
            pose.popPose();
        }

        pose.popPose();
        super.render(entity, yaw, pt, pose, buffers, light);
    }

    private void renderSpoke(PoseStack pose, MultiBufferSource buf, float len, float ang, int argb) {
        VertexConsumer vc = buf.getBuffer(RenderType.eyes(TEX));
        Matrix4f m = pose.last().pose();
        float c = Mth.cos(ang), s = Mth.sin(ang);
        float t = 0.04f;
        v(vc, m, c * 0.3f - s * t, s * 0.3f + c * t, 0.02f, argb);
        v(vc, m, c * len - s * t, s * len + c * t, 0.02f, argb);
        v(vc, m, c * len + s * t, s * len - c * t, 0.02f, argb);
        v(vc, m, c * 0.3f + s * t, s * 0.3f - c * t, 0.02f, argb);
    }

    private void renderBox(PoseStack pose, MultiBufferSource buf, float s, int argb) {
        VertexConsumer vc = buf.getBuffer(RenderType.eyes(TEX));
        Matrix4f m = pose.last().pose();
        float h = s * 0.5f;
        v(vc, m, -h, -h * 0.6f, h, argb);
        v(vc, m, h, -h * 0.6f, h, argb);
        v(vc, m, h, h * 0.6f, h, argb);
        v(vc, m, -h, h * 0.6f, h, argb);
    }

    private void renderRing(PoseStack pose, MultiBufferSource buf, float r, float th, int argb) {
        VertexConsumer vc = buf.getBuffer(RenderType.eyes(TEX));
        Matrix4f m = pose.last().pose();
        int segs = 36;
        for (int i = 0; i < segs; i++) {
            float a0 = (float) (i * Math.PI * 2 / segs);
            float a1 = (float) ((i + 1) * Math.PI * 2 / segs);
            float x0 = Mth.cos(a0) * r, y0 = Mth.sin(a0) * r;
            float x1 = Mth.cos(a1) * r, y1 = Mth.sin(a1) * r;
            v(vc, m, x0, y0, -th, argb);
            v(vc, m, x1, y1, -th, argb);
            v(vc, m, x1, y1, th, argb);
            v(vc, m, x0, y0, th, argb);
        }
    }

    private void renderEye(PoseStack pose, MultiBufferSource buf, float s, int argb) {
        VertexConsumer vc = buf.getBuffer(RenderType.eyes(TEX));
        Matrix4f m = pose.last().pose();
        v(vc, m, -s, -s * 0.5f, 0, argb);
        v(vc, m, s, -s * 0.5f, 0, argb);
        v(vc, m, s, s * 0.5f, 0, argb);
        v(vc, m, -s, s * 0.5f, 0, argb);
        float p = s * 0.35f;
        v(vc, m, -p, -p, 0.01f, 0xFF0A0418);
        v(vc, m, p, -p, 0.01f, 0xFF0A0418);
        v(vc, m, p, p, 0.01f, 0xFF0A0418);
        v(vc, m, -p, p, 0.01f, 0xFF0A0418);
    }

    private void renderPillar(PoseStack pose, MultiBufferSource buf, float w, float h, int argb) {
        VertexConsumer vc = buf.getBuffer(RenderType.eyes(TEX));
        Matrix4f m = pose.last().pose();
        v(vc, m, -w, 0, 0, argb);
        v(vc, m, w, 0, 0, argb);
        v(vc, m, w, h, 0, argb);
        v(vc, m, -w, h, 0, argb);
    }

    private static void v(VertexConsumer vc, Matrix4f m, float x, float y, float z, int argb) {
        int a = (argb >>> 24) & 255, r = (argb >>> 16) & 255, g = (argb >>> 8) & 255, b = argb & 255;
        vc.addVertex(m, x, y, z).setColor(r, g, b, a).setUv(0.5f, 0.5f)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0, 1, 0);
    }

    @Override
    public ResourceLocation getTextureLocation(EndDimensionZoneEntity entity) {
        return TEX;
    }
}

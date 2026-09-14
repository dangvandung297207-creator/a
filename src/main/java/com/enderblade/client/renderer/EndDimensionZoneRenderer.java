package com.enderblade.client.renderer;

import com.enderblade.EnderBladeMod;
import com.enderblade.ability.AbilityHelper;
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
                : progress > 0.85f ? 1f - (progress - 0.85f) / 0.15f
                : 1f;
        expand = Mth.clamp(expand, 0f, 1f);
        float radius = (float) EndDimensionZoneEntity.RADIUS * expand;

        pose.pushPose();
        float t = entity.getZoneAge() + pt;

        // Large rotating void ring on ground
        pose.pushPose();
        pose.mulPose(Axis.XP.rotationDegrees(90));
        pose.mulPose(Axis.ZP.rotation(t * 0.03f));
        renderRing(pose, buffers, radius, 0.15f, 0xAAB45CFF);
        pose.mulPose(Axis.ZP.rotation(-t * 0.05f));
        renderRing(pose, buffers, radius * 0.7f, 0.1f, 0x886B2AD1);
        pose.popPose();

        // Eye-shaped symbols orbiting
        for (int i = 0; i < 4; i++) {
            float ang = t * 0.04f + i * (float) (Math.PI * 0.5);
            float x = Mth.cos(ang) * radius * 0.55f;
            float z = Mth.sin(ang) * radius * 0.55f;
            pose.pushPose();
            pose.translate(x, 1.2f + Mth.sin(t * 0.1f + i) * 0.3f, z);
            pose.mulPose(this.entityRenderDispatcher.cameraOrientation());
            renderEye(pose, buffers, 0.35f, 0xEEC070FF);
            pose.popPose();
        }

        // Central vertical rift
        pose.pushPose();
        renderPillar(pose, buffers, 0.12f * expand, 3.5f * expand, 0x55A050FF);
        pose.popPose();

        pose.popPose();
        super.render(entity, yaw, pt, pose, buffers, light);
    }

    private void renderRing(PoseStack pose, MultiBufferSource buf, float r, float th, int argb) {
        VertexConsumer vc = buf.getBuffer(RenderType.eyes(TEX));
        Matrix4f m = pose.last().pose();
        int segs = 32;
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
        // pupil
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

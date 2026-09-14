package com.enderblade.client.renderer;

import com.enderblade.EnderBladeMod;
import com.enderblade.entity.VoidAnchorEntity;
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

public class VoidAnchorRenderer extends EntityRenderer<VoidAnchorEntity> {

    private static final ResourceLocation TEX =
            ResourceLocation.fromNamespaceAndPath(EnderBladeMod.MOD_ID, "textures/entity/void_anchor.png");

    public VoidAnchorRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(VoidAnchorEntity entity, float yaw, float pt, PoseStack pose,
                       MultiBufferSource buffers, int light) {
        pose.pushPose();
        float t = entity.tickCount + pt;
        pose.translate(0, 0.4 + Mth.sin(t * 0.08f) * 0.05f, 0);

        // Core
        renderBillboardQuad(pose, buffers, 0.35f, 0xFFC070FF, true);
        renderBillboardQuad(pose, buffers, 0.18f, 0xFF0A0418, false);

        // Rotating ring
        pose.pushPose();
        pose.mulPose(Axis.YP.rotation(t * 0.08f));
        pose.mulPose(Axis.XP.rotationDegrees(70));
        renderRing(pose, buffers, 0.45f, 0.03f, 0xAAB45CFF);
        pose.popPose();

        // Vertical distortion pillar
        pose.pushPose();
        renderPillar(pose, buffers, 0.06f, 1.4f, 0x66A050FF);
        pose.popPose();

        pose.popPose();
        super.render(entity, yaw, pt, pose, buffers, light);
    }

    private void renderBillboardQuad(PoseStack pose, MultiBufferSource buf, float s, int argb, boolean eyes) {
        VertexConsumer vc = buf.getBuffer(eyes ? RenderType.eyes(TEX) : RenderType.entityTranslucent(TEX));
        pose.pushPose();
        pose.mulPose(this.entityRenderDispatcher.cameraOrientation());
        Matrix4f m = pose.last().pose();
        int light = eyes ? 0xF000F0 : 0x00F000F0;
        quad(vc, m, -s, -s, s, s, argb, light);
        pose.popPose();
    }

    private void renderRing(PoseStack pose, MultiBufferSource buf, float r, float th, int argb) {
        VertexConsumer vc = buf.getBuffer(RenderType.eyes(TEX));
        Matrix4f m = pose.last().pose();
        int segs = 20;
        for (int i = 0; i < segs; i++) {
            float a0 = (float) (i * Math.PI * 2 / segs);
            float a1 = (float) ((i + 1) * Math.PI * 2 / segs);
            float x0 = Mth.cos(a0) * r, z0 = Mth.sin(a0) * r;
            float x1 = Mth.cos(a1) * r, z1 = Mth.sin(a1) * r;
            v(vc, m, x0, -th, z0, argb);
            v(vc, m, x1, -th, z1, argb);
            v(vc, m, x1, th, z1, argb);
            v(vc, m, x0, th, z0, argb);
        }
    }

    private void renderPillar(PoseStack pose, MultiBufferSource buf, float w, float h, int argb) {
        VertexConsumer vc = buf.getBuffer(RenderType.eyes(TEX));
        Matrix4f m = pose.last().pose();
        v(vc, m, -w, 0, -w, argb);
        v(vc, m, w, 0, -w, argb);
        v(vc, m, w, h, -w, argb);
        v(vc, m, -w, h, -w, argb);
    }

    private static void quad(VertexConsumer vc, Matrix4f m, float x0, float y0, float x1, float y1, int argb, int light) {
        v(vc, m, x0, y0, 0, argb, light);
        v(vc, m, x1, y0, 0, argb, light);
        v(vc, m, x1, y1, 0, argb, light);
        v(vc, m, x0, y1, 0, argb, light);
    }

    private static void v(VertexConsumer vc, Matrix4f m, float x, float y, float z, int argb) {
        v(vc, m, x, y, z, argb, 0xF000F0);
    }

    private static void v(VertexConsumer vc, Matrix4f m, float x, float y, float z, int argb, int light) {
        int a = (argb >>> 24) & 255, r = (argb >>> 16) & 255, g = (argb >>> 8) & 255, b = argb & 255;
        vc.addVertex(m, x, y, z).setColor(r, g, b, a).setUv(0.5f, 0.5f)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 1, 0);
    }

    @Override
    public ResourceLocation getTextureLocation(VoidAnchorEntity entity) {
        return TEX;
    }
}

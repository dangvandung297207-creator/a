package com.enderblade.client.renderer;

import com.enderblade.EnderBladeMod;
import com.enderblade.entity.VoidSlashEntity;
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
 * Dimensional cut — thin purple-black rift plane with fractured emissive edges.
 */
public class VoidSlashRenderer extends EntityRenderer<VoidSlashEntity> {

    private static final ResourceLocation TEX =
            ResourceLocation.fromNamespaceAndPath(EnderBladeMod.MOD_ID, "textures/entity/void_slash.png");

    public VoidSlashRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(VoidSlashEntity entity, float yaw, float pt, PoseStack pose,
                       MultiBufferSource buffers, int light) {
        float progress = entity.getLifeProgress();
        // Open fast, hold, snap shut
        float open = progress < 0.2f ? progress / 0.2f
                : progress > 0.7f ? 1f - (progress - 0.7f) / 0.3f
                : 1f;
        open = Mth.clamp(open, 0f, 1f);
        if (open < 0.01f) return;

        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(-entity.getSlashYaw()));

        float w = 2.2f * open;
        float h = 2.0f * open;
        float depth = 0.08f;

        // Dark void interior
        renderPlane(pose, buffers, w, h, depth, 0xEE05020A, false);
        // Emissive edge frame
        renderFrame(pose, buffers, w, h, 0xFFC070FF);

        // Fractured edge shards
        VertexConsumer vc = buffers.getBuffer(RenderType.eyes(TEX));
        Matrix4f m = pose.last().pose();
        for (int i = 0; i < 6; i++) {
            float ox = (i / 5f - 0.5f) * w * 1.1f;
            float oy = ((i % 3) - 1) * h * 0.35f;
            float s = 0.12f + (i % 2) * 0.08f;
            v(vc, m, ox - s, oy, 0.05f, 0xAAB45CFF);
            v(vc, m, ox + s, oy, 0.05f, 0xAAB45CFF);
            v(vc, m, ox, oy + s * 1.4f, 0.05f, 0xAAB45CFF);
            v(vc, m, ox, oy - s * 0.5f, 0.05f, 0xAAB45CFF);
        }

        pose.popPose();
        super.render(entity, yaw, pt, pose, buffers, light);
    }

    private void renderPlane(PoseStack pose, MultiBufferSource buf, float w, float h, float d, int argb, boolean eyes) {
        VertexConsumer vc = buf.getBuffer(eyes ? RenderType.eyes(TEX) : RenderType.entityTranslucent(TEX));
        Matrix4f m = pose.last().pose();
        // front
        v(vc, m, -w / 2, -h / 2, d, argb);
        v(vc, m, w / 2, -h / 2, d, argb);
        v(vc, m, w / 2, h / 2, d, argb);
        v(vc, m, -w / 2, h / 2, d, argb);
        // back
        v(vc, m, -w / 2, h / 2, -d, argb);
        v(vc, m, w / 2, h / 2, -d, argb);
        v(vc, m, w / 2, -h / 2, -d, argb);
        v(vc, m, -w / 2, -h / 2, -d, argb);
    }

    private void renderFrame(PoseStack pose, MultiBufferSource buf, float w, float h, int argb) {
        VertexConsumer vc = buf.getBuffer(RenderType.eyes(TEX));
        Matrix4f m = pose.last().pose();
        float t = 0.06f;
        // top
        v(vc, m, -w / 2, h / 2 - t, 0.09f, argb);
        v(vc, m, w / 2, h / 2 - t, 0.09f, argb);
        v(vc, m, w / 2, h / 2 + t, 0.09f, argb);
        v(vc, m, -w / 2, h / 2 + t, 0.09f, argb);
        // bottom
        v(vc, m, -w / 2, -h / 2 - t, 0.09f, argb);
        v(vc, m, w / 2, -h / 2 - t, 0.09f, argb);
        v(vc, m, w / 2, -h / 2 + t, 0.09f, argb);
        v(vc, m, -w / 2, -h / 2 + t, 0.09f, argb);
        // left
        v(vc, m, -w / 2 - t, -h / 2, 0.09f, argb);
        v(vc, m, -w / 2 + t, -h / 2, 0.09f, argb);
        v(vc, m, -w / 2 + t, h / 2, 0.09f, argb);
        v(vc, m, -w / 2 - t, h / 2, 0.09f, argb);
        // right
        v(vc, m, w / 2 - t, -h / 2, 0.09f, argb);
        v(vc, m, w / 2 + t, -h / 2, 0.09f, argb);
        v(vc, m, w / 2 + t, h / 2, 0.09f, argb);
        v(vc, m, w / 2 - t, h / 2, 0.09f, argb);
    }

    private static void v(VertexConsumer vc, Matrix4f m, float x, float y, float z, int argb) {
        int a = (argb >>> 24) & 255, r = (argb >>> 16) & 255, g = (argb >>> 8) & 255, b = argb & 255;
        vc.addVertex(m, x, y, z).setColor(r, g, b, a).setUv(0.5f, 0.5f)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0, 0, 1);
    }

    @Override
    public ResourceLocation getTextureLocation(VoidSlashEntity entity) {
        return TEX;
    }
}

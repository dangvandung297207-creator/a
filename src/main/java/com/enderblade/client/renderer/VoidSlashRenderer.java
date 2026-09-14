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
 * Literal cut in space — black/purple interior, sharp fractured edges,
 * glowing violet rim, animated distortion. Open → strike → fracture → collapse.
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
        // Open extremely fast, brief hold, snap shut
        float open = progress < 0.15f ? progress / 0.15f
                : progress > 0.55f ? Math.max(0f, 1f - (progress - 0.55f) / 0.45f)
                : 1f;
        open = Mth.clamp(open, 0f, 1f);
        if (open < 0.01f) return;

        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(-entity.getSlashYaw()));

        // Distortion jitter
        float jitter = Mth.sin((entity.getAge() + pt) * 2.8f) * 0.03f * open;
        pose.translate(jitter, 0, 0);

        float w = 2.4f * open;
        float h = 1.7f * open;
        float depth = 0.07f;

        // Black/purple interior
        renderPlane(pose, buffers, w, h, depth, 0xF005020A, false);
        // Slightly smaller hot purple sheet
        renderPlane(pose, buffers, w * 0.92f, h * 0.88f, depth + 0.01f, 0x551A0840, true);

        // Glowing violet edge frame
        renderFrame(pose, buffers, w, h, 0xFFC070FF);
        // Hot outer rim
        renderFrame(pose, buffers, w * 1.04f, h * 1.04f, 0xAAFF6AE0);

        // Fractured edge shards + trailing fragments
        VertexConsumer vc = buffers.getBuffer(RenderType.eyes(TEX));
        Matrix4f m = pose.last().pose();
        float age = entity.getAge() + pt;
        for (int i = 0; i < 8; i++) {
            float ox = (i / 7f - 0.5f) * w * 1.15f;
            float oy = Mth.sin(i * 1.7f + age) * h * 0.4f;
            float s = 0.10f + (i % 3) * 0.05f;
            int col = (i % 2 == 0) ? 0xC0B45CFF : 0xA0FF6AE0;
            v(vc, m, ox - s, oy, 0.06f, col);
            v(vc, m, ox + s, oy, 0.06f, col);
            v(vc, m, ox + s * 0.3f, oy + s * 1.3f, 0.06f, col);
            v(vc, m, ox - s * 0.3f, oy - s * 0.6f, 0.06f, col);
        }

        // Thin trailing fragments drifting forward
        for (int i = 0; i < 5; i++) {
            float fx = (i - 2) * 0.25f * open;
            float fy = Mth.sin(age * 3f + i) * 0.3f;
            float fz = 0.15f + i * 0.04f;
            float s = 0.06f;
            v(vc, m, fx - s, fy, fz, 0x90A050FF);
            v(vc, m, fx + s, fy, fz, 0x90A050FF);
            v(vc, m, fx + s, fy + s, fz, 0x90A050FF);
            v(vc, m, fx - s, fy + s, fz, 0x90A050FF);
        }

        pose.popPose();
        super.render(entity, yaw, pt, pose, buffers, light);
    }

    private void renderPlane(PoseStack pose, MultiBufferSource buf, float w, float h, float d, int argb, boolean eyes) {
        VertexConsumer vc = buf.getBuffer(eyes ? RenderType.eyes(TEX) : RenderType.entityTranslucent(TEX));
        Matrix4f m = pose.last().pose();
        v(vc, m, -w / 2, -h / 2, d, argb);
        v(vc, m, w / 2, -h / 2, d, argb);
        v(vc, m, w / 2, h / 2, d, argb);
        v(vc, m, -w / 2, h / 2, d, argb);
        v(vc, m, -w / 2, h / 2, -d, argb);
        v(vc, m, w / 2, h / 2, -d, argb);
        v(vc, m, w / 2, -h / 2, -d, argb);
        v(vc, m, -w / 2, -h / 2, -d, argb);
    }

    private void renderFrame(PoseStack pose, MultiBufferSource buf, float w, float h, int argb) {
        VertexConsumer vc = buf.getBuffer(RenderType.eyes(TEX));
        Matrix4f m = pose.last().pose();
        float t = 0.05f;
        // top / bottom / left / right
        v(vc, m, -w / 2, h / 2 - t, 0.09f, argb);
        v(vc, m, w / 2, h / 2 - t, 0.09f, argb);
        v(vc, m, w / 2, h / 2 + t, 0.09f, argb);
        v(vc, m, -w / 2, h / 2 + t, 0.09f, argb);

        v(vc, m, -w / 2, -h / 2 - t, 0.09f, argb);
        v(vc, m, w / 2, -h / 2 - t, 0.09f, argb);
        v(vc, m, w / 2, -h / 2 + t, 0.09f, argb);
        v(vc, m, -w / 2, -h / 2 + t, 0.09f, argb);

        v(vc, m, -w / 2 - t, -h / 2, 0.09f, argb);
        v(vc, m, -w / 2 + t, -h / 2, 0.09f, argb);
        v(vc, m, -w / 2 + t, h / 2, 0.09f, argb);
        v(vc, m, -w / 2 - t, h / 2, 0.09f, argb);

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

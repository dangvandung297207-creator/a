package com.enderblade.client.vfx;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import com.enderblade.EnderBladeMod;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Client-side one-shot mesh VFX (rifts, bursts, afterimages, collapse).
 * Prefer geometry + emissive over particle spam.
 */
public final class VfxManager {

    private static final ResourceLocation TEX =
            ResourceLocation.fromNamespaceAndPath(EnderBladeMod.MOD_ID, "textures/entity/vfx_atlas.png");

    private static final List<Vfx> ACTIVE = new ArrayList<>();

    private VfxManager() {
    }

    public static void spawn(String type, double x, double y, double z, float scale) {
        ACTIVE.add(new Vfx(type, x, y, z, scale));
    }

    public static void tick() {
        Iterator<Vfx> it = ACTIVE.iterator();
        float dt = 0.05f;
        while (it.hasNext()) {
            Vfx v = it.next();
            v.age += dt;
            if (v.age >= v.lifetime) it.remove();
        }
    }

    public static void render(PoseStack pose, MultiBufferSource buffers, float pt) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.gameRenderer == null) return;
        Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();

        for (Vfx v : ACTIVE) {
            float t = (v.age + pt * 0.05f) / v.lifetime;
            t = Mth.clamp(t, 0f, 1f);
            pose.pushPose();
            pose.translate(v.x - cam.x, v.y - cam.y, v.z - cam.z);
            renderOne(v, t, pose, buffers);
            pose.popPose();
        }
    }

    private static void renderOne(Vfx v, float t, PoseStack pose, MultiBufferSource buffers) {
        float fade = t < 0.2f ? t / 0.2f : 1f - (t - 0.2f) / 0.8f;
        fade = Mth.clamp(fade, 0f, 1f);
        int alpha = (int) (fade * 220);
        float s = v.scale;

        switch (v.type) {
            case "spatial_burst", "spatial_cut" -> {
                float expand = 0.3f + t * 1.2f;
                ring(pose, buffers, expand * s, 0.04f, withAlpha(0xB45CFF, alpha));
                ring(pose, buffers, expand * 0.6f * s, 0.03f, withAlpha(0x6B2AD1, alpha));
            }
            case "afterimage" -> {
                billboard(pose, buffers, 0.5f * s * (1f - t * 0.5f), withAlpha(0x8A40D0, alpha / 2));
            }
            case "teleport_arrival", "echo_launch" -> {
                ring(pose, buffers, (0.4f + t) * s, 0.05f, withAlpha(0xC070FF, alpha));
                billboard(pose, buffers, 0.3f * s, withAlpha(0xE0A0FF, alpha));
            }
            case "anchor_place", "anchor_recall" -> {
                pose.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90));
                ring(pose, buffers, 0.6f * s, 0.05f, withAlpha(0xA050FF, alpha));
            }
            case "void_slash" -> {
                float open = t < 0.3f ? t / 0.3f : 1f - (t - 0.3f) / 0.7f;
                plane(pose, buffers, 2.0f * open * s, 1.6f * open * s, withAlpha(0x05020A, alpha), withAlpha(0xC070FF, alpha));
            }
            case "rift_collapse", "ultimate_collapse" -> {
                float shrink = 1.5f * s * (1f - t);
                ring(pose, buffers, shrink, 0.08f, withAlpha(0xFF4FD8, alpha));
                ring(pose, buffers, shrink * 0.5f, 0.06f, withAlpha(0xB45CFF, alpha));
                billboard(pose, buffers, 0.2f * s * (1f - t), withAlpha(0xFFFFFF, alpha));
            }
            case "ultimate_start" -> {
                float expand = t * 3f * s;
                pose.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90));
                ring(pose, buffers, expand, 0.12f, withAlpha(0xB45CFF, alpha));
            }
            default -> ring(pose, buffers, 0.5f * s, 0.04f, withAlpha(0xB45CFF, alpha));
        }
    }

    private static int withAlpha(int rgb, int a) {
        return ((a & 255) << 24) | (rgb & 0xFFFFFF);
    }

    private static void ring(PoseStack pose, MultiBufferSource buf, float r, float th, int argb) {
        VertexConsumer vc = buf.getBuffer(RenderType.eyes(TEX));
        Matrix4f m = pose.last().pose();
        int segs = 20;
        for (int i = 0; i < segs; i++) {
            float a0 = (float) (i * Math.PI * 2 / segs);
            float a1 = (float) ((i + 1) * Math.PI * 2 / segs);
            float x0 = Mth.cos(a0) * r, z0 = Mth.sin(a0) * r;
            float x1 = Mth.cos(a1) * r, z1 = Mth.sin(a1) * r;
            vert(vc, m, x0, -th, z0, argb);
            vert(vc, m, x1, -th, z1, argb);
            vert(vc, m, x1, th, z1, argb);
            vert(vc, m, x0, th, z0, argb);
        }
    }

    private static void billboard(PoseStack pose, MultiBufferSource buf, float s, int argb) {
        pose.pushPose();
        pose.mulPose(Minecraft.getInstance().gameRenderer.getMainCamera().rotation());
        VertexConsumer vc = buf.getBuffer(RenderType.eyes(TEX));
        Matrix4f m = pose.last().pose();
        vert(vc, m, -s, -s, 0, argb);
        vert(vc, m, s, -s, 0, argb);
        vert(vc, m, s, s, 0, argb);
        vert(vc, m, -s, s, 0, argb);
        pose.popPose();
    }

    private static void plane(PoseStack pose, MultiBufferSource buf, float w, float h, int fill, int edge) {
        VertexConsumer vc = buf.getBuffer(RenderType.eyes(TEX));
        Matrix4f m = pose.last().pose();
        vert(vc, m, -w / 2, -h / 2, 0, fill);
        vert(vc, m, w / 2, -h / 2, 0, fill);
        vert(vc, m, w / 2, h / 2, 0, fill);
        vert(vc, m, -w / 2, h / 2, 0, fill);
        float t = 0.05f;
        vert(vc, m, -w / 2, h / 2 - t, 0.01f, edge);
        vert(vc, m, w / 2, h / 2 - t, 0.01f, edge);
        vert(vc, m, w / 2, h / 2 + t, 0.01f, edge);
        vert(vc, m, -w / 2, h / 2 + t, 0.01f, edge);
    }

    private static void vert(VertexConsumer vc, Matrix4f m, float x, float y, float z, int argb) {
        int a = (argb >>> 24) & 255, r = (argb >>> 16) & 255, g = (argb >>> 8) & 255, b = argb & 255;
        vc.addVertex(m, x, y, z).setColor(r, g, b, a).setUv(0.5f, 0.5f)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0, 1, 0);
    }

    private static final class Vfx {
        final String type;
        final double x, y, z;
        final float scale;
        final float lifetime;
        float age;

        Vfx(String type, double x, double y, double z, float scale) {
            this.type = type;
            this.x = x;
            this.y = y;
            this.z = z;
            this.scale = scale;
            this.lifetime = switch (type) {
                case "ultimate_start", "ultimate_collapse" -> 1.2f;
                case "rift_collapse" -> 0.9f;
                case "void_slash" -> 0.55f;
                case "teleport_arrival" -> 0.7f;
                default -> 0.6f;
            };
        }
    }
}

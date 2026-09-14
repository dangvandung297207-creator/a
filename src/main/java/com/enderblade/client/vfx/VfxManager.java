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
 * Client-side geometry VFX (rifts, rings, collapse, afterimages, slash planes).
 * Prefer mesh + emissive over particle spam. Optimized: few active FX, short lifetimes.
 */
public final class VfxManager {

    private static final ResourceLocation TEX =
            ResourceLocation.fromNamespaceAndPath(EnderBladeMod.MOD_ID, "textures/entity/vfx_atlas.png");

    private static final List<Vfx> ACTIVE = new ArrayList<>();
    private static final int MAX_ACTIVE = 48;

    private VfxManager() {
    }

    public static void spawn(String type, double x, double y, double z, float scale) {
        if (ACTIVE.size() >= MAX_ACTIVE) {
            ACTIVE.remove(0);
        }
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
        float fade = t < 0.15f ? t / 0.15f : 1f - (t - 0.15f) / 0.85f;
        fade = Mth.clamp(fade, 0f, 1f);
        int alpha = (int) (fade * 230);
        float s = v.scale;

        switch (v.type) {
            case "spatial_burst", "spatial_cut" -> {
                // Fractured spatial cut: dark plane + violet edge + thin rings
                float open = t < 0.25f ? t / 0.25f : 1f;
                float out = t > 0.55f ? 1f - (t - 0.55f) / 0.45f : 1f;
                float a = fade * out;
                plane(pose, buffers, 1.4f * open * s, 0.1f * s,
                        withAlpha(0x05020A, (int) (a * 220)),
                        withAlpha(0xC070FF, (int) (a * 240)));
                ring(pose, buffers, (0.25f + t * 0.9f) * s, 0.035f, withAlpha(0xB45CFF, (int) (a * 180)));
            }
            case "afterimage" -> {
                billboard(pose, buffers, 0.45f * s * (1f - t * 0.4f), withAlpha(0x8A40D0, alpha / 2));
            }
            case "teleport_depart" -> {
                // Vertical rift collapse into void point
                float h = 1.6f * s * (t < 0.4f ? 1f : 1f - (t - 0.4f) / 0.6f);
                float w = 0.08f * s * (t < 0.35f ? 0.5f + t * 3f : 1.5f * (1f - t));
                plane(pose, buffers, w, h,
                        withAlpha(0x03010A, alpha),
                        withAlpha(0xC070FF, alpha));
                ring(pose, buffers, 0.2f * s * (1f - t), 0.03f, withAlpha(0xE0A0FF, alpha / 2));
            }
            case "teleport_arrival", "echo_launch" -> {
                float expand = (0.35f + t * 1.1f) * s;
                ring(pose, buffers, expand, 0.05f, withAlpha(0xC070FF, alpha));
                ring(pose, buffers, expand * 0.55f, 0.03f, withAlpha(0xE0A0FF, alpha));
                billboard(pose, buffers, 0.25f * s * (1f - t), withAlpha(0xF0C0FF, alpha / 2));
            }
            case "anchor_place", "anchor_recall" -> {
                pose.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90));
                float r = (0.4f + t * 0.5f) * s;
                ring(pose, buffers, r, 0.05f, withAlpha(0xA050FF, alpha));
                ring(pose, buffers, r * 0.55f, 0.035f, withAlpha(0xC070FF, alpha));
            }
            case "void_slash" -> {
                // Literal cut in space: open → strike → fracture → collapse
                float open = t < 0.2f ? t / 0.2f : (t < 0.55f ? 1f : 1f - (t - 0.55f) / 0.45f);
                float jitter = Mth.sin(t * 40f) * 0.02f * open;
                pose.translate(jitter, 0, 0);
                plane(pose, buffers, 2.2f * open * s, 1.5f * open * s,
                        withAlpha(0x05020A, (int) (fade * 230)),
                        withAlpha(0xC070FF, (int) (fade * 250)));
                // fracture edge ticks
                plane(pose, buffers, 2.3f * open * s, 0.04f * s,
                        withAlpha(0xFF6AE0, (int) (fade * 180)),
                        withAlpha(0xFF6AE0, (int) (fade * 180)));
            }
            case "rift_collapse", "ultimate_collapse" -> {
                // SPACE COLLAPSING INWARD — rings shrink, dark core
                float shrink = (1.0f - t) * 1.6f * s;
                ring(pose, buffers, shrink, 0.07f, withAlpha(0xFF4FD8, alpha));
                ring(pose, buffers, shrink * 0.65f, 0.05f, withAlpha(0xB45CFF, alpha));
                ring(pose, buffers, shrink * 0.3f, 0.04f, withAlpha(0x6B2AD1, alpha));
                float core = t < 0.7f ? (1f - t) * 0.35f * s : (t - 0.7f) * 1.2f * s;
                billboard(pose, buffers, Math.max(0.05f, core), withAlpha(0x0A0210, alpha));
                if (t > 0.75f) {
                    // short violent burst shards as small rings
                    float burst = (t - 0.75f) / 0.25f;
                    ring(pose, buffers, burst * 1.4f * s, 0.04f, withAlpha(0xFF80F0, (int) ((1f - burst) * 200)));
                }
            }
            case "ultimate_start" -> {
                float expand = t * 3.2f * s;
                pose.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90));
                ring(pose, buffers, expand, 0.12f, withAlpha(0xB45CFF, alpha));
                ring(pose, buffers, expand * 0.7f, 0.06f, withAlpha(0x8040C0, alpha / 2));
            }
            case "heavy_slash" -> {
                // Large curved dimensional slash remaining briefly
                float open = t < 0.2f ? t / 0.2f : 1f;
                float out = t > 0.5f ? 1f - (t - 0.5f) / 0.5f : 1f;
                arcSlash(pose, buffers, 1.4f * open * s, 0.35f * s, withAlpha(0x05020A, (int) (out * fade * 220)),
                        withAlpha(0xC070FF, (int) (out * fade * 240)));
            }
            case "mark_pulse" -> {
                float r = (0.3f + t * 0.4f) * s;
                pose.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90));
                ring(pose, buffers, r, 0.04f, withAlpha(0xC060FF, alpha));
            }
            case "echo_pass" -> {
                // Entity silhouette cut flash
                billboard(pose, buffers, 0.4f * s * (1f - t), withAlpha(0xE0A0FF, alpha));
                plane(pose, buffers, 0.7f * s, 0.05f * s,
                        withAlpha(0x05020A, alpha), withAlpha(0xFF6AE0, alpha));
            }
            case "equip_rift" -> {
                float h = 0.9f * s;
                float w = 0.06f * s * (t < 0.5f ? 0.5f + t * 4f : 2.5f * (1f - t));
                plane(pose, buffers, w, h, withAlpha(0x03010A, alpha), withAlpha(0xB45CFF, alpha));
            }
            default -> ring(pose, buffers, 0.5f * s, 0.04f, withAlpha(0xB45CFF, alpha));
        }
    }

    private static int withAlpha(int rgb, int a) {
        return ((Math.max(0, Math.min(255, a))) << 24) | (rgb & 0xFFFFFF);
    }

    private static void ring(PoseStack pose, MultiBufferSource buf, float r, float th, int argb) {
        VertexConsumer vc = buf.getBuffer(RenderType.eyes(TEX));
        Matrix4f m = pose.last().pose();
        int segs = 24;
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
        float hw = w / 2f, hh = h / 2f;
        vert(vc, m, -hw, -hh, 0, fill);
        vert(vc, m, hw, -hh, 0, fill);
        vert(vc, m, hw, hh, 0, fill);
        vert(vc, m, -hw, hh, 0, fill);
        // edge rim
        float t = Math.max(0.02f, h * 0.08f);
        vert(vc, m, -hw, hh - t, 0.01f, edge);
        vert(vc, m, hw, hh - t, 0.01f, edge);
        vert(vc, m, hw, hh + t, 0.01f, edge);
        vert(vc, m, -hw, hh + t, 0.01f, edge);
        vert(vc, m, -hw, -hh - t, 0.01f, edge);
        vert(vc, m, hw, -hh - t, 0.01f, edge);
        vert(vc, m, hw, -hh + t, 0.01f, edge);
        vert(vc, m, -hw, -hh + t, 0.01f, edge);
    }

    /** Approximate curved slash with stacked rotated planes. */
    private static void arcSlash(PoseStack pose, MultiBufferSource buf, float radius, float width, int fill, int edge) {
        int slices = 7;
        for (int i = 0; i < slices; i++) {
            float a = -0.9f + i * (1.8f / (slices - 1));
            pose.pushPose();
            pose.mulPose(com.mojang.math.Axis.ZP.rotation(a));
            pose.translate(radius * 0.55f, 0, 0);
            plane(pose, buf, width, radius * 0.35f, fill, edge);
            pose.popPose();
        }
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
                case "ultimate_start", "ultimate_collapse" -> 1.35f;
                case "rift_collapse" -> 1.05f;
                case "void_slash" -> 0.55f;
                case "heavy_slash" -> 0.8f;
                case "teleport_arrival", "teleport_depart" -> 0.65f;
                case "equip_rift" -> 0.7f;
                case "echo_pass" -> 0.35f;
                default -> 0.55f;
            };
        }
    }
}

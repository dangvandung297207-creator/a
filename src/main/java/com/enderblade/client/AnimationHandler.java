package com.enderblade.client;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks per-entity animation playback state for first/third person posing.
 * Driven each client tick by {@link com.enderblade.client.ClientEvents}.
 */
public final class AnimationHandler {

    public static final class AnimState {
        public String id = "idle";
        public float time;
        public float duration = 1.0f;
        public boolean playing;

        // Pose offsets applied to arm / item
        public float armPitch;
        public float armYaw;
        public float armRoll;
        public float itemPitch;
        public float itemYaw;
        public float itemRoll;
        public float itemBob;
    }

    private static final Map<Integer, AnimState> STATES = new ConcurrentHashMap<>();

    private AnimationHandler() {
    }

    public static void play(Entity entity, String animId) {
        AnimState s = STATES.computeIfAbsent(entity.getId(), id -> new AnimState());
        s.id = animId;
        s.time = 0f;
        s.duration = durationOf(animId);
        s.playing = true;
    }

    public static AnimState get(Entity entity) {
        return STATES.computeIfAbsent(entity.getId(), id -> new AnimState());
    }

    public static void tick(float dt) {
        for (AnimState s : STATES.values()) {
            if (!s.playing) {
                // idle breathe
                s.time += dt;
                applyIdle(s);
                continue;
            }
            s.time += dt;
            float t = Math.min(1f, s.time / s.duration);
            apply(s, t);
            if (t >= 1f) {
                s.playing = false;
                s.id = "idle";
                s.time = 0;
            }
        }
    }

    private static float durationOf(String id) {
        return switch (id) {
            case "equip" -> 0.55f;
            case "attack1" -> 0.35f;
            case "attack2" -> 0.40f;
            case "attack3" -> 0.55f;
            case "void_slash" -> 0.50f;
            case "ender_echo" -> 0.40f;
            case "teleport" -> 0.45f;
            case "void_anchor" -> 0.50f;
            case "paradox_step" -> 0.60f;
            case "ultimate" -> 0.90f;
            default -> 0.4f;
        };
    }

    private static void applyIdle(AnimState s) {
        float breathe = (float) Math.sin(s.time * 2.0) * 0.03f;
        s.armPitch = 0.35f + breathe; // assassin lowered stance
        s.armYaw = -0.15f;
        s.armRoll = 0.05f;
        s.itemPitch = 0.4f + breathe * 0.5f;
        s.itemYaw = 0.1f;
        s.itemRoll = -0.2f;
        s.itemBob = breathe * 0.02f;
    }

    private static float smooth(float t) {
        return t * t * (3f - 2f * t);
    }

    private static void apply(AnimState s, float t) {
        float sm = smooth(t);
        switch (s.id) {
            case "equip" -> {
                // Rift emerge: start high pitch then settle
                float emerge = t < 0.4f ? t / 0.4f : 1f;
                s.armPitch = 1.2f * (1f - emerge) + 0.35f * emerge;
                s.itemPitch = -1.5f * (1f - emerge) + 0.4f * emerge;
                s.itemBob = (1f - emerge) * 0.3f;
            }
            case "attack1" -> { // horizontal
                float swing = t < 0.35f ? smooth(t / 0.35f) : 1f - smooth((t - 0.35f) / 0.65f) * 0.4f;
                s.armYaw = -0.8f + swing * 1.6f;
                s.armPitch = 0.2f;
                s.itemRoll = -0.5f + swing;
            }
            case "attack2" -> { // diagonal up
                float swing = t < 0.4f ? smooth(t / 0.4f) : 1f - (t - 0.4f);
                s.armPitch = 0.6f - swing * 1.2f;
                s.armYaw = -0.3f + swing * 0.8f;
                s.itemPitch = 0.5f - swing;
            }
            case "attack3" -> { // heavy down
                float ant = t < 0.25f ? smooth(t / 0.25f) : 1f;
                float strike = t < 0.25f ? 0 : (t < 0.5f ? smooth((t - 0.25f) / 0.25f) : 1f);
                s.armPitch = -0.6f * ant + 1.2f * strike;
                s.itemPitch = -0.8f * ant + 1.0f * strike;
            }
            case "void_slash" -> {
                s.armPitch = -0.4f + sm * 1.4f;
                s.armYaw = sm * 0.6f;
                s.itemRoll = sm * 1.2f;
            }
            case "ender_echo" -> {
                s.armPitch = 0.1f - sm * 0.5f;
                s.itemPitch = -0.3f;
            }
            case "teleport" -> {
                float pulse = (float) Math.sin(t * Math.PI);
                s.itemBob = pulse * 0.25f;
                s.armPitch = 0.35f + pulse * 0.3f;
            }
            case "paradox_step" -> {
                s.armPitch = t < 0.5f ? 0.8f : 0.1f + sm;
                s.armYaw = (t - 0.5f) * 2f;
            }
            case "ultimate" -> {
                s.armPitch = 1.4f * sm;
                s.itemPitch = 1.2f * sm;
            }
            case "void_anchor" -> {
                s.armPitch = 0.9f * (1f - sm) + 0.35f * sm;
            }
            default -> applyIdle(s);
        }
    }

    public static boolean isHoldingBlade(Player player) {
        return player.getMainHandItem().is(com.enderblade.registry.ModItems.ENDER_BLADE.get())
                || player.getOffhandItem().is(com.enderblade.registry.ModItems.ENDER_BLADE.get());
    }
}

package com.enderblade.client;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks per-entity animation playback for first/third person posing.
 * Distinct rhythms per attack; polished equip / ability sequences.
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
        /** 0..2+ crack/emissive combat boost for model */
        public float crackBoost = 1f;
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
        s.crackBoost = 1f;
    }

    public static AnimState get(Entity entity) {
        return STATES.computeIfAbsent(entity.getId(), id -> new AnimState());
    }

    public static void tick(float dt) {
        for (AnimState s : STATES.values()) {
            if (!s.playing) {
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
                s.crackBoost = 1f;
            }
        }
    }

    private static float durationOf(String id) {
        return switch (id) {
            case "equip" -> 1.1f;
            case "attack1" -> 0.42f;
            case "attack2" -> 0.50f;
            case "attack3" -> 0.70f;
            case "heavy" -> 0.95f;
            case "void_slash" -> 0.65f;
            case "ender_echo" -> 0.55f;
            case "teleport" -> 0.70f;
            case "void_anchor" -> 0.60f;
            case "paradox_step" -> 0.85f;
            case "ultimate" -> 1.20f;
            default -> 0.45f;
        };
    }

    private static void applyIdle(AnimState s) {
        float breathe = (float) Math.sin(s.time * 1.8) * 0.028f;
        float sway = (float) Math.sin(s.time * 0.9) * 0.02f;
        // Assassin dimensional stance
        s.armPitch = 0.38f + breathe;
        s.armYaw = -0.18f + sway;
        s.armRoll = 0.08f;
        s.itemPitch = 0.35f + breathe * 0.4f;
        s.itemYaw = 0.08f;
        s.itemRoll = -0.18f + sway * 0.5f;
        s.itemBob = breathe * 0.018f;
        s.crackBoost = 0.9f + (float) Math.sin(s.time * 2.0) * 0.08f;
    }

    private static float smooth(float t) {
        return t * t * (3f - 2f * t);
    }

    private static float seg(float t, float a, float b) {
        if (t <= a) return 0f;
        if (t >= b) return 1f;
        return smooth((t - a) / (b - a));
    }

    private static void apply(AnimState s, float t) {
        switch (s.id) {
            case "equip" -> {
                // Rift emerge → grab → pulse
                float emerge = seg(t, 0.45f, 0.78f);
                float settle = seg(t, 0.8f, 1.0f);
                s.armPitch = 1.15f * (1f - emerge) + 0.38f * emerge;
                s.armYaw = -0.4f * (1f - emerge);
                s.itemPitch = -1.4f * (1f - emerge) + 0.35f * emerge;
                s.itemRoll = 1.2f * (1f - emerge) - 0.18f * emerge;
                s.itemBob = (1f - emerge) * 0.35f - settle * 0.05f;
                s.crackBoost = 0.6f + emerge * 1.4f + seg(t, 0.88f, 1f) * 0.8f;
            }
            case "attack1" -> { // Fast horizontal — distinct rhythm
                float ant = seg(t, 0f, 0.22f);
                float strike = seg(t, 0.22f, 0.48f);
                float follow = seg(t, 0.48f, 1f);
                s.armYaw = -0.95f * ant + strike * 1.9f - follow * 0.35f;
                s.armPitch = 0.22f - ant * 0.1f;
                s.armRoll = 0.35f - strike * 0.4f;
                s.itemRoll = -0.55f * ant + strike * 1.3f;
                s.itemYaw = strike * 0.3f;
                s.crackBoost = 1f + ant * 0.3f + strike * 0.9f;
            }
            case "attack2" -> { // Diagonal aggressive
                float ant = seg(t, 0f, 0.2f);
                float strike = seg(t, 0.2f, 0.55f);
                s.armPitch = 0.85f * ant - strike * 1.55f;
                s.armYaw = -0.35f + strike * 0.95f;
                s.armRoll = 0.15f + strike * 0.55f;
                s.itemPitch = 0.55f * ant - strike * 1.1f;
                s.itemRoll = -0.25f + strike * 0.85f;
                s.crackBoost = 1.1f + strike * 1.0f;
            }
            case "attack3" -> { // Heavy downward — long anticipation
                float ant = seg(t, 0f, 0.32f);
                float strike = seg(t, 0.32f, 0.6f);
                s.armPitch = -0.9f * ant + strike * 2.0f;
                s.armYaw = -0.15f + strike * 0.35f;
                s.itemPitch = -0.95f * ant + strike * 1.4f;
                s.itemRoll = 0.2f * ant;
                s.crackBoost = 1.2f + ant * 1.2f + strike * 1.4f;
            }
            case "heavy" -> {
                float pull = seg(t, 0f, 0.35f);
                float strike = seg(t, 0.5f, 0.75f);
                s.armPitch = -1.05f * pull + strike * 2.15f;
                s.armYaw = -0.55f * pull + strike * 0.9f;
                s.itemPitch = -1.15f * pull + strike * 1.7f;
                s.itemRoll = pull * 0.3f;
                s.crackBoost = 1.4f + pull * 1.5f + strike * 1.2f;
            }
            case "void_slash" -> {
                float ant = seg(t, 0f, 0.28f);
                float cut = seg(t, 0.28f, 0.55f);
                s.armPitch = -0.45f * ant + cut * 1.55f;
                s.armYaw = 0.25f * ant + cut * 0.55f;
                s.itemRoll = cut * 1.4f;
                s.itemPitch = -0.3f * ant + cut * 0.7f;
                s.crackBoost = 1.5f + cut * 1.8f;
            }
            case "ender_echo" -> {
                float wind = seg(t, 0f, 0.35f);
                float cast = seg(t, 0.35f, 0.55f);
                s.armPitch = 0.15f - wind * 0.55f - cast * 0.15f;
                s.itemPitch = -0.45f * wind;
                s.itemBob = wind * 0.05f;
                s.crackBoost = 1.2f + cast * 0.7f;
            }
            case "teleport" -> {
                float pulse = (float) Math.sin(t * Math.PI);
                s.itemBob = pulse * 0.28f;
                s.armPitch = 0.38f + pulse * 0.35f;
                s.itemPitch = 0.35f + pulse * 0.4f;
                s.crackBoost = 1.5f + pulse * 1.0f;
            }
            case "paradox_step" -> {
                float freeze = seg(t, 0f, 0.2f);
                float slash = seg(t, 0.65f, 0.9f);
                s.armPitch = 0.7f * freeze + (1f - freeze) * 0.2f + slash * 0.3f;
                s.armYaw = -0.4f + slash * 1.7f;
                s.itemRoll = slash * 1.15f;
                s.crackBoost = 1.3f + slash * 1.6f;
            }
            case "ultimate" -> {
                float drive = seg(t, 0f, 0.25f);
                float hold = seg(t, 0.25f, 0.75f);
                s.armPitch = 1.45f * drive + hold * 0.15f;
                s.itemPitch = 1.25f * drive;
                s.armRoll = 0.1f * drive;
                s.crackBoost = 1.6f + drive * 1.2f + hold * 0.6f;
            }
            case "void_anchor" -> {
                float plant = seg(t, 0f, 0.45f);
                s.armPitch = 0.95f * plant + 0.38f * (1f - plant);
                s.itemPitch = 0.55f * plant;
                s.crackBoost = 1.15f + plant * 0.5f;
            }
            default -> applyIdle(s);
        }
    }

    public static boolean isHoldingBlade(Player player) {
        return player.getMainHandItem().is(com.enderblade.registry.ModItems.ENDER_BLADE.get())
                || player.getOffhandItem().is(com.enderblade.registry.ModItems.ENDER_BLADE.get());
    }
}

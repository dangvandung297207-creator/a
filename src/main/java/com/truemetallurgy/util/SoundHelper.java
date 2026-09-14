package com.truemetallurgy.util;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;

import java.util.function.Supplier;

/** Plays custom sounds with a quiet vanilla fallback layered underneath. */
public final class SoundHelper {
    private SoundHelper() {}

    public static void play(Level level, BlockPos pos, Supplier<SoundEvent> custom, SoundEvent fallback,
            SoundSource source, float volume, float pitch) {
        if (level.isClientSide) return;
        try {
            level.playSound(null, pos, custom.get(), source, volume, pitch);
        } catch (Exception ignored) {
            // Missing .ogg files must never break gameplay; fallback covers it.
        }
        if (fallback != null) {
            level.playSound(null, pos, fallback, source, Math.min(1.0F, volume * 0.5F), pitch);
        }
    }

    public static void play(Level level, BlockPos pos, SoundEvent event, SoundSource source, float volume, float pitch) {
        if (level.isClientSide) return;
        level.playSound(null, pos, event, source, volume, pitch);
    }
}

package com.masterblacksmith.util;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

/** Pitch-varied playback so repeated hammer work never sounds looped. */
public final class SoundUtil {
    private SoundUtil() {}

    public static float variedPitch(RandomSource rand, float base, float variance) {
        return base + (rand.nextFloat() * 2F - 1F) * variance;
    }

    public static void play(Level level, BlockPos pos, SoundEvent sound, SoundSource source,
                            float volume, float basePitch, float variance) {
        float pitch = variedPitch(level.random, basePitch, variance);
        if (level.isClientSide) {
            level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    sound, source, volume, pitch, false);
        } else {
            level.playSound(null, pos, sound, source, volume, pitch);
        }
    }

    public static void playBlock(Level level, BlockPos pos, SoundEvent sound, float volume, float pitch) {
        play(level, pos, sound, SoundSource.BLOCKS, volume, pitch, 0.06F);
    }
}

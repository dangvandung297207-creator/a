package com.truemetallurgy.util;

import com.truemetallurgy.TrueMetallurgy;
import net.minecraft.resources.ResourceLocation;

/** Shared helpers. */
public final class TMUtil {
    private TMUtil() {}

    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(TrueMetallurgy.MOD_ID, path);
    }

    public static float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }

    public static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }
}

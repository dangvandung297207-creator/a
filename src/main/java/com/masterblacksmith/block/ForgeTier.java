package com.masterblacksmith.block;

import net.minecraft.network.chat.Component;

/** Primitive -&gt; Iron -&gt; Steel -&gt; Master forge progression. */
public enum ForgeTier {
    PRIMITIVE("primitive", 1100, 34F, 1.0F),
    IRON("iron", 1250, 44F, 0.9F),
    STEEL("steel", 1400, 56F, 0.8F),
    MASTER("master", 1650, 72F, 0.65F);

    private final String key;
    private final int maxTemp;
    private final float heatRate;
    private final float fuelUse;

    ForgeTier(String key, int maxTemp, float heatRate, float fuelUse) {
        this.key = key;
        this.maxTemp = maxTemp;
        this.heatRate = heatRate;
        this.fuelUse = fuelUse;
    }

    public int getMaxTemp() { return maxTemp; }
    public float getHeatRate() { return heatRate; }
    public float getFuelUse() { return fuelUse; }

    public Component getDisplayName() {
        return Component.translatable("forge.masterblacksmith." + key);
    }
}

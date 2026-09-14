package com.masterblacksmith.forging;

import com.masterblacksmith.material.MetalMaterial;
import net.minecraft.network.chat.Component;

/**
 * Heat zones for a billet. The sweet spot is per-metal: forging inside it
 * deforms well and builds quality, cold steel cracks, overheated steel scales.
 */
public enum HeatZone {
    COLD("cold", 0x7A8A99, false),
    WARM("warm", 0x8A5A3A, false),
    LOW("low", 0xC4502A, true),
    SWEET("sweet", 0xFFAA2A, true),
    HIGH("high", 0xFFE27A, true),
    OVERHEAT("overheat", 0xFFF6D8, true),
    MOLTEN("molten", 0xFFFFFF, false);

    private final String key;
    private final int color;
    private final boolean forgeable;

    HeatZone(String key, int color, boolean forgeable) {
        this.key = key;
        this.color = color;
        this.forgeable = forgeable;
    }

    public boolean isForgeable() { return forgeable; }
    public int getColor() { return color; }

    public Component getDisplayName() {
        return Component.translatable("heat.masterblacksmith." + key);
    }

    public static HeatZone zoneFor(MetalMaterial metal, float tempC) {
        if (tempC < 300) return COLD;
        if (tempC < metal.getForgeMin()) return WARM;
        if (tempC < metal.getSweetMin()) return LOW;
        if (tempC <= metal.getSweetMax()) return SWEET;
        if (tempC <= metal.getForgeMax()) return HIGH;
        if (tempC <= metal.getBurnTemp()) return OVERHEAT;
        return MOLTEN;
    }

    /** 0..100 score of how good this temperature is for forging. */
    public static float heatScore(MetalMaterial metal, float tempC) {
        HeatZone zone = zoneFor(metal, tempC);
        return switch (zone) {
            case SWEET -> 100F;
            case LOW, HIGH -> 78F;
            case WARM -> 30F;
            case COLD -> 5F;
            case OVERHEAT -> 45F;
            case MOLTEN -> 0F;
        };
    }
}

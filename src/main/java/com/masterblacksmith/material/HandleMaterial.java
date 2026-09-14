package com.masterblacksmith.material;

import net.minecraft.network.chat.Component;

/** Grip, balance and feel profile of one handle material. */
public class HandleMaterial {
    private final String id;
    private final float grip;
    private final float durabilityMod;
    private final float weightKg;
    private final float speedBonus;

    public HandleMaterial(String id, float grip, float durabilityMod, float weightKg, float speedBonus) {
        this.id = id;
        this.grip = grip;
        this.durabilityMod = durabilityMod;
        this.weightKg = weightKg;
        this.speedBonus = speedBonus;
    }

    public String getId() { return id; }
    public float getGrip() { return grip; }
    public float getDurabilityMod() { return durabilityMod; }
    public float getWeightKg() { return weightKg; }
    public float getSpeedBonus() { return speedBonus; }

    public Component getDisplayName() {
        return Component.translatable("handle.masterblacksmith." + id);
    }
}

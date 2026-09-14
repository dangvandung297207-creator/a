package com.masterblacksmith.material;

import net.minecraft.network.chat.Component;

/**
 * Full metallurgy profile of one forgeable metal. No reskins: every metal has
 * its own heat window, sweet spot, workability and combat stats.
 */
public class MetalMaterial {
    private final String id;
    private final int color;
    private final float hardness;
    private final float toughness;
    private final float sharpness;
    private final float flexibility;
    private final float durability;
    private final float heatResistance;
    private final float density;
    private final float forgeMin;
    private final float forgeMax;
    private final float sweetMin;
    private final float sweetMax;
    private final float burnTemp;
    private final float workability;
    private final float baseDamage;
    private final int baseDurability;

    public MetalMaterial(String id, int color,
                         float hardness, float toughness, float sharpness, float flexibility,
                         float durability, float heatResistance, float density,
                         float forgeMin, float forgeMax, float sweetMin, float sweetMax, float burnTemp,
                         float workability, float baseDamage, int baseDurability) {
        this.id = id;
        this.color = color;
        this.hardness = hardness;
        this.toughness = toughness;
        this.sharpness = sharpness;
        this.flexibility = flexibility;
        this.durability = durability;
        this.heatResistance = heatResistance;
        this.density = density;
        this.forgeMin = forgeMin;
        this.forgeMax = forgeMax;
        this.sweetMin = sweetMin;
        this.sweetMax = sweetMax;
        this.burnTemp = burnTemp;
        this.workability = workability;
        this.baseDamage = baseDamage;
        this.baseDurability = baseDurability;
    }

    public String getId() { return id; }
    public int getColor() { return color; }
    public float getHardness() { return hardness; }
    public float getToughness() { return toughness; }
    public float getSharpness() { return sharpness; }
    public float getFlexibility() { return flexibility; }
    public float getDurability() { return durability; }
    public float getHeatResistance() { return heatResistance; }
    public float getDensity() { return density; }
    public float getForgeMin() { return forgeMin; }
    public float getForgeMax() { return forgeMax; }
    public float getSweetMin() { return sweetMin; }
    public float getSweetMax() { return sweetMax; }
    public float getBurnTemp() { return burnTemp; }
    public float getWorkability() { return workability; }
    public float getBaseDamage() { return baseDamage; }
    public int getBaseDurability() { return baseDurability; }

    public Component getDisplayName() {
        return Component.translatable("metal.masterblacksmith." + id);
    }
}

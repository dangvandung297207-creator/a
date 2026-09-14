package com.masterblacksmith.material;

import net.minecraft.network.chat.Component;

/**
 * A quenching medium. Each one reshapes the metallurgy profile instead of
 * handing out flat stat bonuses, and each has its own cooling behaviour.
 */
public class QuenchLiquid {
    private final String id;
    private final float hardnessMod;
    private final float toughnessMod;
    private final float sharpnessMod;
    private final float durabilityMod;
    private final float coolRate;
    private final int color;
    private final float steam;
    private final boolean darkSmoke;
    private final float purityBonus;

    public QuenchLiquid(String id, float hardnessMod, float toughnessMod, float sharpnessMod,
                        float durabilityMod, float coolRate, int color, float steam,
                        boolean darkSmoke, float purityBonus) {
        this.id = id;
        this.hardnessMod = hardnessMod;
        this.toughnessMod = toughnessMod;
        this.sharpnessMod = sharpnessMod;
        this.durabilityMod = durabilityMod;
        this.coolRate = coolRate;
        this.color = color;
        this.steam = steam;
        this.darkSmoke = darkSmoke;
        this.purityBonus = purityBonus;
    }

    public String getId() { return id; }
    public float getHardnessMod() { return hardnessMod; }
    public float getToughnessMod() { return toughnessMod; }
    public float getSharpnessMod() { return sharpnessMod; }
    public float getDurabilityMod() { return durabilityMod; }
    public float getCoolRate() { return coolRate; }
    public int getColor() { return color; }
    public float getSteam() { return steam; }
    public boolean isDarkSmoke() { return darkSmoke; }
    public float getPurityBonus() { return purityBonus; }

    public Component getDisplayName() {
        return Component.translatable("quench.masterblacksmith." + id);
    }
}

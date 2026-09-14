package com.truemetallurgy.metallurgy;

/** Grinding edge-angle presets. Directly trades sharpness for durability. */
public enum GrindAngle {
    /** 15 degrees: extremely sharp, fragile. */
    KEEN(15, 1.22F, 0.78F),
    /** 25 degrees: balanced. */
    BALANCED(25, 1.0F, 1.0F),
    /** 35 degrees: durable, blunter. */
    STURDY(35, 0.86F, 1.28F);

    public final int degrees;
    public final float sharpnessMult;
    public final float durabilityMult;

    GrindAngle(int degrees, float sharpnessMult, float durabilityMult) {
        this.degrees = degrees;
        this.sharpnessMult = sharpnessMult;
        this.durabilityMult = durabilityMult;
    }

    public static GrindAngle byIndex(int index) {
        GrindAngle[] v = values();
        if (index < 0 || index >= v.length) return BALANCED;
        return v[index];
    }

    public String langKey() {
        return "grind.true_metallurgy." + name().toLowerCase();
    }
}

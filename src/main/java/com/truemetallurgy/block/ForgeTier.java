package com.truemetallurgy.block;

/** Forge progression: Primitive -> Iron -> Steel -> Master. */
public enum ForgeTier {
    PRIMITIVE(1150, 1.0F, 1.0F, false, false),
    IRON(1300, 1.2F, 1.2F, false, false),
    STEEL(1450, 1.35F, 1.35F, false, false),
    MASTER(1600, 1.6F, 1.5F, true, true);

    /** Maximum chamber temperature in C. */
    public final int maxTemp;
    /** Heating rate multiplier. */
    public final float heatingMult;
    /** Fuel efficiency multiplier. */
    public final float fuelMult;
    /** Master forge keeps its own airflow without bellows. */
    public final boolean autoAirflow;
    /** Master forge holds temperature precisely near the target. */
    public final boolean tempHold;

    ForgeTier(int maxTemp, float heatingMult, float fuelMult, boolean autoAirflow, boolean tempHold) {
        this.maxTemp = maxTemp;
        this.heatingMult = heatingMult;
        this.fuelMult = fuelMult;
        this.autoAirflow = autoAirflow;
        this.tempHold = tempHold;
    }
}

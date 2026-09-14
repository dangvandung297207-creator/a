package com.truemetallurgy.metallurgy;

/** Handle/grip materials. Affect weight, durability and recovery. */
public enum HandleMaterial {
    OAK("oak", 1.0F, 1.0F, 1.0F),
    SPRUCE("spruce", 0.92F, 0.9F, 1.05F),
    BIRCH("birch", 0.88F, 0.85F, 1.1F),
    DARK_OAK("dark_oak", 1.1F, 1.1F, 0.95F),
    BAMBOO("bamboo", 0.75F, 0.8F, 1.18F),
    REINFORCED("reinforced", 1.25F, 1.35F, 0.9F),
    LEATHER_WRAPPED("leather_wrapped", 1.05F, 1.2F, 1.12F),
    BONE("bone", 0.95F, 1.15F, 1.05F);

    /** Display id. */
    public final String id;
    /** Weight multiplier (attack speed impact). */
    public final float weightMult;
    /** Durability multiplier. */
    public final float durabilityMult;
    /** Attack recovery multiplier. */
    public final float recoveryMult;

    HandleMaterial(String id, float weightMult, float durabilityMult, float recoveryMult) {
        this.id = id;
        this.weightMult = weightMult;
        this.durabilityMult = durabilityMult;
        this.recoveryMult = recoveryMult;
    }

    public String langKey() {
        return "handle.true_metallurgy." + id;
    }
}

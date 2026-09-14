package com.truemetallurgy.forging;

/** Outcome of one evaluated hammer strike. Deterministic: skill, not RNG. */
public record StrikeResult(
    Grade grade,
    float progress,
    float scoreDelta,
    float purityDelta,
    boolean cracked,
    boolean tooCold,
    boolean tooHot,
    boolean rushed,
    boolean finished
) {
    public enum Grade {
        PERFECT, GOOD, MISS, BAD;

        public String langKey() {
            return "strike.true_metallurgy." + name().toLowerCase();
        }
    }

    public static StrikeResult none() {
        return new StrikeResult(Grade.MISS, 0, 0, 0, false, false, false, false, false);
    }
}

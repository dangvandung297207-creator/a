package com.truemetallurgy.metallurgy;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Quenching medium definition. New liquids (salt water, herbal oil, mineral
 * oil, alchemical oil, ...) register here without touching quench logic.
 */
public record QuenchMedium(
    String id,
    /** Degrees per second removed while quenching. */
    int coolingRate,
    /** Damage/sharpness multiplier. */
    float hardnessMult,
    /** Durability multiplier. */
    float toughnessMult,
    /** Extra craftsmanship bonus/penalty. */
    int qualityBonus,
    boolean waterLike
) {
    private static final Map<String, QuenchMedium> BY_ID = new LinkedHashMap<>();

    public static final QuenchMedium WATER = register(new QuenchMedium("water", 900, 1.18F, 0.82F, 0, true));
    public static final QuenchMedium OIL = register(new QuenchMedium("oil", 420, 1.05F, 1.22F, 2, false));
    // Future: SALT_WATER, HERBAL_OIL, MINERAL_OIL, ALCHEMICAL_OIL, ...

    public static QuenchMedium register(QuenchMedium medium) {
        BY_ID.put(medium.id(), medium);
        return medium;
    }

    public static QuenchMedium get(String id) {
        QuenchMedium m = BY_ID.get(id);
        return m != null ? m : WATER;
    }

    public static boolean exists(String id) {
        return BY_ID.containsKey(id);
    }

    public static Collection<QuenchMedium> all() {
        return Collections.unmodifiableCollection(BY_ID.values());
    }

    public String langKey() {
        return "quench.true_metallurgy." + id;
    }
}

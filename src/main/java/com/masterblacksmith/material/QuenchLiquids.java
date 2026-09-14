package com.masterblacksmith.material;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/** Water, oil and the six advanced quenching media. Cool rates in C per second. */
public class QuenchLiquids {
    private static final Map<String, QuenchLiquid> REGISTRY = new LinkedHashMap<>();

    public static final QuenchLiquid WATER = register(new QuenchLiquid("water",
            1.25F, 0.80F, 1.20F, 0.90F, 900F, 0x3F76E4, 1.0F, false, 0F));
    public static final QuenchLiquid OIL = register(new QuenchLiquid("oil",
            1.05F, 1.25F, 1.00F, 1.20F, 350F, 0x6B4A1F, 0.5F, true, 0F));
    public static final QuenchLiquid SALT_WATER = register(new QuenchLiquid("salt_water",
            1.35F, 0.70F, 1.30F, 0.80F, 1100F, 0x5FA8D3, 1.2F, false, 0F));
    public static final QuenchLiquid HERBAL_OIL = register(new QuenchLiquid("herbal_oil",
            1.10F, 1.30F, 1.05F, 1.35F, 300F, 0x5F7A3A, 0.5F, true, 1F));
    public static final QuenchLiquid MINERAL_OIL = register(new QuenchLiquid("mineral_oil",
            1.15F, 1.20F, 1.00F, 1.50F, 280F, 0x4A4A55, 0.6F, true, 0F));
    public static final QuenchLiquid ALCHEMICAL_OIL = register(new QuenchLiquid("alchemical_oil",
            1.20F, 1.30F, 1.25F, 1.30F, 250F, 0x7A3FA0, 0.7F, false, 2F));
    public static final QuenchLiquid BLOOD_INFUSED = register(new QuenchLiquid("blood_infused",
            1.30F, 1.10F, 1.35F, 1.00F, 500F, 0x8A1A1A, 0.8F, true, 0F));
    public static final QuenchLiquid STARFALL = register(new QuenchLiquid("starfall",
            1.40F, 1.40F, 1.40F, 1.40F, 200F, 0x4A5FFF, 0.9F, false, 3F));

    private static QuenchLiquid register(QuenchLiquid q) {
        REGISTRY.put(q.getId(), q);
        return q;
    }

    public static QuenchLiquid get(String id) {
        return REGISTRY.getOrDefault(id, WATER);
    }

    public static boolean exists(String id) {
        return REGISTRY.containsKey(id);
    }

    public static Collection<QuenchLiquid> all() {
        return REGISTRY.values();
    }
}

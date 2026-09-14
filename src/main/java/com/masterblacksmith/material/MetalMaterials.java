package com.masterblacksmith.material;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/** The eight forgeable metals. Temperatures in degrees C. */
public class MetalMaterials {
    private static final Map<String, MetalMaterial> REGISTRY = new LinkedHashMap<>();

    public static final MetalMaterial COPPER = register(new MetalMaterial("copper", 0xC8734B,
            0.35F, 0.50F, 0.35F, 0.80F, 0.50F, 0.30F, 8.90F,
            550, 850, 640, 760, 1050, 1.25F, 3.0F, 220));
    public static final MetalMaterial TIN = register(new MetalMaterial("tin", 0xC9D2D6,
            0.20F, 0.35F, 0.20F, 0.90F, 0.30F, 0.15F, 7.30F,
            300, 520, 350, 460, 700, 1.40F, 2.0F, 130));
    public static final MetalMaterial BRONZE = register(new MetalMaterial("bronze", 0xB08D57,
            0.55F, 0.65F, 0.60F, 0.60F, 0.65F, 0.45F, 8.70F,
            650, 950, 740, 880, 1100, 1.15F, 4.0F, 340));
    public static final MetalMaterial IRON = register(new MetalMaterial("iron", 0x8A8D91,
            0.60F, 0.70F, 0.60F, 0.55F, 0.70F, 0.55F, 7.85F,
            750, 1050, 850, 1000, 1300, 1.00F, 5.0F, 520));
    public static final MetalMaterial STEEL = register(new MetalMaterial("steel", 0x6E7681,
            0.80F, 0.80F, 0.85F, 0.60F, 0.90F, 0.70F, 7.85F,
            850, 1150, 950, 1100, 1400, 0.90F, 6.0F, 920));
    public static final MetalMaterial HARDENED_STEEL = register(new MetalMaterial("hardened_steel", 0x525A66,
            0.95F, 0.75F, 0.95F, 0.45F, 1.00F, 0.80F, 7.90F,
            900, 1200, 1000, 1150, 1450, 0.80F, 7.0F, 1450));
    public static final MetalMaterial DAMASCUS_STEEL = register(new MetalMaterial("damascus_steel", 0x7A8699,
            0.90F, 0.95F, 1.00F, 0.70F, 1.10F, 0.85F, 7.85F,
            900, 1220, 1000, 1175, 1450, 0.75F, 7.5F, 1850));
    public static final MetalMaterial STARFALL_STEEL = register(new MetalMaterial("starfall_steel", 0x6E7BFF,
            1.00F, 1.00F, 1.00F, 0.80F, 1.25F, 1.00F, 8.20F,
            1000, 1350, 1100, 1300, 1600, 0.70F, 9.0F, 2600));

    private static MetalMaterial register(MetalMaterial m) {
        REGISTRY.put(m.getId(), m);
        return m;
    }

    public static MetalMaterial get(String id) {
        return REGISTRY.getOrDefault(id, IRON);
    }

    public static boolean exists(String id) {
        return REGISTRY.containsKey(id);
    }

    public static Collection<MetalMaterial> all() {
        return REGISTRY.values();
    }
}

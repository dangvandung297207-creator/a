package com.masterblacksmith.material;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/** The eight handle materials. */
public class HandleMaterials {
    private static final Map<String, HandleMaterial> REGISTRY = new LinkedHashMap<>();

    public static final HandleMaterial OAK = register(new HandleMaterial("oak", 0.60F, 1.00F, 0.40F, 0.00F));
    public static final HandleMaterial SPRUCE = register(new HandleMaterial("spruce", 0.55F, 0.90F, 0.35F, 0.05F));
    public static final HandleMaterial BIRCH = register(new HandleMaterial("birch", 0.65F, 1.00F, 0.38F, 0.05F));
    public static final HandleMaterial DARK_OAK = register(new HandleMaterial("dark_oak", 0.70F, 1.15F, 0.45F, 0.00F));
    public static final HandleMaterial BAMBOO = register(new HandleMaterial("bamboo", 0.50F, 0.80F, 0.25F, 0.15F));
    public static final HandleMaterial REINFORCED = register(new HandleMaterial("reinforced", 0.75F, 1.50F, 0.70F, -0.10F));
    public static final HandleMaterial LEATHER_WRAPPED = register(new HandleMaterial("leather_wrapped", 0.95F, 1.20F, 0.50F, 0.05F));
    public static final HandleMaterial BONE = register(new HandleMaterial("bone", 0.60F, 1.30F, 0.55F, 0.00F));

    private static HandleMaterial register(HandleMaterial h) {
        REGISTRY.put(h.getId(), h);
        return h;
    }

    public static HandleMaterial get(String id) {
        return REGISTRY.getOrDefault(id, OAK);
    }

    public static Collection<HandleMaterial> all() {
        return REGISTRY.values();
    }
}

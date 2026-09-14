package com.truemetallurgy.metallurgy;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Extensible material registry. Built-in metals are registered here; addons
 * may call {@link #register(Material)} during mod construction to add more
 * without touching forging logic.
 */
public final class Materials {
    private Materials() {}

    private static final Map<String, Material> BY_ID = new LinkedHashMap<>();

    public static final Material COPPER = register(new Material(
        "copper", 1.2F, 1.6F, 0.9F, 1.6F, 220, 0.6F, 88.0F, 2.1F,
        0.15F, 1085, 700, 950, 830, 4.0F, 5.0F));
    public static final Material IRON = register(new Material(
        "iron", 2.0F, 2.2F, 1.0F, 1.2F, 320, 0.8F, 90.0F, 2.4F,
        0.35F, 1538, 750, 1050, 900, 5.0F, 6.0F));
    public static final Material STEEL = register(new Material(
        "steel", 3.0F, 2.6F, 1.35F, 1.0F, 520, 1.0F, 92.0F, 2.4F,
        0.6F, 1510, 850, 1150, 1000, 6.5F, 7.5F));
    public static final Material HARDENED_STEEL = register(new Material(
        "hardened_steel", 3.8F, 2.2F, 1.6F, 0.7F, 700, 1.2F, 94.0F, 2.4F,
        0.85F, 1500, 900, 1200, 1060, 7.5F, 8.5F));

    public static Material register(Material material) {
        BY_ID.put(material.id(), material);
        return material;
    }

    public static Material get(String id) {
        Material m = BY_ID.get(id);
        return m != null ? m : IRON;
    }

    public static boolean exists(String id) {
        return BY_ID.containsKey(id);
    }

    public static Collection<Material> all() {
        return Collections.unmodifiableCollection(BY_ID.values());
    }
}

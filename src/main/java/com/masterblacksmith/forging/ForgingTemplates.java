package com.masterblacksmith.forging;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Sword, axe, pickaxe, spear and armour-plate templates. */
public class ForgingTemplates {
    private static final Map<String, ForgingTemplate> REGISTRY = new LinkedHashMap<>();

    public static final ForgingTemplate SWORD = register(new ForgingTemplate("sword", List.of(
            new ForgingTemplate.Stage("draw_out", 4),
            new ForgingTemplate.Stage("shape_blade", 6),
            new ForgingTemplate.Stage("define_edge", 6),
            new ForgingTemplate.Stage("forge_tang", 4)), "sword_blade"));

    public static final ForgingTemplate AXE = register(new ForgingTemplate("axe", List.of(
            new ForgingTemplate.Stage("draw_out", 4),
            new ForgingTemplate.Stage("shape_head", 6),
            new ForgingTemplate.Stage("define_edge", 5),
            new ForgingTemplate.Stage("punch_eye", 4)), "axe_head"));

    public static final ForgingTemplate PICKAXE = register(new ForgingTemplate("pickaxe", List.of(
            new ForgingTemplate.Stage("draw_out", 4),
            new ForgingTemplate.Stage("shape_pick", 6),
            new ForgingTemplate.Stage("reinforce_eye", 5)), "pickaxe_head"));

    public static final ForgingTemplate SPEAR = register(new ForgingTemplate("spear", List.of(
            new ForgingTemplate.Stage("draw_out", 3),
            new ForgingTemplate.Stage("shape_head", 5),
            new ForgingTemplate.Stage("forge_socket", 4)), "spear_head"));

    public static final ForgingTemplate PLATE = register(new ForgingTemplate("plate", List.of(
            new ForgingTemplate.Stage("draw_out", 4),
            new ForgingTemplate.Stage("dish_plate", 6)), "armor_plate"));

    private static ForgingTemplate register(ForgingTemplate t) {
        REGISTRY.put(t.getId(), t);
        return t;
    }

    public static ForgingTemplate get(String id) {
        return REGISTRY.getOrDefault(id, SWORD);
    }

    public static boolean exists(String id) {
        return REGISTRY.containsKey(id);
    }

    public static Collection<ForgingTemplate> all() {
        return REGISTRY.values();
    }
}

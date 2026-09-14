package com.truemetallurgy.item;

/** Forgeable component kinds, each with a shaping template and tool output. */
public enum ComponentKind {
    SWORD_BLADE("sword_blade", "sword"),
    AXE_HEAD("axe_head", "axe"),
    PICKAXE_HEAD("pickaxe_head", "pickaxe"),
    SPEAR_HEAD("spear_head", "spear");

    /** Id used by ForgingShape.forComponent and item registry. */
    public final String shapeId;
    /** Finished tool kind produced at assembly. */
    public final String toolKind;

    ComponentKind(String shapeId, String toolKind) {
        this.shapeId = shapeId;
        this.toolKind = toolKind;
    }

    public static ComponentKind byShapeId(String id) {
        for (ComponentKind k : values()) {
            if (k.shapeId.equals(id)) return k;
        }
        return SWORD_BLADE;
    }
}

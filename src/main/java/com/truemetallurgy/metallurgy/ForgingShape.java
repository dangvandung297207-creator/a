package com.truemetallurgy.metallurgy;

import java.util.List;

/**
 * Stage-based forging templates. Each component kind defines its shaping
 * stages; every stage needs a fixed number of effective strikes. No voxel
 * simulation - stages map to distinct models.
 */
public record ForgingShape(String id, List<Stage> stages) {
    public record Stage(String nameKey, int strikesRequired) {}

    public int totalStrikes() {
        int total = 0;
        for (Stage s : stages) total += s.strikesRequired();
        return total;
    }

    public static final ForgingShape SWORD_BLADE = new ForgingShape("sword_blade", List.of(
        new Stage("stage.true_metallurgy.blade", 4),
        new Stage("stage.true_metallurgy.edge", 4),
        new Stage("stage.true_metallurgy.tang", 3),
        new Stage("stage.true_metallurgy.finish_blade", 3)));
    public static final ForgingShape AXE_HEAD = new ForgingShape("axe_head", List.of(
        new Stage("stage.true_metallurgy.head", 4),
        new Stage("stage.true_metallurgy.edge", 4),
        new Stage("stage.true_metallurgy.eye", 3),
        new Stage("stage.true_metallurgy.finish_head", 3)));
    public static final ForgingShape PICKAXE_HEAD = new ForgingShape("pickaxe_head", List.of(
        new Stage("stage.true_metallurgy.pick_head", 4),
        new Stage("stage.true_metallurgy.reinforced_eye", 4),
        new Stage("stage.true_metallurgy.finish_head", 3)));
    public static final ForgingShape SPEAR_HEAD = new ForgingShape("spear_head", List.of(
        new Stage("stage.true_metallurgy.spear_head", 4),
        new Stage("stage.true_metallurgy.edge", 3),
        new Stage("stage.true_metallurgy.socket", 3),
        new Stage("stage.true_metallurgy.finish_head", 2)));
    /** Bloom -> billet consolidation. */
    public static final ForgingShape BILLET = new ForgingShape("billet", List.of(
        new Stage("stage.true_metallurgy.consolidate", 3),
        new Stage("stage.true_metallurgy.draw_out", 3)));

    public static ForgingShape forComponent(String componentId) {
        return switch (componentId) {
            case "axe_head" -> AXE_HEAD;
            case "pickaxe_head" -> PICKAXE_HEAD;
            case "spear_head" -> SPEAR_HEAD;
            case "billet" -> BILLET;
            default -> SWORD_BLADE;
        };
    }
}

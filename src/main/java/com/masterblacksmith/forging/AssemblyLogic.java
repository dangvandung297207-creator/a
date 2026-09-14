package com.masterblacksmith.forging;

import com.masterblacksmith.ModItems;
import com.masterblacksmith.item.BlueprintItem;
import com.masterblacksmith.item.ComponentPartItem;
import com.masterblacksmith.item.ForgedComponentItem;
import com.masterblacksmith.item.HandleItem;
import com.masterblacksmith.material.HandleMaterials;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** Turns finished components + parts into a single storied weapon or armour. */
public final class AssemblyLogic {
    public record Plan(ItemStack result, int quality, Component label, boolean valid) {
        public static Plan invalid(Component label) {
            return new Plan(ItemStack.EMPTY, 0, label, false);
        }
    }

    private AssemblyLogic() {}

    public static Plan plan(List<ItemStack> parts, ItemStack blueprint, int mode, Player player) {
        ItemStack component = ItemStack.EMPTY;
        ItemStack guard = ItemStack.EMPTY;
        ItemStack grip = ItemStack.EMPTY;
        ItemStack pommel = ItemStack.EMPTY;
        ItemStack handle = ItemStack.EMPTY;
        int leather = 0;

        for (ItemStack s : parts) {
            if (s.isEmpty()) continue;
            if (s.getItem() instanceof ForgedComponentItem c && c.isFinished()) {
                if (!component.isEmpty()) return Plan.invalid(Component.translatable("assembly.masterblacksmith.one_component"));
                component = s;
            } else if (s.getItem() instanceof ComponentPartItem p) {
                switch (p.getKind()) {
                    case GUARD -> { if (!guard.isEmpty()) return Plan.invalid(duplicate()); guard = s; }
                    case GRIP -> { if (!grip.isEmpty()) return Plan.invalid(duplicate()); grip = s; }
                    case POMMEL -> { if (!pommel.isEmpty()) return Plan.invalid(duplicate()); pommel = s; }
                }
            } else if (s.getItem() instanceof HandleItem) {
                if (!handle.isEmpty()) return Plan.invalid(duplicate());
                handle = s;
            } else if (s.is(ModItems.LEATHER_STRIP.get())) {
                leather += s.getCount();
            }
        }

        if (component.isEmpty()) {
            return Plan.invalid(Component.translatable("assembly.masterblacksmith.need_component"));
        }
        if (!ForgingData.isQuenched(component)) {
            return Plan.invalid(Component.translatable("assembly.masterblacksmith.need_quench"));
        }
        String template = ((ForgedComponentItem) component.getItem()).getTemplateId();
        boolean needsEdge = !template.equals("plate");
        if (needsEdge && !ForgingData.isGround(component)) {
            return Plan.invalid(Component.translatable("assembly.masterblacksmith.need_grind"));
        }

        String blueprintId = blueprint.getItem() instanceof BlueprintItem b ? b.getBlueprintId() : "";

        return switch (template) {
            case "sword" -> {
                if (guard.isEmpty() || grip.isEmpty() || pommel.isEmpty()) {
                    yield Plan.invalid(Component.translatable("assembly.masterblacksmith.sword_needs"));
                }
                yield build(ModItems.FORGED_SWORD.get(), "sword", component, guard, grip, pommel,
                        handle, blueprintId, "kings_edge", player);
            }
            case "axe" -> {
                if (handle.isEmpty()) yield Plan.invalid(Component.translatable("assembly.masterblacksmith.axe_needs"));
                yield build(ModItems.FORGED_AXE.get(), "axe", component, guard, grip, pommel,
                        handle, blueprintId, "oathkeeper", player);
            }
            case "pickaxe" -> {
                if (handle.isEmpty()) yield Plan.invalid(Component.translatable("assembly.masterblacksmith.pick_needs"));
                yield build(ModItems.FORGED_PICKAXE.get(), "pickaxe", component, guard, grip, pommel,
                        handle, blueprintId, "stonesplitter", player);
            }
            case "spear" -> {
                if (handle.isEmpty() || leather < 1) yield Plan.invalid(Component.translatable("assembly.masterblacksmith.spear_needs"));
                yield build(ModItems.FORGED_SPEAR.get(), "spear", component, guard, grip, pommel,
                        handle, blueprintId, "skypiercer", player);
            }
            case "plate" -> {
                if (leather < 2) yield Plan.invalid(Component.translatable("assembly.masterblacksmith.plate_needs"));
                if (mode < 1 || mode > 4) yield Plan.invalid(Component.translatable("assembly.masterblacksmith.pick_piece"));
                Item armor = switch (mode) {
                    case 1 -> ModItems.FORGED_HELMET.get();
                    case 2 -> ModItems.FORGED_CHESTPLATE.get();
                    case 3 -> ModItems.FORGED_LEGGINGS.get();
                    default -> ModItems.FORGED_BOOTS.get();
                };
                yield build(armor, "armor", component, guard, grip, pommel,
                        handle, blueprintId, "aegis", player);
            }
            default -> Plan.invalid(Component.translatable("assembly.masterblacksmith.unknown"));
        };
    }

    private static Component duplicate() {
        return Component.translatable("assembly.masterblacksmith.duplicate");
    }

    private static Plan build(Item resultItem, String type, ItemStack component,
                              ItemStack guard, ItemStack grip, ItemStack pommel, ItemStack handle,
                              String blueprintId, String wantedBlueprint, Player player) {
        String metal = ForgingData.getMetalId(component);
        float purity = ForgingData.getPurity(component);
        float forgingQ = ForgingData.getForgingQ(component);
        float quenchQ = ForgingData.getQuenchQ(component);
        float grindQ = type.equals("armor") ? 78F : ForgingData.getGrindQ(component);

        int partsBonus = 0;
        for (ItemStack s : List.of(guard, grip, pommel)) {
            if (s.getItem() instanceof ComponentPartItem p) partsBonus += p.getQualityBonus();
        }
        String handleId = "oak";
        float gripValue = 0.55F;
        if (handle.getItem() instanceof HandleItem h) {
            handleId = h.getHandleId();
            gripValue = HandleMaterials.get(handleId).getGrip();
        } else if (grip.getItem() instanceof ComponentPartItem p) {
            gripValue = 0.55F + p.getQualityBonus() / 100F;
        }
        boolean fullSet = type.equals("sword") ? (!guard.isEmpty() && !grip.isEmpty() && !pommel.isEmpty()) : !handle.isEmpty() || type.equals("armor");
        float assemblyQ = QualityCalculator.assemblyQuality(partsBonus, gripValue, fullSet);

        boolean hasBlueprint = !blueprintId.isEmpty() && blueprintId.equals(wantedBlueprint);
        int cap = ForgingData.getAnvilCap(component);
        int score = QualityCalculator.finalScore(forgingQ, quenchQ, grindQ, assemblyQ, hasBlueprint, cap);

        ItemStack result = new ItemStack(resultItem);
        List<String> history = new ArrayList<>(ForgingData.getHistory(component));
        history.add("assembled." + type);
        ItemIdentity.write(result, new ItemIdentity.IdentityData(
                player.getGameProfile().getName(), metal, purity, score,
                QualityTier.tierFor(score).getKey(), ForgingData.getQuenchId(component),
                type.equals("armor") ? 0F : ForgingData.getGrindAngle(component),
                handleId, hasBlueprint ? blueprintId : "",
                ItemIdentity.estimateWeight(type, metal, handleId), history));

        if (QualityTier.tierFor(score) == QualityTier.LEGENDARY && hasBlueprint) {
            result.setHoverName(Component.translatable("legendary.masterblacksmith." + wantedBlueprint));
        }
        Component label = Component.translatable("assembly.masterblacksmith.result",
                QualityTier.tierFor(score).getDisplayName(),
                Component.translatable(result.getDescriptionId()));
        return new Plan(result, score, label, true);
    }
}

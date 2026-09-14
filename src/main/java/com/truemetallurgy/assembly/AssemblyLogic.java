package com.truemetallurgy.assembly;

import com.truemetallurgy.components.FinishedData;
import com.truemetallurgy.components.ForgedComponentData;
import com.truemetallurgy.components.JournalData;
import com.truemetallurgy.item.BlueprintItem;
import com.truemetallurgy.item.ForgedComponentItem;
import com.truemetallurgy.item.ForgedToolHelper;
import com.truemetallurgy.item.HandleItem;
import com.truemetallurgy.metallurgy.GrindAngle;
import com.truemetallurgy.metallurgy.HandleMaterial;
import com.truemetallurgy.metallurgy.Material;
import com.truemetallurgy.metallurgy.Materials;
import com.truemetallurgy.metallurgy.Quality;
import com.truemetallurgy.metallurgy.QuenchMedium;
import com.truemetallurgy.registry.ModDataComponents;
import com.truemetallurgy.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Server-side assembly: components become a finished, uniquely-identified
 * weapon or tool. Craftsmanship is computed from the recorded history -
 * never rolled.
 */
public final class AssemblyLogic {
    private AssemblyLogic() {}

    public record Result(ItemStack output, boolean legendary, String messageKey) {}

    /** Validate + assemble. Returns null with a feedback message on failure. */
    public static Result assemble(String category, ItemStack blade, ItemStack partB, ItemStack partC,
            ItemStack partD, ItemStack blueprint, ServerPlayer player) {
        if (!(blade.getItem() instanceof ForgedComponentItem)) {
            return fail("message.true_metallurgy.assembly_no_blade");
        }
        ForgedComponentData data = ForgedComponentItem.dataOf(blade);
        if (!data.isFinished()) return fail("message.true_metallurgy.assembly_unfinished");
        if (!data.quenched()) return fail("message.true_metallurgy.assembly_unquenched");

        HandleMaterial handle;
        float bonus = 0.0F;
        switch (category) {
            case "sword" -> {
                if (!isGuard(partB)) return fail("message.true_metallurgy.assembly_no_guard");
                handle = handleOf(partC);
                if (handle == null) return fail("message.true_metallurgy.assembly_no_handle");
                if (!isPommel(partD)) return fail("message.true_metallurgy.assembly_no_pommel");
                if (data.materialId().equals(guardMaterial(partB))) bonus += 2.0F;
                if ("steel".equals(guardMaterial(partB)) || "steel".equals(pommelMaterial(partD))) bonus += 1.0F;
            }
            case "axe", "pickaxe" -> {
                handle = handleOf(partB);
                if (handle == null) return fail("message.true_metallurgy.assembly_no_handle");
            }
            case "spear" -> {
                if (!partB.is(ModItems.LONG_SHAFT.get())) return fail("message.true_metallurgy.assembly_no_shaft");
                if (!partC.is(ModItems.BINDING.get())) return fail("message.true_metallurgy.assembly_no_binding");
                handle = HandleMaterial.OAK;
            }
            default -> {
                return fail("message.true_metallurgy.assembly_unknown");
            }
        }

        Material mat = Materials.get(data.materialId());
        GrindAngle angle = data.grindAngle() == 15 ? GrindAngle.KEEN
            : data.grindAngle() == 35 ? GrindAngle.STURDY : GrindAngle.BALANCED;
        QuenchMedium quench = QuenchMedium.get(data.quenchId());

        float score = data.score() * 0.7F + 30.0F * 0.3F + bonus;
        boolean hasKingsEdge = blueprint.getItem() instanceof BlueprintItem bp && "kings_edge".equals(bp.getDesignId());
        String design = "";
        if (hasKingsEdge) {
            score += 5.0F;
            design = "kings_edge";
        }
        int finalScore = Quality.clampScore(score);
        boolean legendary = finalScore >= 90 && hasKingsEdge;

        String crafter = data.crafterName().isEmpty() ? player.getGameProfile().getName() : data.crafterName();
        String crafterId = data.crafterId().isEmpty() ? player.getStringUUID() : data.crafterId();
        float weight = mat.density() * handle.weightMult + ("sword".equals(category) ? 0.4F : 0.2F);
        FinishedData finished = new FinishedData(
            mat.id(), data.kind().replace("_blade", "").replace("_head", ""),
            finalScore, data.purity(), data.quenchId(), data.grindAngle(), data.grindQuality(),
            crafter, crafterId, legendary || finalScore >= 70,
            legendary ? "The King's Edge" : "",
            design, weight);

        String toolKind = switch (data.kind()) {
            case "axe_head" -> "axe";
            case "pickaxe_head" -> "pickaxe";
            case "spear_head" -> "spear";
            default -> "sword";
        };
        Item tool = ModItems.finishedTool(mat.id(), toolKind);
        ItemStack output = ForgedToolHelper.assemble(tool, finished, handle, data.grindAngle() > 0 ? angle : null, quench);
        recordInJournal(player, output, finished);
        return new Result(output, legendary,
            legendary ? "message.true_metallurgy.legendary_forged" : "message.true_metallurgy.assembled");
    }

    private static Result fail(String key) {
        return new Result(ItemStack.EMPTY, false, key);
    }

    private static boolean isGuard(ItemStack stack) {
        return stack.is(ModItems.IRON_GUARD.get()) || stack.is(ModItems.STEEL_GUARD.get());
    }

    private static boolean isPommel(ItemStack stack) {
        return stack.is(ModItems.IRON_POMMEL.get()) || stack.is(ModItems.STEEL_POMMEL.get());
    }

    private static String guardMaterial(ItemStack stack) {
        if (stack.is(ModItems.STEEL_GUARD.get())) return "steel";
        if (stack.is(ModItems.IRON_GUARD.get())) return "iron";
        return "";
    }

    private static String pommelMaterial(ItemStack stack) {
        if (stack.is(ModItems.STEEL_POMMEL.get())) return "steel";
        if (stack.is(ModItems.IRON_POMMEL.get())) return "iron";
        return "";
    }

    private static HandleMaterial handleOf(ItemStack stack) {
        if (stack.getItem() instanceof HandleItem handle) return handle.getMaterial();
        if (stack.is(ModItems.LONG_SHAFT.get())) return HandleMaterial.OAK;
        return null;
    }

    /** Record the creation in the assembler's journal, if carried. */
    private static void recordInJournal(ServerPlayer player, ItemStack output, FinishedData data) {
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.is(ModItems.JOURNAL.get())) {
                JournalData old = stack.get(ModDataComponents.JOURNAL.get());
                if (old == null) old = JournalData.EMPTY;
                List<String> discovered = new ArrayList<>(old.discovered());
                if (!discovered.contains(data.materialId())) discovered.add(data.materialId());
                List<JournalData.Creation> creations = new ArrayList<>(old.creations());
                String name = output.getHoverName().getString();
                creations.add(0, new JournalData.Creation(name, data.score(),
                    data.materialId() + "|" + data.toolKind() + "|" + data.quenchId()));
                while (creations.size() > 12) creations.remove(creations.size() - 1);
                int best = old.bestScore();
                String bestName = old.bestName();
                if (data.score() > best) {
                    best = data.score();
                    bestName = name;
                }
                stack.set(ModDataComponents.JOURNAL.get(), new JournalData(discovered, best, bestName, creations));
                player.displayClientMessage(Component.translatable("message.true_metallurgy.journal_recorded"), true);
                return;
            }
        }
    }
}

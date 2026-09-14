package com.masterblacksmith.forging;

import com.masterblacksmith.material.MetalMaterials;
import com.masterblacksmith.material.QuenchLiquids;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** The birth certificate of a finished piece: crafter, steel, score, history. */
public final class ItemIdentity {
    public static final String TAG = "MBSIdentity";

    public record IdentityData(String crafter, String metal, float purity, int score, String tier,
                               String quench, float edge, String handle, String blueprint,
                               float weight, List<String> history) {
        public static IdentityData empty() {
            return new IdentityData("?", "iron", 0F, 0, "flawed", "water", 25F, "oak", "", 0F, List.of());
        }
    }

    private ItemIdentity() {}

    public static void write(ItemStack stack, IdentityData data) {
        CompoundTag t = new CompoundTag();
        t.putString("Crafter", data.crafter());
        t.putString("Metal", data.metal());
        t.putFloat("Purity", data.purity());
        t.putInt("Score", data.score());
        t.putString("Tier", data.tier());
        t.putString("Quench", data.quench());
        t.putFloat("Edge", data.edge());
        t.putString("Handle", data.handle());
        t.putString("Blueprint", data.blueprint());
        t.putFloat("Weight", data.weight());
        ListTag history = new ListTag();
        for (String s : data.history()) history.add(StringTag.valueOf(s));
        t.put("History", history);
        stack.getOrCreateTag().put(TAG, t);
    }

    public static IdentityData read(ItemStack stack) {
        CompoundTag root = stack.getTag();
        if (root == null || !root.contains(TAG)) return IdentityData.empty();
        CompoundTag t = root.getCompound(TAG);
        List<String> history = new ArrayList<>();
        if (t.contains("History")) {
            for (var e : t.getList("History", 8)) history.add(e.getAsString());
        }
        return new IdentityData(
                t.getString("Crafter"), t.getString("Metal"), t.getFloat("Purity"),
                t.getInt("Score"), t.getString("Tier"), t.getString("Quench"),
                t.getFloat("Edge"), t.getString("Handle"), t.getString("Blueprint"),
                t.getFloat("Weight"), history);
    }

    public static boolean hasIdentity(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains(TAG);
    }

    /** Lore lines shown under a finished piece. */
    public static List<Component> lore(ItemStack stack) {
        IdentityData d = read(stack);
        List<Component> out = new ArrayList<>();
        QualityTier tier = QualityTier.tierFor(d.score());
        out.add(tier.getDisplayName());
        out.add(Component.translatable("lore.masterblacksmith.forged_by", d.crafter()).withStyle(ChatFormatting.GRAY));
        out.add(Component.translatable("lore.masterblacksmith.steel_purity",
                MetalMaterials.get(d.metal()).getDisplayName(),
                String.format("%.1f%%", d.purity())).withStyle(ChatFormatting.GRAY));
        out.add(Component.translatable("lore.masterblacksmith.craftsmanship",
                d.score(), QualityTier.tierFor(d.score()).getDisplayName()).withStyle(ChatFormatting.GRAY));
        out.add(Component.translatable("lore.masterblacksmith.quenched_in",
                QuenchLiquids.get(d.quench()).getDisplayName()).withStyle(ChatFormatting.GRAY));
        out.add(Component.translatable("lore.masterblacksmith.edge",
                String.format("%.0f", d.edge())).withStyle(ChatFormatting.GRAY));
        out.add(Component.translatable("lore.masterblacksmith.weight",
                String.format("%.1fkg", d.weight())).withStyle(ChatFormatting.GRAY));
        if (!d.blueprint().isEmpty()) {
            out.add(Component.translatable("blueprint.masterblacksmith." + d.blueprint())
                    .withStyle(ChatFormatting.DARK_PURPLE));
        }
        return out;
    }

    public static float estimateWeight(String weaponType, String metalId, String handleId) {
        float base = switch (weaponType) {
            case "sword" -> 1.6F;
            case "axe" -> 2.1F;
            case "pickaxe" -> 2.6F;
            case "spear" -> 1.9F;
            default -> 2.0F;
        };
        float density = MetalMaterials.get(metalId).getDensity();
        return base * (density / 7.85F) + 0.8F;
    }
}

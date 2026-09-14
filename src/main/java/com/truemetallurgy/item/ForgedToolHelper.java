package com.truemetallurgy.item;

import com.truemetallurgy.components.FinishedData;
import com.truemetallurgy.metallurgy.GrindAngle;
import com.truemetallurgy.metallurgy.HandleMaterial;
import com.truemetallurgy.metallurgy.Material;
import com.truemetallurgy.metallurgy.Materials;
import com.truemetallurgy.metallurgy.Quality;
import com.truemetallurgy.metallurgy.QuenchMedium;
import com.truemetallurgy.registry.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Shared logic for finished weapons/tools: stat computation at assembly and
 * the medieval identity tooltip.
 */
public final class ForgedToolHelper {
    private ForgedToolHelper() {}

    public static Tiers vanillaTier(Material material) {
        return switch (material.id()) {
            case "copper" -> Tiers.STONE;
            case "steel" -> Tiers.DIAMOND;
            case "hardened_steel" -> Tiers.NETHERITE;
            default -> Tiers.IRON;
        };
    }

    public static float baseAttackSpeed(String toolKind) {
        return switch (toolKind) {
            case "axe" -> -3.0F;
            case "pickaxe" -> -2.8F;
            case "spear" -> -2.7F;
            default -> -2.4F;
        };
    }

    public static float kindDamageBonus(String toolKind) {
        return switch (toolKind) {
            case "axe" -> 2.0F;
            case "pickaxe" -> -2.0F;
            case "spear" -> 1.0F;
            default -> 0.0F;
        };
    }

    /** Apply assembly results: attributes, durability and display name. */
    public static ItemStack assemble(Item tool, FinishedData data, HandleMaterial handle, GrindAngle angle, QuenchMedium quench) {
        Material mat = Materials.get(data.materialId());
        Tiers tier = vanillaTier(mat);
        float damage = Math.max(1.0F, (mat.baseDamage() + kindDamageBonus(data.toolKind()))
            * mat.sharpness()
            * Quality.performanceMult(data.score())
            * quench.hardnessMult()
            * (angle != null ? angle.sharpnessMult() : 1.0F));
        float speed = baseAttackSpeed(data.toolKind()) + (handle.recoveryMult - 1.0F) * 0.4F;
        int maxDamage = Math.max(10, Math.round(mat.durability()
            * Quality.durabilityMult(data.score())
            * quench.toughnessMult()
            * (angle != null ? angle.durabilityMult() : 1.0F)
            * handle.durabilityMult));
        float damageParam = damage - tier.getAttackDamageBonus();

        ItemStack stack = new ItemStack(tool);
        if (tool instanceof AxeItem) {
            stack.set(DataComponents.ATTRIBUTE_MODIFIERS, AxeItem.createAttributes(tier, damageParam, speed));
        } else if (tool instanceof PickaxeItem) {
            stack.set(DataComponents.ATTRIBUTE_MODIFIERS, PickaxeItem.createAttributes(tier, damageParam, speed));
        } else {
            stack.set(DataComponents.ATTRIBUTE_MODIFIERS, SwordItem.createAttributes(tier, damageParam, speed));
        }
        stack.set(DataComponents.MAX_DAMAGE, maxDamage);
        stack.set(ModDataComponents.FINISHED.get(), data);
        stack.set(DataComponents.CUSTOM_NAME, displayName(data, mat));
        return stack;
    }

    public static Component displayName(FinishedData data, Material mat) {
        if (!data.legendaryName().isEmpty()) {
            return Component.literal(data.legendaryName()).withStyle(ChatFormatting.GOLD);
        }
        Quality.Tier tier = Quality.Tier.fromScore(data.score());
        String toolKey = "tool.true_metallurgy." + data.toolKind();
        return Component.translatable("item.true_metallurgy.forged_name",
            Component.translatable(tier.langKey()),
            Component.translatable("material.true_metallurgy." + mat.id()),
            Component.translatable(toolKey));
    }

    public static FinishedData dataOf(ItemStack stack) {
        FinishedData data = stack.get(ModDataComponents.FINISHED.get());
        return data != null ? data : FinishedData.EMPTY;
    }

    /** Medieval identity tooltip. Compact by default, full stats when advanced. */
    public static void appendFinishedTooltip(ItemStack stack, List<Component> lines, TooltipFlag flag, Material fallback) {
        FinishedData data = stack.get(ModDataComponents.FINISHED.get());
        if (data == null) {
            lines.add(Component.translatable("tooltip.true_metallurgy.unforged").withStyle(ChatFormatting.DARK_GRAY));
            return;
        }
        Material mat = Materials.get(data.materialId());
        Quality.Tier tier = Quality.Tier.fromScore(data.score());
        ChatFormatting tierColor = switch (tier) {
            case FLAWED -> ChatFormatting.GRAY;
            case STANDARD -> ChatFormatting.WHITE;
            case MASTERWORK -> ChatFormatting.AQUA;
            case LEGENDARY -> ChatFormatting.GOLD;
        };
        lines.add(Component.translatable(tier.langKey()).withStyle(tierColor));
        if (!data.crafterName().isEmpty()) {
            lines.add(Component.translatable("tooltip.true_metallurgy.forged_by", data.crafterName())
                .withStyle(s -> s.withColor(0xD8B25C).withItalic(true)));
        }
        lines.add(Component.translatable("tooltip.true_metallurgy.purity_craft",
            String.format("%.1f", data.purity()), data.score()).withStyle(ChatFormatting.GRAY));
        if (!data.quenchId().equals("none")) {
            lines.add(Component.translatable("tooltip.true_metallurgy.quenched",
                Component.translatable("quench.true_metallurgy." + data.quenchId())).withStyle(ChatFormatting.BLUE));
        }
        if (data.grindAngle() > 0) {
            lines.add(Component.translatable("tooltip.true_metallurgy.edge", data.grindAngle())
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        }
        lines.add(Component.translatable("tooltip.true_metallurgy.weight", String.format("%.1f", data.weightKg()))
            .withStyle(ChatFormatting.DARK_GRAY));
        if (data.makerMark()) {
            lines.add(Component.translatable("tooltip.true_metallurgy.maker_mark").withStyle(ChatFormatting.GOLD));
        }
        if (flag.isAdvanced()) {
            lines.add(Component.translatable("tooltip.true_metallurgy.material_stats",
                String.format("%.1f", mat.hardness()), String.format("%.1f", mat.toughness()),
                String.format("%.1f", mat.sharpness()), String.format("%.1f", mat.flexibility()))
                .withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}

package com.masterblacksmith.forging;

import com.masterblacksmith.material.HandleMaterials;
import com.masterblacksmith.material.MetalMaterials;
import com.masterblacksmith.material.QuenchLiquids;
import net.minecraft.world.item.ItemStack;

/** Final combat stats derived from the metallurgy profile + craftsmanship. */
public final class ForgedStats {
    private ForgedStats() {}

    public static float attackDamage(ItemStack weapon, String weaponType) {
        ItemIdentity.IdentityData d = ItemIdentity.read(weapon);
        float typeBonus = switch (weaponType) {
            case "sword" -> 1.0F;
            case "axe" -> 2.0F;
            case "spear" -> 0.0F;
            case "pickaxe" -> -1.0F;
            default -> 0F;
        };
        float base = MetalMaterials.get(d.metal()).getBaseDamage() + typeBonus;
        float tier = QualityTier.tierFor(d.score()).getPerformanceMult();
        float quench = (QuenchLiquids.get(d.quench()).getSharpnessMod() + QuenchLiquids.get(d.quench()).getHardnessMod()) / 2F;
        float grind = d.edge() > 0 ? GrindingProfile.sharpnessMult(d.edge()) : 1F;
        float purity = 0.85F + (d.purity() / 100F) * 0.3F;
        return Math.max(1F, base * tier * quench * grind * purity);
    }

    public static double attackSpeed(ItemStack weapon, String weaponType, double base) {
        ItemIdentity.IdentityData d = ItemIdentity.read(weapon);
        double speed = base + HandleMaterials.get(d.handle()).getSpeedBonus();
        float density = MetalMaterials.get(d.metal()).getDensity();
        speed -= (density - 7.85F) * 0.02;
        if (QualityTier.tierFor(d.score()) == QualityTier.LEGENDARY) speed += 0.1;
        return speed;
    }

    public static int maxDurability(ItemStack weapon) {
        ItemIdentity.IdentityData d = ItemIdentity.read(weapon);
        int base = MetalMaterials.get(d.metal()).getBaseDurability();
        float tier = QualityTier.tierFor(d.score()).getDurabilityMult();
        float quench = QuenchLiquids.get(d.quench()).getDurabilityMod();
        float grind = d.edge() > 0 ? GrindingProfile.durabilityMult(d.edge()) : 1F;
        float handle = HandleMaterials.get(d.handle()).getDurabilityMod();
        return Math.max(10, Math.round(base * tier * quench * grind * handle));
    }

    /** Stock stats for un-assembled creative-tab pieces. */
    public static float stockDamage(String weaponType) {
        return switch (weaponType) {
            case "sword" -> 5F;
            case "axe" -> 7F;
            case "spear" -> 4F;
            case "pickaxe" -> 3F;
            default -> 4F;
        };
    }
}

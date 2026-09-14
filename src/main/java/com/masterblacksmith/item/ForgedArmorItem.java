package com.masterblacksmith.item;

import com.masterblacksmith.forging.ItemIdentity;
import com.masterblacksmith.forging.QualityTier;
import com.masterblacksmith.material.QuenchLiquids;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** A finished armour piece carrying its forging history. */
public class ForgedArmorItem extends ArmorItem {
    public ForgedArmorItem(ArmorMaterial material, Type type) {
        super(material, type, new Properties());
    }

    /** Dynamic durability via the Forge hook when present; harmless otherwise. */
    public int getMaxDamage(ItemStack stack) {
        if (!ItemIdentity.hasIdentity(stack)) return getMaterial().getDurabilityForType(getType());
        var d = ItemIdentity.read(stack);
        int base = getMaterial().getDurabilityForType(getType());
        float mult = QualityTier.tierFor(d.score()).getDurabilityMult()
                * QuenchLiquids.get(d.quench()).getDurabilityMod()
                * (0.8F + d.score() / 500F);
        return Math.max(10, Math.round(base * mult));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, level, lines, flag);
        if (ItemIdentity.hasIdentity(stack)) {
            lines.addAll(ItemIdentity.lore(stack));
        }
    }
}

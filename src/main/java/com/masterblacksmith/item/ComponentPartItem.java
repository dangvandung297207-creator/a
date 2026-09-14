package com.masterblacksmith.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Guards, grips and pommels. Fine parts lift the assembly score. */
public class ComponentPartItem extends Item {
    public enum PartKind { GUARD, GRIP, POMMEL }

    private final PartKind kind;
    private final String materialName;
    private final int qualityBonus;

    public ComponentPartItem(PartKind kind, String materialName, int qualityBonus) {
        super(new Properties());
        this.kind = kind;
        this.materialName = materialName;
        this.qualityBonus = qualityBonus;
    }

    public PartKind getKind() { return kind; }
    public String getMaterialName() { return materialName; }
    public int getQualityBonus() { return qualityBonus; }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, level, lines, flag);
        lines.add(Component.translatable("part.masterblacksmith." + kind.name().toLowerCase())
                .withStyle(ChatFormatting.GRAY));
        if (qualityBonus > 0) {
            lines.add(Component.translatable("tooltip.masterblacksmith.parts_bonus",
                    "+" + qualityBonus).withStyle(ChatFormatting.GREEN));
        }
    }
}

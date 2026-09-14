package com.masterblacksmith.item;

import com.masterblacksmith.forging.ForgingData;
import com.masterblacksmith.forging.ForgingTemplate;
import com.masterblacksmith.forging.ForgingTemplates;
import com.masterblacksmith.forging.HeatZone;
import com.masterblacksmith.forging.HeatingHelper;
import com.masterblacksmith.forging.QualityCalculator;
import com.masterblacksmith.material.MetalMaterials;
import com.masterblacksmith.material.QuenchLiquids;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Unfinished blanks and finished (quench-ready) heads, blades and plates. */
public class ForgedComponentItem extends Item {
    private final String templateId;
    private final boolean finished;

    public ForgedComponentItem(String templateId, boolean finished) {
        super(new Properties().stacksTo(1));
        this.templateId = templateId;
        this.finished = finished;
    }

    public String getTemplateId() { return templateId; }
    public boolean isFinished() { return finished; }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return ForgingData.getTemp(stack) > 300;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.min(13, Math.round(ForgingData.getTemp(stack) / 1600F * 13F));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        int glow = HeatingHelper.glowColorForTemp(ForgingData.getTemp(stack));
        return glow == 0 ? 0xFF5500 : glow;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, level, lines, flag);
        String metal = ForgingData.getMetalId(stack);
        float temp = ForgingData.getTemp(stack);
        lines.add(MetalMaterials.get(metal).getDisplayName().copy().withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable("tooltip.masterblacksmith.temperature",
                String.format("%.0f", temp),
                HeatZone.zoneFor(ForgingData.getMetal(stack), temp).getDisplayName())
                .withStyle(ChatFormatting.GOLD));
        if (!finished) {
            ForgingTemplate t = ForgingTemplates.get(templateId);
            int stage = Math.min(ForgingData.getStage(stack), t.stageCount() - 1);
            lines.add(Component.translatable("tooltip.masterblacksmith.stage",
                    t.getStages().get(stage).displayName(),
                    stage + 1, t.stageCount()).withStyle(ChatFormatting.YELLOW));
            lines.add(Component.translatable("tooltip.masterblacksmith.forging_q",
                    Math.round(QualityCalculator.componentScore(stack))).withStyle(ChatFormatting.GRAY));
        } else {
            if (ForgingData.isQuenched(stack)) {
                lines.add(Component.translatable("lore.masterblacksmith.quenched_in",
                        QuenchLiquids.get(ForgingData.getQuenchId(stack)).getDisplayName())
                        .withStyle(ChatFormatting.AQUA));
            } else {
                lines.add(Component.translatable("tooltip.masterblacksmith.needs_quench").withStyle(ChatFormatting.RED));
            }
            if (ForgingData.isGround(stack)) {
                lines.add(Component.translatable("lore.masterblacksmith.edge",
                        String.format("%.0f", ForgingData.getGrindAngle(stack))).withStyle(ChatFormatting.GRAY));
            } else if (!templateId.equals("plate")) {
                lines.add(Component.translatable("tooltip.masterblacksmith.needs_grind").withStyle(ChatFormatting.RED));
            }
        }
        lines.add(Component.translatable("tooltip.masterblacksmith.purity",
                String.format("%.1f%%", ForgingData.getPurity(stack))).withStyle(ChatFormatting.DARK_GRAY));
    }
}

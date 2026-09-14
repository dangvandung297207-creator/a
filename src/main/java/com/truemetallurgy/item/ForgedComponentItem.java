package com.truemetallurgy.item;

import com.truemetallurgy.components.ForgedComponentData;
import com.truemetallurgy.forging.HotMetal;
import com.truemetallurgy.metallurgy.ForgingShape;
import com.truemetallurgy.metallurgy.Heat;
import com.truemetallurgy.metallurgy.Materials;
import com.truemetallurgy.registry.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/** A blade/head in progress. Unique forging history, hot-capable. */
public class ForgedComponentItem extends Item {
    private final ComponentKind kind;

    public ForgedComponentItem(ComponentKind kind) {
        super(new Properties().stacksTo(1));
        this.kind = kind;
    }

    public ComponentKind getKind() {
        return kind;
    }

    public static ForgedComponentData dataOf(ItemStack stack) {
        ForgedComponentData data = stack.get(ModDataComponents.COMPONENT.get());
        return data != null ? data : ForgedComponentData.EMPTY;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (!level.isClientSide && entity.tickCount % 20 == 0) {
            HotMetal.inventoryCool(stack);
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return HotMetal.temperatureOf(stack) >= Heat.FORGE_MIN;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, lines, flag);
        ForgedComponentData data = dataOf(stack);
        ForgingShape shape = ForgingShape.forComponent(kind.shapeId);
        lines.add(Component.translatable("tooltip.true_metallurgy.material",
            Component.translatable("material.true_metallurgy." + data.materialId())).withStyle(ChatFormatting.GRAY));
        if (!data.isFinished()) {
            int stage = Math.min(data.stage(), shape.stages().size() - 1);
            lines.add(Component.translatable("tooltip.true_metallurgy.stage",
                Component.translatable(shape.stages().get(Math.max(0, stage)).nameKey()),
                data.stageProgress(), shape.stages().get(Math.max(0, stage)).strikesRequired())
                .withStyle(ChatFormatting.YELLOW));
        } else {
            lines.add(Component.translatable("tooltip.true_metallurgy.stage_done").withStyle(ChatFormatting.GREEN));
        }
        Heat.Zone zone = Heat.Zone.fromTemp(data.temperature());
        lines.add(Component.translatable("tooltip.true_metallurgy.temperature", data.temperature(),
            Component.translatable("heat.true_metallurgy." + zone.name().toLowerCase()))
            .withStyle(zone == Heat.Zone.COLD ? ChatFormatting.GRAY : ChatFormatting.GOLD));
        lines.add(Component.translatable("tooltip.true_metallurgy.quality_progress", String.format("%.0f", data.score()))
            .withStyle(ChatFormatting.AQUA));
        if (data.quenched()) {
            lines.add(Component.translatable("tooltip.true_metallurgy.quenched",
                Component.translatable("quench.true_metallurgy." + data.quenchId())).withStyle(ChatFormatting.BLUE));
        }
        if (data.grindAngle() > 0) {
            lines.add(Component.translatable("tooltip.true_metallurgy.edge", data.grindAngle())
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        }
        if (flag.isAdvanced()) {
            lines.add(Component.translatable("tooltip.true_metallurgy.strikes",
                data.strikes(), data.perfects(), data.goods(), data.misses(), data.bads(), data.reheats())
                .withStyle(ChatFormatting.DARK_GRAY));
            lines.add(Component.translatable("tooltip.true_metallurgy.purity", String.format("%.1f", data.purity()))
                .withStyle(ChatFormatting.DARK_GRAY));
            if (!data.crafterName().isEmpty()) {
                lines.add(Component.translatable("tooltip.true_metallurgy.forged_by", data.crafterName())
                    .withStyle(ChatFormatting.DARK_GRAY));
            }
        }
        Materials.get(data.materialId());
    }
}

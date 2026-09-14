package com.truemetallurgy.screen;

import com.truemetallurgy.menu.ForgeHearthMenu;
import com.truemetallurgy.metallurgy.Heat;
import com.truemetallurgy.util.TMUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

/** Medieval forge interface: temperature, fuel, airflow, alloying. */
public class ForgeHearthScreen extends AbstractContainerScreen<ForgeHearthMenu> {
    private static final ResourceLocation TEXTURE = TMUtil.rl("textures/gui/forge.png");

    private float displayedTemp = 20.0F;

    public ForgeHearthScreen(ForgeHearthMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 186;
        this.inventoryLabelY = 93;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        displayedTemp += (menu.temperature() - displayedTemp) * 0.15F;
        super.render(g, mouseX, mouseY, partialTick);
        // Hover tooltips for the bars.
        if (isHovering(152, 20, 8, 62, mouseX, mouseY)) {
            g.renderTooltip(font,
                Component.translatable("tooltip.true_metallurgy.temperature",
                    menu.temperature(), Component.translatable("heat.true_metallurgy." + zoneKey())),
                mouseX, mouseY);
        } else if (isHovering(44, 76, 18, 5, mouseX, mouseY)) {
            g.renderTooltip(font, Component.translatable("tooltip.true_metallurgy.fuel", menu.fuel()), mouseX, mouseY);
        } else if (isHovering(116, 76, 18, 5, mouseX, mouseY)) {
            g.renderTooltip(font, Component.translatable("tooltip.true_metallurgy.airflow", menu.airflow()), mouseX, mouseY);
        }
    }

    private String zoneKey() {
        return Heat.Zone.fromTemp(menu.temperature()).name().toLowerCase();
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        g.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        // Temperature bar (vertical).
        float frac = Math.min(1.0F, displayedTemp / Math.max(1, menu.maxTemp()));
        int h = Math.round(frac * 60.0F);
        int color = displayedTemp < 300 ? 0xFF6A6A6A
            : displayedTemp < 700 ? 0xFFB03020
            : displayedTemp < 1000 ? 0xFFFF7A1A
            : displayedTemp < 1200 ? 0xFFFFC93A : 0xFFFFF2B0;
        g.fill(leftPos + 152, topPos + 20 + (60 - h), leftPos + 160, topPos + 80, 0xFF1A1210);
        g.fill(leftPos + 152, topPos + 20 + (60 - h), leftPos + 160, topPos + 80, color);
        // Fuel bar.
        int fw = Math.round(menu.fuel() / (float) menu.maxFuel() * 18.0F);
        g.fill(leftPos + 44, topPos + 76, leftPos + 62, topPos + 81, 0xFF1A1210);
        g.fill(leftPos + 44, topPos + 76, leftPos + 44 + fw, topPos + 81, 0xFF8A5A20);
        // Airflow bar.
        int aw = Math.round(menu.airflow() / 100.0F * 18.0F);
        g.fill(leftPos + 116, topPos + 76, leftPos + 134, topPos + 81, 0xFF1A1210);
        g.fill(leftPos + 116, topPos + 76, leftPos + 116 + aw, topPos + 81, 0xFF7AB8D8);
        // Alloy arrow.
        int pw = Math.round(menu.alloyProgress() / (float) menu.alloyTime() * 9.0F);
        g.fill(leftPos + 132, topPos + 46, leftPos + 141, topPos + 63, 0xFF1A1210);
        g.fill(leftPos + 132, topPos + 46, leftPos + 132 + pw, topPos + 63, 0xFFD8B25C);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(font, title, 8, 6, 0xE8DCC0, false);
        g.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 0x9A8A6A, false);
        g.drawString(font, menu.temperature() + "C", 8, 20, 0xFFC93A, false);
        Heat.ForgeState state = Heat.ForgeState.fromTemp(menu.temperature());
        g.drawString(font, Component.translatable("forge.true_metallurgy." + state.name().toLowerCase()), 8, 30, 0x9A8A6A, false);
    }
}

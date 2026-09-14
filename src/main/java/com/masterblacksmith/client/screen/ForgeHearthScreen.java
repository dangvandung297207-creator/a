package com.masterblacksmith.client.screen;

import com.masterblacksmith.MasterBlacksmith;
import com.masterblacksmith.forging.HeatingHelper;
import com.masterblacksmith.menu.ForgeHearthMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/** Hearth management: forge heat, fuel flame and billet soak. */
public class ForgeHearthScreen extends AbstractContainerScreen<ForgeHearthMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MasterBlacksmith.MOD_ID, "textures/gui/forge_hearth.png");

    public ForgeHearthScreen(ForgeHearthMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics g, float partial, int mouseX, int mouseY) {
        g.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        // Forge temperature bar.
        int temp = menu.forgeTemp();
        int w = (int) (148F * Math.min(1F, temp / 1650F));
        g.fill(leftPos + 14, topPos + 62, leftPos + 162, topPos + 70, 0xFF1A1210);
        int glow = HeatingHelper.glowColorForTemp(temp);
        if (w > 0) g.fill(leftPos + 14, topPos + 62, leftPos + 14 + w, topPos + 70, 0xFF000000 | glow);
        // Fuel flame.
        int burn = menu.burnTime();
        int max = Math.max(1, menu.maxBurn());
        int fh = (int) (14F * Math.min(1F, (float) burn / max));
        if (fh > 0) g.fill(leftPos + 57, topPos + 36 + (14 - fh), leftPos + 70, topPos + 50, 0xFFFF7A1A);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(font, title, 8, 6, 0xE8D8B8, false);
        g.drawString(font, Component.translatable("gui.masterblacksmith.forge_temp", menu.forgeTemp()),
                14, 20, 0xFFC8874A, false);
        g.drawString(font, Component.translatable("gui.masterblacksmith.heat_state", heatName()),
                14, 30, 0xC8A878, false);
        g.drawString(font, Component.translatable("gui.masterblacksmith.billet_temp", menu.billetTemp()),
                88, 20, 0xFFC8874A, false);
        g.drawString(font, playerInventoryTitle, 8, imageHeight - 94, 0xE8D8B8, false);
    }

    private Component heatName() {
        return Component.translatable("heat_state.masterblacksmith." + menu.heatState());
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        renderBackground(g);
        super.render(g, mouseX, mouseY, partial);
        renderTooltip(g, mouseX, mouseY);
    }
}

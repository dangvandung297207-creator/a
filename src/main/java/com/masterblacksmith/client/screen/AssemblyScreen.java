package com.masterblacksmith.client.screen;

import com.masterblacksmith.MasterBlacksmith;
import com.masterblacksmith.menu.AssemblyMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/** Assembly bench: parts, armour-piece mode and live quality prediction. */
public class AssemblyScreen extends AbstractContainerScreen<AssemblyMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MasterBlacksmith.MOD_ID, "textures/gui/assembly.png");

    public AssemblyScreen(AssemblyMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 186;
    }

    @Override
    protected void init() {
        super.init();
        String[] labels = {"Auto", "Helm", "Chest", "Legs", "Boots"};
        for (int i = 0; i < 5; i++) {
            final int id = 10 + i;
            addRenderableWidget(Button.builder(Component.literal(labels[i]), b -> sendMode(id))
                    .bounds(leftPos + 8 + i * 33, topPos + 62, 31, 16).build());
        }
    }

    private void sendMode(int id) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
        }
    }

    @Override
    protected void renderBg(GuiGraphics g, float partial, int mouseX, int mouseY) {
        g.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int q = menu.predictedQuality();
        int w = (int) (120F * q / 100F);
        g.fill(leftPos + 28, topPos + 78, leftPos + 148, topPos + 84, 0xFF1A1210);
        int color = q >= 90 ? 0xFFC86AFF : q >= 70 ? 0xFFFFAA2A : q >= 40 ? 0xFF8A8A8A : 0xFF6A2A2A;
        if (w > 0) g.fill(leftPos + 28, topPos + 78, leftPos + 28 + w, topPos + 84, color);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(font, title, 8, 6, 0xE8D8B8, false);
        g.drawString(font, Component.translatable("gui.masterblacksmith.predicted", menu.predictedQuality()),
                28, 88, 0xFFD86A, false);
        var plan = menu.plan();
        if (!plan.label().getString().isEmpty()) {
            g.drawString(font, plan.label(), 8, 52, plan.valid() ? 0x9AE89A : 0xC87878, false);
        }
        g.drawString(font, playerInventoryTitle, 8, imageHeight - 94, 0xE8D8B8, false);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        renderBackground(g);
        super.render(g, mouseX, mouseY, partial);
        renderTooltip(g, mouseX, mouseY);
    }
}

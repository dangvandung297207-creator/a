package com.truemetallurgy.screen;

import com.truemetallurgy.menu.GrindingMenu;
import com.truemetallurgy.network.ModNetworking;
import com.truemetallurgy.util.TMUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

/** Grinding interface: edge angle select, wheel wear, grind button. */
public class GrindingScreen extends AbstractContainerScreen<GrindingMenu> {
    private static final ResourceLocation TEXTURE = TMUtil.rl("textures/gui/grind.png");
    private static final int[] ANGLES = {15, 25, 35};

    private Button grindButton;

    public GrindingScreen(GrindingMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 186;
        this.inventoryLabelY = 93;
    }

    @Override
    protected void init() {
        super.init();
        for (int i = 0; i < 3; i++) {
            final int angle = i;
            addRenderableWidget(Button.builder(Component.literal(ANGLES[i] + "°"),
                btn -> PacketDistributor.sendToServer(new ModNetworking.GrindActionPayload(menu.pos(), 0, angle)))
                .bounds(leftPos + 8 + i * 40, topPos + 62, 38, 18)
                .build());
        }
        grindButton = Button.builder(Component.translatable("gui.true_metallurgy.grind"),
            btn -> PacketDistributor.sendToServer(new ModNetworking.GrindActionPayload(menu.pos(), 1, 0)))
            .bounds(leftPos + 128, topPos + 62, 40, 18)
            .build();
        addRenderableWidget(grindButton);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        if (grindButton != null) grindButton.active = menu.canGrind();
        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        g.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        // Grinding progress.
        float frac = 1.0F - menu.progress() / (float) menu.max();
        int pw = menu.progress() > 0 ? (int) (52 * frac) : 0;
        g.fill(leftPos + 104, topPos + 35, leftPos + 156, topPos + 41, 0xFF1A1210);
        g.fill(leftPos + 104, topPos + 35, leftPos + 104 + pw, topPos + 41, 0xFFD8B25C);
        // Wheel wear.
        int ww = (int) (52 * menu.wear() / 100.0F);
        g.fill(leftPos + 104, topPos + 50, leftPos + 156, topPos + 55, 0xFF1A1210);
        g.fill(leftPos + 104, topPos + 50, leftPos + 104 + ww, topPos + 55, 0xFFE84A4A);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(font, title, 8, 6, 0xE8DCC0, false);
        g.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 0x9A8A6A, false);
        int angle = ANGLES[Math.max(0, Math.min(2, menu.angle()))];
        g.drawString(font, Component.translatable("gui.true_metallurgy.edge_angle", angle), 104, 24, 0xE8DCC0, false);
        g.drawString(font, Component.translatable("gui.true_metallurgy.wear", menu.wear()), 8, 35, 0x9A8A6A, false);
        if (menu.wear() >= 80) {
            g.drawString(font, Component.translatable("gui.true_metallurgy.worn"), 8, 45, 0xE84A4A, false);
        }
    }
}

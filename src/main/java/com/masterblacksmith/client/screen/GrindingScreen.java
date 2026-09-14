package com.masterblacksmith.client.screen;

import com.masterblacksmith.MasterBlacksmith;
import com.masterblacksmith.ModNetwork;
import com.masterblacksmith.forging.GrindingProfile;
import com.masterblacksmith.menu.GrindingMenu;
import com.masterblacksmith.network.GrindPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/** Edge-angle bench: keenness against durability, three passes per edge. */
public class GrindingScreen extends AbstractContainerScreen<GrindingMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MasterBlacksmith.MOD_ID, "textures/gui/grinding.png");
    private float angle = 25F;

    public GrindingScreen(GrindingMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 186;
    }

    @Override
    protected void init() {
        super.init();
        angle = menu.angle();
        addRenderableWidget(Button.builder(Component.literal("-"), b -> nudge(-1)).bounds(leftPos + 30, topPos + 52, 20, 18).build());
        addRenderableWidget(Button.builder(Component.literal("+"), b -> nudge(1)).bounds(leftPos + 130, topPos + 52, 20, 18).build());
        float[] presets = {15F, 25F, 35F};
        for (int i = 0; i < 3; i++) {
            final float p = presets[i];
            addRenderableWidget(Button.builder(Component.literal(String.format("%.0f", p)), b -> setAngle(p))
                    .bounds(leftPos + 56 + i * 26, topPos + 52, 24, 18).build());
        }
        addRenderableWidget(Button.builder(Component.translatable("gui.masterblacksmith.grind"), b -> grind())
                .bounds(leftPos + 58, topPos + 74, 60, 18).build());
    }

    private void nudge(int d) {
        setAngle(Math.max(GrindingProfile.MIN_ANGLE, Math.min(GrindingProfile.MAX_ANGLE, angle + d)));
    }

    private void setAngle(float a) {
        angle = a;
        ModNetwork.CHANNEL.sendToServer(new GrindPacket(menu.wheel().getBlockPos(), angle, false));
    }

    private void grind() {
        ModNetwork.CHANNEL.sendToServer(new GrindPacket(menu.wheel().getBlockPos(), angle, true));
    }

    @Override
    protected void renderBg(GuiGraphics g, float partial, int mouseX, int mouseY) {
        g.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int pw = (int) (120F * menu.progress() / 100F);
        g.fill(leftPos + 28, topPos + 98, leftPos + 148, topPos + 104, 0xFF1A1210);
        if (pw > 0) g.fill(leftPos + 28, topPos + 98, leftPos + 28 + pw, topPos + 104, 0xFF7AB8FF);
        int ww = (int) (120F * menu.wear() / 100F);
        g.fill(leftPos + 28, topPos + 106, leftPos + 148, topPos + 110, 0xFF1A1210);
        if (ww > 0) g.fill(leftPos + 28, topPos + 106, leftPos + 28 + ww, topPos + 110, 0xFFC86A4A);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(font, title, 8, 6, 0xE8D8B8, false);
        g.drawString(font, Component.translatable("gui.masterblacksmith.angle", String.format("%.0f", angle)),
                60, 40, 0xFFD86A, false);
        g.drawString(font, Component.translatable("gui.masterblacksmith.wheel_wear", menu.wear()),
                28, 112, 0xC8A878, false);
        g.drawString(font, playerInventoryTitle, 8, imageHeight - 94, 0xE8D8B8, false);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        renderBackground(g);
        super.render(g, mouseX, mouseY, partial);
        renderTooltip(g, mouseX, mouseY);
    }
}

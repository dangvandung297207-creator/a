package com.masterblacksmith.client.screen;

import com.masterblacksmith.MasterBlacksmith;
import com.masterblacksmith.ModNetwork;
import com.masterblacksmith.forging.ForgingData;
import com.masterblacksmith.forging.ForgingTemplates;
import com.masterblacksmith.forging.HeatZone;
import com.masterblacksmith.forging.HeatingHelper;
import com.masterblacksmith.item.SmithingHammerItem;
import com.masterblacksmith.menu.AnvilForgingMenu;
import com.masterblacksmith.network.StrikePacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * Compact forging panel: heat band with sweet-spot marker, stage pips,
 * quality estimate and the timing-bar strike controls.
 */
public class AnvilForgingScreen extends AbstractContainerScreen<AnvilForgingMenu> {
    private static final net.minecraft.resources.ResourceLocation TEXTURE =
            new net.minecraft.resources.ResourceLocation(MasterBlacksmith.MOD_ID, "textures/gui/anvil_forging.png");
    private static final int BAR_W = 160;

    public AnvilForgingScreen(AnvilForgingMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 200;
        this.imageHeight = 196;
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.translatable("gui.masterblacksmith.strike"), b -> strike(0.7F))
                .bounds(leftPos + 20, topPos + 148, 70, 18).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.masterblacksmith.heavy"), b -> strike(0.95F))
                .bounds(leftPos + 110, topPos + 148, 70, 18).build());
        String[] labels = {"S", "A", "P", "R", "L"};
        for (int i = 0; i < 5; i++) {
            final int id = i;
            addRenderableWidget(Button.builder(Component.literal(labels[i]), b -> chooseTemplate(id))
                    .bounds(leftPos + 20 + i * 24, topPos + 126, 20, 16).build());
        }
    }

    private void strike(float force) {
        if (minecraft == null || minecraft.player == null) return;
        if (!(minecraft.player.getMainHandItem().getItem() instanceof SmithingHammerItem)) return;
        float cursor = cursorPos();
        ModNetwork.CHANNEL.sendToServer(new StrikePacket(menu.anvil().getBlockPos(), cursor, force));
    }

    private void chooseTemplate(int id) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
        }
    }

    /** Ping-pong timing cursor, ~1.2s per sweep. */
    private float cursorPos() {
        long t = System.currentTimeMillis() % 1200;
        float phase = t / 1200F;
        return phase < 0.5F ? phase * 2F : 2F - phase * 2F;
    }

    @Override
    protected void renderBg(GuiGraphics g, float partial, int mouseX, int mouseY) {
        g.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        ItemStack work = menu.anvil().getWork();
        boolean hasWork = !work.isEmpty();
        int barX = leftPos + 20;
        int barY = topPos + 44;

        // Heat bar background + sweet-spot band.
        g.fill(barX, barY, barX + BAR_W, barY + 10, 0xFF1A1210);
        if (hasWork) {
            var metal = ForgingData.getMetal(work);
            int sweetL = (int) (BAR_W * Math.min(1F, metal.getSweetMin() / 1650F));
            int sweetR = (int) (BAR_W * Math.min(1F, metal.getSweetMax() / 1650F));
            int forgeL = (int) (BAR_W * Math.min(1F, metal.getForgeMin() / 1650F));
            int forgeR = (int) (BAR_W * Math.min(1F, metal.getForgeMax() / 1650F));
            g.fill(barX + forgeL, barY, barX + forgeR, barY + 10, 0xFF5A2A10);
            g.fill(barX + sweetL, barY, barX + sweetR, barY + 10, 0xFFB86A1A);
            int tw = (int) (BAR_W * Math.min(1F, menu.workTemp() / 1650F));
            int glow = HeatingHelper.glowColorForTemp(menu.workTemp());
            if (tw > 0) g.fill(barX, barY + 6, barX + tw, barY + 10, 0xFF000000 | (glow == 0 ? 0x888888 : glow));
            // Needle.
            g.fill(barX + tw - 1, barY - 2, barX + tw + 1, barY + 12, 0xFFFFFFFF);
        }

        // Timing bar.
        int timeY = topPos + 108;
        g.fill(barX, timeY, barX + BAR_W, timeY + 10, 0xFF1A1210);
        int zoneL = (int) (BAR_W * 0.40F);
        int zoneR = (int) (BAR_W * 0.60F);
        g.fill(barX + zoneL, timeY, barX + zoneR, timeY + 10, 0xFF2A6A2A);
        int cursor = (int) (BAR_W * cursorPos());
        g.fill(barX + cursor - 1, timeY - 2, barX + cursor + 1, timeY + 12, 0xFFFFD86A);

        // Stage pips.
        if (hasWork()) {
            int need = Math.max(1, menu.strikesNeeded());
            for (int i = 0; i < need && i < 12; i++) {
                int color = i < menu.strikes() ? 0xFFFFAA2A : 0xFF4A3A2A;
                g.fill(barX + i * 12, topPos + 88, barX + i * 12 + 10, topPos + 96, color);
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(font, title, 8, 6, 0xE8D8B8, false);
        ItemStack work = menu.anvil().getWork();
        if (work.isEmpty()) {
            g.drawString(font, Component.translatable("gui.masterblacksmith.no_work"), 20, 30, 0x8A7A68, false);
        } else {
            float temp = menu.workTemp();
            var metal = ForgingData.getMetal(work);
            g.drawString(font, Component.translatable("tooltip.masterblacksmith.temperature",
                    String.format("%.0f", temp), HeatZone.zoneFor(metal, temp).getDisplayName()), 20, 30, 0xFFC8874A, false);
            String template = ForgingData.getTemplateId(work);
            if (template.isEmpty()) {
                g.drawString(font, Component.translatable("gui.masterblacksmith.pick_template"), 20, 74, 0xFFD86A, false);
            } else {
                var t = ForgingTemplates.get(template);
                int stage = Math.min(menu.stage(), t.stageCount() - 1);
                g.drawString(font, t.getStages().get(stage).displayName(), 20, 74, 0xE8D8B8, false);
            }
            g.drawString(font, Component.translatable("gui.masterblacksmith.quality_est", menu.qualityEst()),
                    120, 74, 0xFFD86A, false);
            if (menu.reheats() > 0) {
                g.drawString(font, Component.translatable("gui.masterblacksmith.reheats", menu.reheats()),
                        120, 84, 0xC87878, false);
            }
        }
        if (minecraft != null && minecraft.player != null
                && !(minecraft.player.getMainHandItem().getItem() instanceof SmithingHammerItem)) {
            g.drawString(font, Component.translatable("gui.masterblacksmith.need_hammer"), 20, 170, 0xC87878, false);
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

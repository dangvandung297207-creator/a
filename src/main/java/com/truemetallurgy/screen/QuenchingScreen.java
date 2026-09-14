package com.truemetallurgy.screen;

import com.truemetallurgy.menu.QuenchingMenu;
import com.truemetallurgy.network.ModNetworking;
import com.truemetallurgy.util.TMUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

/** Quenching interface: liquid, units, input heat, quench button. */
public class QuenchingScreen extends AbstractContainerScreen<QuenchingMenu> {
    private static final ResourceLocation TEXTURE = TMUtil.rl("textures/gui/quench.png");

    private Button quenchButton;

    public QuenchingScreen(QuenchingMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 186;
        this.inventoryLabelY = 93;
    }

    @Override
    protected void init() {
        super.init();
        quenchButton = Button.builder(Component.translatable("gui.true_metallurgy.quench"),
            btn -> PacketDistributor.sendToServer(new ModNetworking.QuenchActionPayload(menu.pos())))
            .bounds(leftPos + 38, topPos + 62, 100, 20)
            .build();
        addRenderableWidget(quenchButton);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        if (quenchButton != null) quenchButton.active = menu.canQuench();
        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        g.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        // Liquid units bar.
        int uw = (int) (52 * menu.units() / 4.0F);
        int color = menu.liquid() == 2 ? 0xFF3A2A1A : 0xFF2A6AD8;
        g.fill(leftPos + 104, topPos + 35, leftPos + 156, topPos + 41, 0xFF1A1210);
        g.fill(leftPos + 104, topPos + 35, leftPos + 104 + uw, topPos + 41, color);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(font, title, 8, 6, 0xE8DCC0, false);
        g.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 0x9A8A6A, false);
        String liquid = switch (menu.liquid()) {
            case 1 -> Component.translatable("quench.true_metallurgy.water").getString();
            case 2 -> Component.translatable("quench.true_metallurgy.oil").getString();
            default -> Component.translatable("gui.true_metallurgy.empty").getString();
        };
        g.drawString(font, liquid, 104, 24, 0xE8DCC0, false);
        g.drawString(font, menu.units() + "/4", 104, 44, 0x9A8A6A, false);
        if (menu.temperature() >= 0) {
            g.drawString(font, menu.temperature() + "C", 8, 35, 0xFFC93A, false);
        }
        if (!menu.canQuench()) {
            g.drawString(font, Component.translatable("gui.true_metallurgy.quench_hint"), 8, 86, 0x9A8A6A, false);
        }
    }
}

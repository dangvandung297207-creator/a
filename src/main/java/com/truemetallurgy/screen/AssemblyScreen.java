package com.truemetallurgy.screen;

import com.truemetallurgy.menu.AssemblyMenu;
import com.truemetallurgy.network.ModNetworking;
import com.truemetallurgy.util.TMUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

/** Assembly interface: component slots, category readout, craft button. */
public class AssemblyScreen extends AbstractContainerScreen<AssemblyMenu> {
    private static final ResourceLocation TEXTURE = TMUtil.rl("textures/gui/assembly.png");

    private Button craftButton;

    public AssemblyScreen(AssemblyMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 186;
        this.inventoryLabelY = 93;
    }

    @Override
    protected void init() {
        super.init();
        craftButton = Button.builder(Component.translatable("gui.true_metallurgy.assemble"),
            btn -> PacketDistributor.sendToServer(new ModNetworking.AssemblyCraftPayload(menu.pos())))
            .bounds(leftPos + 96, topPos + 70, 60, 18)
            .build();
        addRenderableWidget(craftButton);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        if (craftButton != null) craftButton.active = menu.canCraft();
        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        g.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(font, title, 8, 6, 0xE8DCC0, false);
        g.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 0x9A8A6A, false);
        g.drawString(font, Component.translatable("gui.true_metallurgy.blade"), 26, 24, 0x9A8A6A, false);
        g.drawString(font, Component.translatable("gui.true_metallurgy.blueprint"), 74, 35, 0x9A8A6A, false);
        g.drawString(font, Component.translatable("gui.true_metallurgy.output"), 128, 35, 0x9A8A6A, false);
        if (menu.canCraft()) {
            String cat = switch (menu.category()) {
                case 1 -> Component.translatable("tool.true_metallurgy.axe").getString();
                case 2 -> Component.translatable("tool.true_metallurgy.pickaxe").getString();
                case 3 -> Component.translatable("tool.true_metallurgy.spear").getString();
                default -> Component.translatable("tool.true_metallurgy.sword").getString();
            };
            g.drawString(font, cat, 96, 24, 0xD8B25C, false);
        }
    }
}

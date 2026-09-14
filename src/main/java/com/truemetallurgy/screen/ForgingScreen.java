package com.truemetallurgy.screen;

import com.truemetallurgy.client.ClientHooks;
import com.truemetallurgy.menu.ForgingMenu;
import com.truemetallurgy.metallurgy.ForgingShape;
import com.truemetallurgy.metallurgy.Heat;
import com.truemetallurgy.network.ModNetworking;
import com.truemetallurgy.util.TMUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

/** Compact forging interface: heat, stage, score, target zone, kind select. */
public class ForgingScreen extends AbstractContainerScreen<ForgingMenu> {
    private static final ResourceLocation TEXTURE = TMUtil.rl("textures/gui/forging.png");
    private static final String[] KINDS = {"sword_blade", "axe_head", "pickaxe_head", "spear_head"};

    private float displayedTemp = 20.0F;

    public ForgingScreen(ForgingMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 186;
        this.inventoryLabelY = 93;
    }

    @Override
    protected void init() {
        super.init();
        int baseY = topPos + 62;
        for (int i = 0; i < 4; i++) {
            final int kind = i;
            addRenderableWidget(Button.builder(Component.translatable("kind.true_metallurgy.short_" + i),
                btn -> PacketDistributor.sendToServer(new ModNetworking.AnvilActionPayload(menu.pos(), kind)))
                .bounds(leftPos + 8 + i * 36, baseY, 34, 16)
                .build());
        }
        addRenderableWidget(Button.builder(Component.translatable("gui.true_metallurgy.take"),
            btn -> PacketDistributor.sendToServer(new ModNetworking.AnvilActionPayload(menu.pos(), 4)))
            .bounds(leftPos + 8, topPos + 80, 52, 16)
            .build());
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        displayedTemp += (menu.temperature() - displayedTemp) * 0.2F;
        super.render(g, mouseX, mouseY, partialTick);
        if (isHovering(20, 20, 136, 7, mouseX, mouseY)) {
            Heat.Zone zone = Heat.Zone.fromTemp(menu.temperature());
            g.renderTooltip(font,
                Component.translatable("tooltip.true_metallurgy.temperature", menu.temperature(),
                    Component.translatable("heat.true_metallurgy." + zone.name().toLowerCase())),
                mouseX, mouseY);
        }
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        g.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        // Temperature needle bar (0-1400C).
        int bx = leftPos + 20;
        int by = topPos + 20;
        g.fill(bx, by, bx + 136, by + 7, 0xFF1A1210);
        g.fill(bx, by, bx + (int) (136 * 300 / 1400.0), by + 7, 0xFF4A4A52);
        g.fill(bx + (int) (136 * 300 / 1400.0), by, bx + (int) (136 * 700 / 1400.0), by + 7, 0xFF8A2820);
        g.fill(bx + (int) (136 * 700 / 1400.0), by, bx + (int) (136 * 1000 / 1400.0), by + 7, 0xFFE86A1A);
        g.fill(bx + (int) (136 * 1000 / 1400.0), by, bx + (int) (136 * 1200 / 1400.0), by + 7, 0xFFFFC93A);
        g.fill(bx + (int) (136 * 1200 / 1400.0), by, bx + 136, by + 7, 0xFFFFF2B0);
        int needle = bx + (int) (136 * Math.min(1.0F, displayedTemp / 1400.0F));
        g.fill(needle - 1, by - 2, needle + 1, by + 9, 0xFFFFFFFF);
        // Stage progress bar.
        int req = Math.max(1, menu.stageRequired());
        int pw = menu.stageRequired() > 0 ? (int) (52 * Math.min(1.0F, menu.stageProgress() / (float) req)) : 0;
        g.fill(leftPos + 104, topPos + 35, leftPos + 156, topPos + 40, 0xFF1A1210);
        g.fill(leftPos + 104, topPos + 35, leftPos + 104 + pw, topPos + 40, 0xFFD8B25C);
        // Target zone map (top view of the anvil face).
        int mx = leftPos + 104;
        int my = topPos + 44;
        g.fill(mx, my, mx + 40, my + 40, 0xFF1A1210);
        g.fill(mx + 1, my + 1, mx + 39, my + 39, 0xFF2A2226);
        int dotX = mx + 20 + (int) (menu.targetX() * 60.0F);
        int dotZ = my + 20 + (int) (menu.targetZ() * 60.0F);
        g.fill(dotX - 2, dotZ - 2, dotX + 2, dotZ + 2, 0xFF6AE86A);
        g.fill(mx + 19, my + 19, mx + 21, my + 21, 0xFF8A8A8A);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(font, title, 8, 6, 0xE8DCC0, false);
        g.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 0x9A8A6A, false);
        Heat.Zone zone = Heat.Zone.fromTemp(menu.temperature());
        g.drawString(font, menu.temperature() + "C", 8, 30, 0xFFC93A, false);
        g.drawString(font, Component.translatable("heat.true_metallurgy." + zone.name().toLowerCase()), 8, 40, 0x9A8A6A, false);
        if (menu.hasWorkpiece()) {
            int stage = menu.stage();
            if ((menu.flags() & 2) != 0) {
                g.drawString(font, Component.translatable("tooltip.true_metallurgy.stage_done"), 8, 50, 0x6AE86A, false);
            } else if (menu.stageRequired() > 0) {
                String kindId = KINDS[Math.max(0, Math.min(3, menu.kind()))];
                if ((menu.flags() & 4) != 0) {
                    ForgingShape shape = ForgingShape.BILLET;
                    int s = Math.max(0, Math.min(stage, shape.stages().size() - 1));
                    g.drawString(font, Component.translatable(shape.stages().get(s).nameKey()), 58, 30, 0xE8DCC0, false);
                } else {
                    ForgingShape shape = ForgingShape.forComponent(kindId);
                    int s = Math.max(0, Math.min(stage, shape.stages().size() - 1));
                    g.drawString(font, Component.translatable(shape.stages().get(s).nameKey()), 58, 30, 0xE8DCC0, false);
                }
                g.drawString(font, menu.stageProgress() + "/" + menu.stageRequired(), 104, 30, 0xD8B25C, false);
            }
            g.drawString(font, Component.translatable("gui.true_metallurgy.score", menu.score()), 8, 50, 0x7AD8E8, false);
        } else {
            g.drawString(font, Component.translatable("gui.true_metallurgy.no_workpiece"), 8, 50, 0x9A8A6A, false);
        }
        // Strike flash from the last server feedback.
        long age = Minecraft.getInstance().level != null
            ? Minecraft.getInstance().level.getGameTime() - ClientHooks.lastFeedbackTime() : 999;
        if (age < 25) {
            int color = switch (ClientHooks.lastFeedbackGrade()) {
                case 0 -> 0xFFFFE86A;
                case 1 -> 0xFF6AE86A;
                case 2 -> 0xFF9A8A6A;
                default -> 0xFFE84A4A;
            };
            String key = switch (ClientHooks.lastFeedbackGrade()) {
                case 0 -> "strike.true_metallurgy.perfect";
                case 1 -> "strike.true_metallurgy.good";
                case 2 -> "strike.true_metallurgy.miss";
                default -> "strike.true_metallurgy.bad";
            };
            g.drawString(font, Component.translatable(key), 148, 50, color, true);
        }
    }
}

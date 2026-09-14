package com.masterblacksmith.client.screen;

import com.masterblacksmith.MasterBlacksmith;
import com.masterblacksmith.forging.ForgingData;
import com.masterblacksmith.forging.ItemIdentity;
import com.masterblacksmith.material.HandleMaterials;
import com.masterblacksmith.material.MetalMaterials;
import com.masterblacksmith.material.QuenchLiquids;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** The Blacksmith's Journal: steels, quenches, method and the held piece. */
public class JournalScreen extends Screen {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MasterBlacksmith.MOD_ID, "textures/gui/journal.png");
    private static final int PAGES = 5;
    private int page;
    private int leftPos;
    private int topPos;

    public JournalScreen() {
        super(Component.translatable("item.masterblacksmith.blacksmith_journal"));
    }

    public static void open() {
        Minecraft.getInstance().setScreen(new JournalScreen());
    }

    @Override
    protected void init() {
        leftPos = (width - 220) / 2;
        topPos = (height - 200) / 2;
        addRenderableWidget(Button.builder(Component.literal("<"), b -> turn(-1))
                .bounds(leftPos + 12, topPos + 172, 24, 18).build());
        addRenderableWidget(Button.builder(Component.literal(">"), b -> turn(1))
                .bounds(leftPos + 184, topPos + 172, 24, 18).build());
    }

    private void turn(int d) {
        page = Math.floorMod(page + d, PAGES);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        renderBackground(g);
        g.blit(TEXTURE, leftPos, topPos, 0, 0, 220, 200);
        List<Component> lines = pageLines();
        int y = topPos + 16;
        g.drawString(font, Component.translatable("journal.masterblacksmith.page", page + 1, PAGES),
                leftPos + 88, topPos + 174, 0x5A4A38, false);
        for (Component line : lines) {
            for (var seq : font.split(line, 188)) {
                g.drawString(font, seq, leftPos + 16, y, 0x3A2E22, false);
                y += 11;
                if (y > topPos + 168) return;
            }
        }
        super.render(g, mouseX, mouseY, partial);
    }

    private List<Component> pageLines() {
        List<Component> out = new ArrayList<>();
        switch (page) {
            case 0 -> {
                out.add(Component.translatable("journal.masterblacksmith.title"));
                out.add(Component.literal(" "));
                out.add(Component.translatable("journal.masterblacksmith.index_metals"));
                out.add(Component.translatable("journal.masterblacksmith.index_quench"));
                out.add(Component.translatable("journal.masterblacksmith.index_method"));
                out.add(Component.translatable("journal.masterblacksmith.index_piece"));
                out.add(Component.literal(" "));
                out.add(Component.translatable("journal.masterblacksmith.motto"));
            }
            case 1 -> {
                out.add(Component.translatable("journal.masterblacksmith.metals"));
                for (var m : MetalMaterials.all()) {
                    out.add(Component.literal(String.format("%s  %.0f-%.0f  *%.0f-%.0f",
                            m.getDisplayName().getString(), m.getForgeMin(), m.getForgeMax(),
                            m.getSweetMin(), m.getSweetMax())));
                }
            }
            case 2 -> {
                out.add(Component.translatable("journal.masterblacksmith.quenches"));
                for (var q : QuenchLiquids.all()) {
                    out.add(Component.literal(String.format("%s  H%.2f T%.2f S%.2f D%.2f",
                            q.getDisplayName().getString(), q.getHardnessMod(), q.getToughnessMod(),
                            q.getSharpnessMod(), q.getDurabilityMod())));
                }
            }
            case 3 -> {
                out.add(Component.translatable("journal.masterblacksmith.method"));
                out.add(Component.translatable("journal.masterblacksmith.step1"));
                out.add(Component.translatable("journal.masterblacksmith.step2"));
                out.add(Component.translatable("journal.masterblacksmith.step3"));
                out.add(Component.translatable("journal.masterblacksmith.step4"));
                out.add(Component.translatable("journal.masterblacksmith.step5"));
                out.add(Component.translatable("journal.masterblacksmith.step6"));
                out.add(Component.literal(" "));
                out.add(Component.translatable("journal.masterblacksmith.handles",
                        HandleMaterials.all().size()));
            }
            default -> {
                out.add(Component.translatable("journal.masterblacksmith.held"));
                ItemStack held = Minecraft.getInstance().player == null ? ItemStack.EMPTY
                        : Minecraft.getInstance().player.getMainHandItem();
                if (held.isEmpty()) {
                    out.add(Component.translatable("journal.masterblacksmith.empty_hand"));
                } else if (ItemIdentity.hasIdentity(held)) {
                    out.addAll(ItemIdentity.lore(held));
                } else if (ForgingData.isWorkable(held) || ForgingData.isAssemblyReady(held)
                        || ForgingData.isQuenchable(held)) {
                    out.add(held.getHoverName());
                    out.add(Component.translatable("tooltip.masterblacksmith.temperature",
                            String.format("%.0f", ForgingData.getTemp(held)), ""));
                    out.add(Component.translatable("tooltip.masterblacksmith.purity",
                            String.format("%.1f%%", ForgingData.getPurity(held))));
                    out.add(Component.translatable("tooltip.masterblacksmith.forging_q",
                            Math.round(com.masterblacksmith.forging.QualityCalculator.componentScore(held))));
                } else {
                    out.add(held.getHoverName());
                    out.add(Component.translatable("journal.masterblacksmith.not_forged"));
                }
            }
        }
        return out;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

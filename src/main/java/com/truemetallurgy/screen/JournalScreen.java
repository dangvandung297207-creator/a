package com.truemetallurgy.screen;

import com.truemetallurgy.components.JournalData;
import com.truemetallurgy.item.BlacksmithJournalItem;
import com.truemetallurgy.metallurgy.Materials;
import com.truemetallurgy.registry.ModSounds;
import com.truemetallurgy.util.TMUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;

/**
 * The Blacksmith's Journal: parchment pages for materials, processes and
 * recorded creations. Also hosts the read-only inspect view.
 */
public class JournalScreen extends Screen {
    private static final ResourceLocation TEXTURE = TMUtil.rl("textures/gui/journal.png");
    private static final int W = 240;
    private static final int H = 180;

    public record Inspect(String itemName, int score, String materialId, String quenchId, int edge, float weight, String crafter) {}

    private final ItemStack journal;
    private final Inspect inspect;
    private final JournalData data;
    private int page;
    private final int maxPage;

    private Button prevButton;
    private Button nextButton;

    public JournalScreen(ItemStack journal, Inspect inspect) {
        super(Component.translatable("item.true_metallurgy.blacksmith_journal"));
        this.journal = journal;
        this.inspect = inspect;
        this.data = inspect != null ? JournalData.EMPTY : BlacksmithJournalItem.dataOf(journal);
        this.maxPage = inspect != null ? 0 : 5;
    }

    @Override
    protected void init() {
        super.init();
        int x = (width - W) / 2;
        int y = (height - H) / 2;
        prevButton = Button.builder(Component.literal("<"), btn -> turnPage(-1))
            .bounds(x + 12, y + H - 26, 20, 18).build();
        nextButton = Button.builder(Component.literal(">"), btn -> turnPage(1))
            .bounds(x + W - 32, y + H - 26, 20, 18).build();
        addRenderableWidget(prevButton);
        addRenderableWidget(nextButton);
        updateButtons();
    }

    private void turnPage(int dir) {
        page = Math.max(0, Math.min(maxPage, page + dir));
        Minecraft mc = Minecraft.getInstance();
        if (mc != null) {
            mc.getSoundManager().play(SimpleSoundInstance.forUI(ModSounds.JOURNAL_PAGE.get(), 1.0F));
            mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 0.5F));
        }
        updateButtons();
    }

    private void updateButtons() {
        if (prevButton != null) prevButton.active = page > 0;
        if (nextButton != null) nextButton.active = page < maxPage;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        int x = (width - W) / 2;
        int y = (height - H) / 2;
        g.blit(TEXTURE, x, y, 0, 0, W, H, 256, 256);
        int ink = 0x3A2A1A;
        int accent = 0x7A4A1A;
        if (inspect != null) {
            drawLine(g, x, y, 14, Component.literal(inspect.itemName()).getString(), accent);
            drawLine(g, x, y, 28, Component.translatable("tooltip.true_metallurgy.forged_by", inspect.crafter()).getString(), ink);
            drawLine(g, x, y, 42, Component.translatable("tooltip.true_metallurgy.craft_score", inspect.score()).getString(), ink);
            drawLine(g, x, y, 56, Component.translatable("material.true_metallurgy." + inspect.materialId()).getString(), ink);
            if (!inspect.quenchId().equals("none")) {
                drawLine(g, x, y, 70, Component.translatable("tooltip.true_metallurgy.quenched",
                    Component.translatable("quench.true_metallurgy." + inspect.quenchId())).getString(), ink);
            }
            if (inspect.edge() > 0) {
                drawLine(g, x, y, 84, Component.translatable("tooltip.true_metallurgy.edge", inspect.edge()).getString(), ink);
            }
            drawLine(g, x, y, 98, Component.translatable("tooltip.true_metallurgy.weight",
                String.format("%.1f", inspect.weight())).getString(), ink);
            return;
        }
        switch (page) {
            case 0 -> {
                drawLine(g, x, y, 14, Component.translatable("journal.true_metallurgy.title").getString(), accent);
                drawWrapped(g, x, y, 30, Component.translatable("journal.true_metallurgy.intro").getString(), ink);
                drawLine(g, x, y, 120, Component.translatable("journal.true_metallurgy.best",
                    data.bestName().isEmpty() ? "-" : data.bestName(), data.bestScore()).getString(), accent);
            }
            case 1 -> {
                drawLine(g, x, y, 14, Component.translatable("journal.true_metallurgy.materials").getString(), accent);
                int line = 30;
                for (var mat : Materials.all()) {
                    boolean known = data.discovered().contains(mat.id());
                    String name = known ? Component.translatable("material.true_metallurgy." + mat.id()).getString() : "? ? ?";
                    drawLine(g, x, y, line, name + (known ? "  " + mat.forgeMin() + "-" + mat.forgeMax() + "C" : ""), ink);
                    line += 14;
                    if (line > 140) break;
                }
            }
            case 2 -> {
                drawLine(g, x, y, 14, Component.translatable("journal.true_metallurgy.forging").getString(), accent);
                drawWrapped(g, x, y, 30, Component.translatable("journal.true_metallurgy.forging_text").getString(), ink);
            }
            case 3 -> {
                drawLine(g, x, y, 14, Component.translatable("journal.true_metallurgy.quenching").getString(), accent);
                drawWrapped(g, x, y, 30, Component.translatable("journal.true_metallurgy.quenching_text").getString(), ink);
            }
            case 4 -> {
                drawLine(g, x, y, 14, Component.translatable("journal.true_metallurgy.grinding").getString(), accent);
                drawWrapped(g, x, y, 30, Component.translatable("journal.true_metallurgy.grinding_text").getString(), ink);
            }
            default -> {
                drawLine(g, x, y, 14, Component.translatable("journal.true_metallurgy.creations").getString(), accent);
                int line = 30;
                if (data.creations().isEmpty()) {
                    drawLine(g, x, y, line, Component.translatable("journal.true_metallurgy.no_creations").getString(), ink);
                }
                for (JournalData.Creation c : data.creations()) {
                    drawLine(g, x, y, line, c.name() + "  [" + c.score() + "]", ink);
                    line += 13;
                    if (line > 145) break;
                }
            }
        }
    }

    private void drawLine(GuiGraphics g, int x, int y, int dy, String text, int color) {
        g.drawString(Minecraft.getInstance().font, text, x + 18, y + dy, color, false);
    }

    private void drawWrapped(GuiGraphics g, int x, int y, int dy, String text, int color) {
        var font = Minecraft.getInstance().font;
        int line = 0;
        for (var part : font.split(Component.literal(text), W - 40)) {
            g.drawString(font, part, x + 18, y + dy + line * 11, color, false);
            if (++line > 10) break;
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

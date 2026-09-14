package com.masterblacksmith.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.inventory.Slot;

/** Shared medieval widget painting for the workshop screens. */
public final class ScreenUtil {
    private ScreenUtil() {}

    /** Draw an 18x18 recessed slot frame around a menu slot. */
    public static void slotFrames(GuiGraphics g, int leftPos, int topPos, Iterable<Slot> slots) {
        for (Slot slot : slots) {
            int x = leftPos + slot.x - 1;
            int y = topPos + slot.y - 1;
            g.fill(x, y, x + 18, y + 18, 0xFF14100C);
            g.fill(x + 1, y + 1, x + 17, y + 17, 0xFF2A2118);
            g.fill(x + 1, y + 1, x + 17, y + 2, 0xFF0A0806);
            g.fill(x + 1, y + 1, x + 2, y + 17, 0xFF0A0806);
        }
    }
}

package com.masterblacksmith.forging;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

/** Hammer-strike verdicts. Every strike leaves a mark. */
public enum StrikeResult {
    PERFECT("perfect", ChatFormatting.GOLD),
    GOOD("good", ChatFormatting.GREEN),
    MISS("miss", ChatFormatting.GRAY),
    BAD("bad", ChatFormatting.RED);

    private final String key;
    private final ChatFormatting color;

    StrikeResult(String key, ChatFormatting color) {
        this.key = key;
        this.color = color;
    }

    public Component getDisplayName() {
        return Component.translatable("strike.masterblacksmith." + key).withStyle(color);
    }
}

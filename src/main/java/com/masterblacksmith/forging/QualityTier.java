package com.masterblacksmith.forging;

import com.masterblacksmith.MBSConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

/** Flawed -&gt; Standard -&gt; Masterwork -&gt; Legendary craftsmanship tiers. */
public enum QualityTier {
    FLAWED("flawed", 0, 39, ChatFormatting.DARK_RED, 0.60F, 0.80F),
    STANDARD("standard", 40, 69, ChatFormatting.GRAY, 1.00F, 1.00F),
    MASTERWORK("masterwork", 70, 89, ChatFormatting.GOLD, 1.25F, 1.15F),
    LEGENDARY("legendary", 90, 100, ChatFormatting.LIGHT_PURPLE, 1.60F, 1.35F);

    private final String key;
    private final int min;
    private final int max;
    private final ChatFormatting color;
    private final float durabilityMult;
    private final float performanceMult;

    QualityTier(String key, int min, int max, ChatFormatting color, float durabilityMult, float performanceMult) {
        this.key = key;
        this.min = min;
        this.max = max;
        this.color = color;
        this.durabilityMult = durabilityMult;
        this.performanceMult = performanceMult;
    }

    public String getKey() { return key; }
    public ChatFormatting getColor() { return color; }
    public float getDurabilityMult() { return durabilityMult; }
    public float getPerformanceMult() { return performanceMult; }

    public Component getDisplayName() {
        return Component.translatable("tier.masterblacksmith." + key).withStyle(color);
    }

    public static QualityTier tierFor(int score) {
        int legendary = MBSConfig.LEGENDARY_THRESHOLD.get();
        int masterwork = MBSConfig.MASTERWORK_THRESHOLD.get();
        if (score >= legendary) return LEGENDARY;
        if (score >= masterwork) return MASTERWORK;
        if (score >= 40) return STANDARD;
        return FLAWED;
    }
}

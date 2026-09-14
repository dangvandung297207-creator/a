package com.truemetallurgy.client;

import com.truemetallurgy.config.TMConfig;
import com.truemetallurgy.screen.JournalScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * Client-side effects entry point. Called from menus, items and S2C packets.
 * Never referenced from server-only code paths.
 */
public final class ClientHooks {
    private ClientHooks() {}

    private static int lastGrade;
    private static long lastFeedbackTime = -1000;

    public static int lastFeedbackGrade() { return lastGrade; }
    public static long lastFeedbackTime() { return lastFeedbackTime; }

    public static void openJournal(ItemStack stack) {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null) {
            mc.setScreen(new JournalScreen(stack.copy(), null));
        }
    }

    public static void openInspect(String itemName, int score, String materialId, String quenchId,
            int edge, float weight, String crafter) {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null) {
            mc.setScreen(new JournalScreen(ItemStack.EMPTY,
                new JournalScreen.Inspect(itemName, score, materialId, quenchId, edge, weight, crafter)));
        }
    }

    /** Strike broadcast: GUI flash timing + subtle nearby camera kick. */
    public static void onStrikeFeedback(BlockPos pos, int grade, boolean cracked) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.level == null || mc.player == null) return;
        lastGrade = grade;
        lastFeedbackTime = mc.level.getGameTime();
        double dist = mc.player.distanceToSqr(Vec3.atCenterOf(pos));
        if (dist > 64.0) return;
        boolean shake;
        try {
            shake = TMConfig.ENABLE_CAMERA_SHAKE.get();
        } catch (IllegalStateException notLoaded) {
            shake = true;
        }
        if (!shake) return;
        float kick = switch (grade) {
            case 0 -> 0.5F;
            case 1 -> 0.3F;
            case 2 -> 0.05F;
            default -> 0.4F;
        } + (cracked ? 0.2F : 0.0F);
        float falloff = (float) (1.0 - dist / 64.0);
        mc.player.setXRot(Math.max(-90.0F, Math.min(90.0F, mc.player.getXRot() + kick * falloff)));
        mc.player.setYRot(mc.player.getYRot() + (mc.level.random.nextFloat() - 0.5F) * kick * falloff);
    }
}

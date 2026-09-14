package com.enderblade.client;

import com.enderblade.client.vfx.VfxManager;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

/**
 * Safe entry points from network handlers into client-only code.
 */
public final class ClientHooks {

    private ClientHooks() {
    }

    public static void playAnimation(int entityId, String animId) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        Entity e = mc.level.getEntity(entityId);
        if (e != null) {
            AnimationHandler.play(e, animId);
        }
    }

    public static void spawnVfx(String type, double x, double y, double z, float scale) {
        VfxManager.spawn(type, x, y, z, scale);
    }
}

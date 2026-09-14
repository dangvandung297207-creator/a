package com.enderblade.client;

import com.enderblade.EnderBladeMod;
import com.enderblade.registry.ModEntities;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * Client-only bootstrap: entity renderers and client setup hooks.
 * Loaded exclusively on the physical client via {@code dist = Dist.CLIENT}.
 */
@Mod(value = EnderBladeMod.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = EnderBladeMod.MOD_ID, value = Dist.CLIENT)
public class EnderBladeClient {

    public EnderBladeClient(ModContainer container) {
        // No config screen needed for this mod.
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        EnderBladeMod.LOGGER.info("Ender Blade client setup.");
    }

    /**
     * Registers a {@link ThrownItemRenderer} for the Ender Phantom projectile.
     * The projectile supplies an Ender Eye stack via {@code getItem()}, so it
     * renders as a floating void orb with the particle trail providing motion feel.
     */
    @SubscribeEvent
    static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(
                ModEntities.ENDER_PHANTOM.get(),
                context -> new ThrownItemRenderer<>(context, 1.0f, true)
        );
    }
}

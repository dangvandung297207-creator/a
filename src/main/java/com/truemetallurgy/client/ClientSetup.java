package com.truemetallurgy.client;

import com.truemetallurgy.TrueMetallurgy;
import com.truemetallurgy.client.particle.TMParticles;
import com.truemetallurgy.client.renderer.AnvilWorkpieceRenderer;
import com.truemetallurgy.client.renderer.BlacksmithRenderer;
import com.truemetallurgy.entity.BlacksmithEntity;
import com.truemetallurgy.forging.HotMetal;
import com.truemetallurgy.metallurgy.Heat;
import com.truemetallurgy.registry.ModBlockEntities;
import com.truemetallurgy.registry.ModEntities;
import com.truemetallurgy.registry.ModItems;
import com.truemetallurgy.registry.ModMenus;
import com.truemetallurgy.registry.ModParticles;
import com.truemetallurgy.screen.AssemblyScreen;
import com.truemetallurgy.screen.ForgeHearthScreen;
import com.truemetallurgy.screen.ForgingScreen;
import com.truemetallurgy.screen.GrindingScreen;
import com.truemetallurgy.screen.QuenchingScreen;
import com.truemetallurgy.util.TMUtil;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

/** All client registration. Never loaded on a dedicated server. */
@EventBusSubscriber(modid = TrueMetallurgy.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientSetup {
    private ClientSetup() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            Item[] hotItems = {
                ModItems.COPPER_BILLET.get(), ModItems.IRON_BILLET.get(),
                ModItems.STEEL_BILLET.get(), ModItems.HARDENED_BILLET.get(),
                ModItems.SWORD_BLADE.get(), ModItems.AXE_HEAD.get(),
                ModItems.PICKAXE_HEAD.get(), ModItems.SPEAR_HEAD.get(),
                ModItems.IRON_BLOOM.get(), ModItems.COPPER_BLOOM.get(),
                ModItems.STEEL_BLOOM.get(), ModItems.HARDENED_BLOOM.get(),
            };
            for (Item item : hotItems) {
                ItemProperties.register(item, TMUtil.rl("heat"),
                    (ItemStack stack, net.minecraft.client.multiplayer.ClientLevel level,
                     LivingEntity entity, int seed) ->
                        Heat.modelHeat(Math.max(0, HotMetal.temperatureOf(stack))));
            }
        });
    }

    @SubscribeEvent
    public static void onRegisterScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.FORGE.get(), ForgeHearthScreen::new);
        event.register(ModMenus.FORGING.get(), ForgingScreen::new);
        event.register(ModMenus.QUENCHING.get(), QuenchingScreen::new);
        event.register(ModMenus.GRINDING.get(), GrindingScreen::new);
        event.register(ModMenus.ASSEMBLY.get(), AssemblyScreen::new);
    }

    @SubscribeEvent
    public static void onRegisterParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.SPARK.get(), TMParticles.Spark.Provider::new);
        event.registerSpriteSet(ModParticles.EMBER.get(), TMParticles.Ember.Provider::new);
        event.registerSpriteSet(ModParticles.STEAM.get(), TMParticles.Steam.Provider::new);
        event.registerSpriteSet(ModParticles.METAL_DUST.get(), TMParticles.MetalDust.Provider::new);
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.BLACKSMITH.get(), BlacksmithRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ANVIL.get(), AnvilWorkpieceRenderer::new);
    }
}

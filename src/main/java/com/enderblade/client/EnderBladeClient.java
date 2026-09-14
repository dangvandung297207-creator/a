package com.enderblade.client;

import com.enderblade.EnderBladeMod;
import com.enderblade.client.model.EnderBladeModel;
import com.enderblade.client.particle.ModParticleProviders;
import com.enderblade.client.renderer.EndDimensionZoneRenderer;
import com.enderblade.client.renderer.EnderBladeItemRenderer;
import com.enderblade.client.renderer.EnderEchoRenderer;
import com.enderblade.client.renderer.VoidAnchorRenderer;
import com.enderblade.client.renderer.VoidSlashRenderer;
import com.enderblade.client.vfx.VfxManager;
import com.enderblade.network.AbilityKeyPayload;
import com.enderblade.registry.ModEntities;
import com.enderblade.registry.ModItems;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

/**
 * Client bootstrap: custom 3D item renderer, entity renderers, particles, keys, VFX.
 */
@Mod(value = EnderBladeMod.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = EnderBladeMod.MOD_ID, value = Dist.CLIENT)
public class EnderBladeClient {

    public static KeyMapping KEY_VOID_SLASH;
    public static KeyMapping KEY_PARADOX;
    public static KeyMapping KEY_ULTIMATE;

    private static EnderBladeItemRenderer itemRenderer;

    public EnderBladeClient(ModContainer container) {
        NeoForge.EVENT_BUS.addListener(EnderBladeClient::onClientTick);
        NeoForge.EVENT_BUS.addListener(EnderBladeClient::onRenderLevel);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // Idle property placeholder (for future json overrides)
            ItemProperties.register(ModItems.ENDER_BLADE.get(),
                    ResourceLocation.fromNamespaceAndPath(EnderBladeMod.MOD_ID, "active"),
                    (stack, level, entity, seed) -> 0f);
        });
        EnderBladeMod.LOGGER.info("Ender Blade client online.");
    }

    @SubscribeEvent
    static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(EnderBladeModel.LAYER, EnderBladeModel::createBodyLayer);
    }

    @SubscribeEvent
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.ENDER_ECHO.get(), EnderEchoRenderer::new);
        event.registerEntityRenderer(ModEntities.VOID_ANCHOR.get(), VoidAnchorRenderer::new);
        event.registerEntityRenderer(ModEntities.VOID_SLASH.get(), VoidSlashRenderer::new);
        event.registerEntityRenderer(ModEntities.END_DIMENSION_ZONE.get(), EndDimensionZoneRenderer::new);
    }

    @SubscribeEvent
    static void registerClientExt(RegisterClientExtensionsEvent event) {
        itemRenderer = new EnderBladeItemRenderer();
        event.registerItem(new IClientItemExtensions() {
            @Override
            public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return itemRenderer;
            }
        }, ModItems.ENDER_BLADE.get());
    }

    @SubscribeEvent
    static void registerParticles(RegisterParticleProvidersEvent event) {
        ModParticleProviders.register(event);
    }

    @SubscribeEvent
    static void registerKeys(RegisterKeyMappingsEvent event) {
        KEY_VOID_SLASH = new KeyMapping("key.enderblade.void_slash", GLFW.GLFW_KEY_R, "key.categories.enderblade");
        KEY_PARADOX = new KeyMapping("key.enderblade.paradox", GLFW.GLFW_KEY_V, "key.categories.enderblade");
        KEY_ULTIMATE = new KeyMapping("key.enderblade.ultimate", GLFW.GLFW_KEY_G, "key.categories.enderblade");
        event.register(KEY_VOID_SLASH);
        event.register(KEY_PARADOX);
        event.register(KEY_ULTIMATE);
    }

    static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        AnimationHandler.tick(0.05f);
        VfxManager.tick();

        if (KEY_VOID_SLASH != null && KEY_VOID_SLASH.consumeClick()) {
            PacketDistributor.sendToServer(new AbilityKeyPayload("void_slash"));
        }
        if (KEY_PARADOX != null && KEY_PARADOX.consumeClick()) {
            PacketDistributor.sendToServer(new AbilityKeyPayload("paradox"));
        }
        if (KEY_ULTIMATE != null && KEY_ULTIMATE.consumeClick()) {
            PacketDistributor.sendToServer(new AbilityKeyPayload("ultimate"));
        }
    }

    static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        PoseStack pose = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
        VfxManager.render(pose, buffers, 0.0f);
        buffers.endBatch();
    }
}

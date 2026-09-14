package com.masterblacksmith.client;

import com.masterblacksmith.MasterBlacksmith;
import com.masterblacksmith.ModBlockEntities;
import com.masterblacksmith.ModEntities;
import com.masterblacksmith.ModItems;
import com.masterblacksmith.ModMenus;
import com.masterblacksmith.ModParticles;
import com.masterblacksmith.client.particle.EmberParticle;
import com.masterblacksmith.client.particle.SparkParticle;
import com.masterblacksmith.client.particle.SteamParticle;
import com.masterblacksmith.client.renderer.MetalShelfRenderer;
import com.masterblacksmith.client.renderer.ToolRackRenderer;
import com.masterblacksmith.client.renderer.TravellingBlacksmithRenderer;
import com.masterblacksmith.client.screen.AnvilForgingScreen;
import com.masterblacksmith.client.screen.AssemblyScreen;
import com.masterblacksmith.client.screen.ForgeHearthScreen;
import com.masterblacksmith.client.screen.GrindingScreen;
import com.masterblacksmith.forging.ForgingData;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.entity.EntityRenderersEvent;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.registries.ForgeRegistries;

/** Client-only wiring: screens, renderers, particles, heat predicates. */
public class ClientSetup {
    public static void init() {
        MenuScreens.register(ModMenus.FORGE_HEARTH.get(), ForgeHearthScreen::new);
        MenuScreens.register(ModMenus.ANVIL_FORGING.get(), AnvilForgingScreen::new);
        MenuScreens.register(ModMenus.ASSEMBLY.get(), AssemblyScreen::new);
        MenuScreens.register(ModMenus.GRINDING.get(), GrindingScreen::new);
        registerItemProperties();
    }

    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers e) {
        e.registerEntityRenderer(ModEntities.TRAVELLING_BLACKSMITH.get(), TravellingBlacksmithRenderer::new);
        e.registerBlockEntityRenderer(ModBlockEntities.TOOL_RACK.get(), ToolRackRenderer::new);
        e.registerBlockEntityRenderer(ModBlockEntities.METAL_SHELF.get(), MetalShelfRenderer::new);
    }

    public static void registerParticleProviders(RegisterParticleProvidersEvent e) {
        e.registerSpriteSet(ModParticles.EMBER.get(), EmberParticle.Provider::new);
        e.registerSpriteSet(ModParticles.FORGE_SPARK.get(), SparkParticle.Provider::new);
        e.registerSpriteSet(ModParticles.QUENCH_STEAM.get(), SteamParticle.Provider::new);
        e.registerSpriteSet(ModParticles.GRIND_DUST.get(), SteamParticle.Provider::new);
    }

    /** "heat" predicate drives glowing-hot model overrides on workpieces. */
    private static void registerItemProperties() {
        ResourceLocation heat = new ResourceLocation(MasterBlacksmith.MOD_ID, "heat");
        for (String m : ModItems.METALS) {
            heatProp(heat, "billet_" + m);
            heatProp(heat, "bloom_" + m);
        }
        heatProp(heat, "blade_blank");
        heatProp(heat, "axe_head_blank");
        heatProp(heat, "pick_head_blank");
        heatProp(heat, "spear_head_blank");
        heatProp(heat, "armor_plate_blank");
        heatProp(heat, "sword_blade");
        heatProp(heat, "axe_head");
        heatProp(heat, "pickaxe_head");
        heatProp(heat, "spear_head");
        heatProp(heat, "armor_plate");
    }

    private static void heatProp(ResourceLocation key, String itemId) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(MasterBlacksmith.MOD_ID, itemId));
        if (item == null) return;
        ItemProperties.register(item, key, (stack, level, entity, seed) -> {
            float t = ForgingData.getTemp(stack);
            if (t < 400) return 0F;
            if (t < 700) return 0.33F;
            if (t < 1000) return 0.66F;
            return 1F;
        });
    }
}

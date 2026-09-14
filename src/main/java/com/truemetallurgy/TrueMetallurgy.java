package com.truemetallurgy;

import com.truemetallurgy.datagen.TMDataGen;
import com.truemetallurgy.config.TMConfig;
import com.truemetallurgy.network.ModNetworking;
import com.truemetallurgy.registry.ModBlockEntities;
import com.truemetallurgy.registry.ModBlocks;
import com.truemetallurgy.registry.ModCreativeTabs;
import com.truemetallurgy.registry.ModDataComponents;
import com.truemetallurgy.registry.ModEntities;
import com.truemetallurgy.registry.ModFeatures;
import com.truemetallurgy.registry.ModItems;
import com.truemetallurgy.registry.ModLootModifiers;
import com.truemetallurgy.registry.ModMenus;
import com.truemetallurgy.registry.ModParticles;
import com.truemetallurgy.registry.ModRecipeTypes;
import com.truemetallurgy.registry.ModSounds;
import com.truemetallurgy.util.TMUtil;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Master Blacksmith - True Metallurgy.
 *
 * <p>Core progression:
 * Raw Ore -&gt; Processed Ore -&gt; Bloom -&gt; Billet -&gt; Hot Billet -&gt;
 * Forged Component -&gt; Quenching -&gt; Grinding -&gt; Assembly -&gt; Finished Item.
 */
@Mod(TrueMetallurgy.MOD_ID)
public class TrueMetallurgy {
    public static final String MOD_ID = "true_metallurgy";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public TrueMetallurgy(IEventBus modEventBus, ModContainer modContainer) {
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);
        ModSounds.SOUNDS.register(modEventBus);
        ModParticles.PARTICLES.register(modEventBus);
        ModCreativeTabs.TABS.register(modEventBus);
        ModDataComponents.TYPES.register(modEventBus);
        ModRecipeTypes.RECIPE_TYPES.register(modEventBus);
        ModRecipeTypes.RECIPE_SERIALIZERS.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModFeatures.FEATURES.register(modEventBus);
        ModLootModifiers.SERIALIZERS.register(modEventBus);

        modEventBus.addListener(ModNetworking::registerPayloads);
        modEventBus.addListener(TMDataGen::gatherData);
        modEventBus.addListener(TrueMetallurgy::registerCapabilities);
        modEventBus.addListener(TrueMetallurgy::registerAttributes);

        modContainer.registerConfig(ModConfig.Type.SERVER, TMConfig.SERVER_SPEC);
        modContainer.registerConfig(ModConfig.Type.CLIENT, TMConfig.CLIENT_SPEC);

        LOGGER.info("[TrueMetallurgy] Forge lit. Every hammer strike matters.");
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
            net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK,
            ModBlockEntities.FORGE.get(),
            (be, side) -> be.getItemHandler());
        event.registerBlockEntity(
            net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK,
            ModBlockEntities.ANVIL.get(),
            (be, side) -> be.getItemHandler());
        event.registerBlockEntity(
            net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK,
            ModBlockEntities.QUENCH_BARREL.get(),
            (be, side) -> be.getItemHandler());
        event.registerBlockEntity(
            net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK,
            ModBlockEntities.GRINDING_WHEEL.get(),
            (be, side) -> be.getItemHandler());
        event.registerBlockEntity(
            net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK,
            ModBlockEntities.ASSEMBLY_TABLE.get(),
            (be, side) -> be.getItemHandler());
    }

    private static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.BLACKSMITH.get(), com.truemetallurgy.entity.BlacksmithEntity.createAttributes().build());
    }

    public static ResourceLocation rl(String path) {
        return TMUtil.rl(path);
    }
}

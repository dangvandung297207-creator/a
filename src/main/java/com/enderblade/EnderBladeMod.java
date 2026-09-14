package com.enderblade;

import com.enderblade.registry.ModCreativeTabs;
import com.enderblade.registry.ModDataComponents;
import com.enderblade.registry.ModEntities;
import com.enderblade.registry.ModItems;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;

/**
 * Main entry point for the Ender Blade mod (Đoản Kiếm Hư Không).
 * <p>
 * Targets Minecraft 1.21.1 / NeoForge 21.1.x (Java 21).
 */
@Mod(EnderBladeMod.MOD_ID)
public class EnderBladeMod {

    public static final String MOD_ID = "enderblade";
    public static final Logger LOGGER = LogUtils.getLogger();

    public EnderBladeMod(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        ModDataComponents.DATA_COMPONENTS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);

        LOGGER.info("Ender Blade mod constructing — registries queued.");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Ender Blade common setup complete.");
    }
}

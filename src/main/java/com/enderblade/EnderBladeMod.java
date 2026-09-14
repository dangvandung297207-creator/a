package com.enderblade;

import com.enderblade.network.ModNetworking;
import com.enderblade.registry.ModAttachments;
import com.enderblade.registry.ModCreativeTabs;
import com.enderblade.registry.ModDataComponents;
import com.enderblade.registry.ModEffects;
import com.enderblade.registry.ModEntities;
import com.enderblade.registry.ModItems;
import com.enderblade.registry.ModParticles;
import com.enderblade.registry.ModSounds;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;

/**
 * Đoản Kiếm Hư Không — Ender Blade
 * NeoForge 1.21.1 / Java 21
 */
@Mod(EnderBladeMod.MOD_ID)
public class EnderBladeMod {

    public static final String MOD_ID = "enderblade";
    public static final Logger LOGGER = LogUtils.getLogger();

    public EnderBladeMod(IEventBus modBus, ModContainer container) {
        modBus.addListener(this::commonSetup);

        ModDataComponents.DATA_COMPONENTS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModEntities.ENTITY_TYPES.register(modBus);
        ModSounds.SOUND_EVENTS.register(modBus);
        ModEffects.MOB_EFFECTS.register(modBus);
        ModParticles.PARTICLE_TYPES.register(modBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modBus);
        ModAttachments.ATTACHMENT_TYPES.register(modBus);

        modBus.addListener(ModNetworking::register);

        LOGGER.info("Ender Blade constructing — dimensional registers online.");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Ender Blade common setup complete.");
    }
}

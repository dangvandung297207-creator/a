package com.masterblacksmith;

import com.masterblacksmith.client.ClientSetup;
import com.masterblacksmith.event.ModEvents;
import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

/**
 * The Master Blacksmith (codename: True Metallurgy).
 * Turns crafting metal gear into a real blacksmithing profession:
 * ore -&gt; metallurgy -&gt; heat -&gt; forge -&gt; hammer -&gt; quench -&gt; grind -&gt; assembly -&gt; craftsmanship.
 */
@Mod(MasterBlacksmith.MOD_ID)
public class MasterBlacksmith {
    public static final String MOD_ID = "masterblacksmith";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MasterBlacksmith() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modBus);
        ModMenus.MENUS.register(modBus);
        ModParticles.PARTICLES.register(modBus);
        ModSounds.SOUNDS.register(modBus);
        ModEntities.ENTITIES.register(modBus);
        ModFeatures.FEATURES.register(modBus);
        ModVillagers.POI_TYPES.register(modBus);
        ModVillagers.PROFESSIONS.register(modBus);
        ModCreativeTabs.TABS.register(modBus);

        modBus.addListener(this::commonSetup);
        modBus.addListener(this::clientSetup);
        modBus.addListener(ModEntities::registerAttributes);
        modBus.addListener(ClientSetup::registerRenderers);
        modBus.addListener(ClientSetup::registerParticleProviders);

        MinecraftForge.EVENT_BUS.register(ModEvents.class);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, MBSConfig.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ModNetwork.register();
            net.minecraft.world.entity.SpawnPlacements.register(ModEntities.TRAVELLING_BLACKSMITH.get(),
                    net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
                    net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    net.minecraft.world.entity.PathfinderMob::checkMobSpawnRules);
        });
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(ClientSetup::init);
    }
}

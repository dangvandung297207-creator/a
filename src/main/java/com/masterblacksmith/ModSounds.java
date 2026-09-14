package com.masterblacksmith;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * The blacksmith soundscape. Sound files live under
 * assets/masterblacksmith/sounds/ (see docs/AUDIO.md). Until final recordings
 * land, key actions also layer matching vanilla sounds so feedback stays rich.
 */
public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MasterBlacksmith.MOD_ID);

    public static final RegistryObject<SoundEvent> FORGE_CRACKLE = sound("forge_crackle");
    public static final RegistryObject<SoundEvent> FORGE_ROAR = sound("forge_roar");
    public static final RegistryObject<SoundEvent> BELLOWS_PUMP = sound("bellows_pump");
    public static final RegistryObject<SoundEvent> BELLOWS_AIR = sound("bellows_air");

    public static final RegistryObject<SoundEvent> HAMMER_IRON = sound("hammer_iron");
    public static final RegistryObject<SoundEvent> HAMMER_STEEL = sound("hammer_steel");
    public static final RegistryObject<SoundEvent> HAMMER_MASTER = sound("hammer_master");
    public static final RegistryObject<SoundEvent> HAMMER_MISS = sound("hammer_miss");
    public static final RegistryObject<SoundEvent> HAMMER_CRACK = sound("hammer_crack");
    public static final RegistryObject<SoundEvent> ANVIL_RING = sound("anvil_ring");

    public static final RegistryObject<SoundEvent> QUENCH_WATER = sound("quench_water");
    public static final RegistryObject<SoundEvent> QUENCH_OIL = sound("quench_oil");
    public static final RegistryObject<SoundEvent> QUENCH_SPECIAL = sound("quench_special");

    public static final RegistryObject<SoundEvent> GRIND_WHEEL = sound("grind_wheel");
    public static final RegistryObject<SoundEvent> GRIND_SPARK = sound("grind_spark");

    public static final RegistryObject<SoundEvent> ASSEMBLY_WOOD = sound("assembly_wood");
    public static final RegistryObject<SoundEvent> ASSEMBLY_LEATHER = sound("assembly_leather");
    public static final RegistryObject<SoundEvent> ASSEMBLY_RIVET = sound("assembly_rivet");

    public static final RegistryObject<SoundEvent> JOURNAL_PAGE = sound("journal_page");
    public static final RegistryObject<SoundEvent> MASTERWORK_COMPLETE = sound("masterwork_complete");
    public static final RegistryObject<SoundEvent> LEGENDARY_COMPLETE = sound("legendary_complete");
    public static final RegistryObject<SoundEvent> TONGS_CLANK = sound("tongs_clank");
    public static final RegistryObject<SoundEvent> BILLET_PLACE = sound("billet_place");

    private static RegistryObject<SoundEvent> sound(String name) {
        return SOUNDS.register(name,
                () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(MasterBlacksmith.MOD_ID, name)));
    }
}

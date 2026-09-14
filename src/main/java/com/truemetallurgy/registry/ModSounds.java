package com.truemetallurgy.registry;

import com.truemetallurgy.TrueMetallurgy;
import com.truemetallurgy.util.TMUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Custom sound events. Each has multiple variants in sounds.json to avoid
 * repetition. Gameplay code layers a quiet vanilla sound underneath so the
 * workshop still gives feedback if the .ogg pack is not installed yet.
 */
public final class ModSounds {
    private ModSounds() {}

    public static final DeferredRegister<SoundEvent> SOUNDS =
        DeferredRegister.create(Registries.SOUND_EVENT, TrueMetallurgy.MOD_ID);

    private static DeferredHolder<SoundEvent, SoundEvent> sound(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(TMUtil.rl(name)));
    }

    public static final DeferredHolder<SoundEvent, SoundEvent> FORGE_CRACKLE = sound("forge_crackle");
    public static final DeferredHolder<SoundEvent, SoundEvent> FORGE_ROAR = sound("forge_roar");
    public static final DeferredHolder<SoundEvent, SoundEvent> BELLOWS_WHOOSH = sound("bellows_whoosh");
    public static final DeferredHolder<SoundEvent, SoundEvent> BELLOWS_CREAK = sound("bellows_creak");
    public static final DeferredHolder<SoundEvent, SoundEvent> HAMMER_IRON = sound("hammer_iron");
    public static final DeferredHolder<SoundEvent, SoundEvent> HAMMER_STEEL = sound("hammer_steel");
    public static final DeferredHolder<SoundEvent, SoundEvent> HAMMER_MASTER = sound("hammer_master");
    public static final DeferredHolder<SoundEvent, SoundEvent> STRIKE_PERFECT = sound("strike_perfect");
    public static final DeferredHolder<SoundEvent, SoundEvent> STRIKE_BAD = sound("strike_bad");
    public static final DeferredHolder<SoundEvent, SoundEvent> QUENCH_WATER = sound("quench_water");
    public static final DeferredHolder<SoundEvent, SoundEvent> QUENCH_OIL = sound("quench_oil");
    public static final DeferredHolder<SoundEvent, SoundEvent> GRIND_LOOP = sound("grind_loop");
    public static final DeferredHolder<SoundEvent, SoundEvent> GRIND_SCRAPE = sound("grind_scrape");
    public static final DeferredHolder<SoundEvent, SoundEvent> ASSEMBLY_WOOD = sound("assembly_wood");
    public static final DeferredHolder<SoundEvent, SoundEvent> ASSEMBLY_METAL = sound("assembly_metal");
    public static final DeferredHolder<SoundEvent, SoundEvent> MASTERWORK_CHIME = sound("masterwork_chime");
    public static final DeferredHolder<SoundEvent, SoundEvent> JOURNAL_PAGE = sound("journal_page");
    public static final DeferredHolder<SoundEvent, SoundEvent> TONGS_CLINK = sound("tongs_clink");
}

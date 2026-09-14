package com.enderblade.registry;

import com.enderblade.EnderBladeMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, EnderBladeMod.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> EQUIP = register("equip");
    public static final DeferredHolder<SoundEvent, SoundEvent> SLASH = register("slash");
    public static final DeferredHolder<SoundEvent, SoundEvent> HEAVY_SLASH = register("heavy_slash");
    public static final DeferredHolder<SoundEvent, SoundEvent> VOID_SLASH = register("void_slash");
    public static final DeferredHolder<SoundEvent, SoundEvent> ECHO_LAUNCH = register("echo_launch");
    public static final DeferredHolder<SoundEvent, SoundEvent> TELEPORT = register("teleport");
    public static final DeferredHolder<SoundEvent, SoundEvent> ANCHOR = register("anchor");
    public static final DeferredHolder<SoundEvent, SoundEvent> RIFT_COLLAPSE = register("rift_collapse");
    public static final DeferredHolder<SoundEvent, SoundEvent> ULTIMATE = register("ultimate");
    public static final DeferredHolder<SoundEvent, SoundEvent> PARADOX = register("paradox");
    public static final DeferredHolder<SoundEvent, SoundEvent> MARK = register("mark");

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(EnderBladeMod.MOD_ID, name)));
    }

    private ModSounds() {
    }
}

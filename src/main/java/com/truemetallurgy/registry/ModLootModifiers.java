package com.truemetallurgy.registry;

import com.mojang.serialization.MapCodec;
import com.truemetallurgy.TrueMetallurgy;
import com.truemetallurgy.worldgen.WorkshopCacheModifier;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/** Global loot modifier serializers. */
public final class ModLootModifiers {
    private ModLootModifiers() {}

    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> SERIALIZERS =
        DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, TrueMetallurgy.MOD_ID);

    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<WorkshopCacheModifier>> WORKSHOP_CACHE =
        SERIALIZERS.register("workshop_cache", () -> WorkshopCacheModifier.CODEC);
}

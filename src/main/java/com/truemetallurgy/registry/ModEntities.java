package com.truemetallurgy.registry;

import com.truemetallurgy.TrueMetallurgy;
import com.truemetallurgy.entity.BlacksmithEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** NPC entities. */
public final class ModEntities {
    private ModEntities() {}

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
        DeferredRegister.create(Registries.ENTITY_TYPE, TrueMetallurgy.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<BlacksmithEntity>> BLACKSMITH =
        ENTITY_TYPES.register("blacksmith", () -> EntityType.Builder.of(BlacksmithEntity::new, MobCategory.CREATURE)
            .sized(0.6F, 1.95F)
            .eyeHeight(1.62F)
            .clientTrackingRange(10)
            .build(TrueMetallurgy.MOD_ID + ":blacksmith"));
}

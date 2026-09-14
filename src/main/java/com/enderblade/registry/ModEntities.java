package com.enderblade.registry;

import com.enderblade.EnderBladeMod;
import com.enderblade.entity.EnderPhantomProjectile;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Entity type registry — primarily the Ender Phantom projectile.
 */
public final class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, EnderBladeMod.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<EnderPhantomProjectile>> ENDER_PHANTOM =
            ENTITY_TYPES.register("ender_phantom", () ->
                    EntityType.Builder.<EnderPhantomProjectile>of(EnderPhantomProjectile::new, MobCategory.MISC)
                            .sized(0.35f, 0.35f)
                            .clientTrackingRange(8)
                            .updateInterval(1)
                            .build("ender_phantom")
            );

    private ModEntities() {
    }
}

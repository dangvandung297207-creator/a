package com.enderblade.registry;

import com.enderblade.EnderBladeMod;
import com.enderblade.entity.EndDimensionZoneEntity;
import com.enderblade.entity.EnderEchoProjectile;
import com.enderblade.entity.VoidAnchorEntity;
import com.enderblade.entity.VoidSlashEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, EnderBladeMod.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<EnderEchoProjectile>> ENDER_ECHO =
            ENTITY_TYPES.register("ender_echo", () ->
                    EntityType.Builder.<EnderEchoProjectile>of(EnderEchoProjectile::new, MobCategory.MISC)
                            .sized(0.4f, 0.4f)
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .build("ender_echo"));

    public static final DeferredHolder<EntityType<?>, EntityType<VoidAnchorEntity>> VOID_ANCHOR =
            ENTITY_TYPES.register("void_anchor", () ->
                    EntityType.Builder.<VoidAnchorEntity>of(VoidAnchorEntity::new, MobCategory.MISC)
                            .sized(0.5f, 0.8f)
                            .clientTrackingRange(10)
                            .updateInterval(2)
                            .fireImmune()
                            .build("void_anchor"));

    public static final DeferredHolder<EntityType<?>, EntityType<VoidSlashEntity>> VOID_SLASH =
            ENTITY_TYPES.register("void_slash", () ->
                    EntityType.Builder.<VoidSlashEntity>of(VoidSlashEntity::new, MobCategory.MISC)
                            .sized(0.1f, 0.1f)
                            .clientTrackingRange(8)
                            .updateInterval(1)
                            .build("void_slash"));

    public static final DeferredHolder<EntityType<?>, EntityType<EndDimensionZoneEntity>> END_DIMENSION_ZONE =
            ENTITY_TYPES.register("end_dimension_zone", () ->
                    EntityType.Builder.<EndDimensionZoneEntity>of(EndDimensionZoneEntity::new, MobCategory.MISC)
                            .sized(0.1f, 0.1f)
                            .clientTrackingRange(12)
                            .updateInterval(1)
                            .fireImmune()
                            .build("end_dimension_zone"));

    private ModEntities() {
    }
}

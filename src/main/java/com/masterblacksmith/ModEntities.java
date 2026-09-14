package com.masterblacksmith;

import com.masterblacksmith.entity.TravellingBlacksmithEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** The travelling blacksmith who trades tools, materials and blueprints. */
public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MasterBlacksmith.MOD_ID);

    public static final RegistryObject<EntityType<TravellingBlacksmithEntity>> TRAVELLING_BLACKSMITH =
            ENTITIES.register("travelling_blacksmith", () -> EntityType.Builder
                    .of(TravellingBlacksmithEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.95F).clientTrackingRange(10).build("travelling_blacksmith"));

    public static void registerAttributes(EntityAttributeCreationEvent event) {
        AttributeSupplier attrs = TravellingBlacksmithEntity.createAttributes().build();
        event.put(TRAVELLING_BLACKSMITH.get(), attrs);
    }
}

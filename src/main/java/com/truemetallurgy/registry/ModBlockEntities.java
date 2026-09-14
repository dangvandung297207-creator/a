package com.truemetallurgy.registry;

import com.truemetallurgy.TrueMetallurgy;
import com.truemetallurgy.blockentity.AssemblyTableBlockEntity;
import com.truemetallurgy.blockentity.BellowsBlockEntity;
import com.truemetallurgy.blockentity.BlacksmithAnvilBlockEntity;
import com.truemetallurgy.blockentity.ForgeHearthBlockEntity;
import com.truemetallurgy.blockentity.GrindingWheelBlockEntity;
import com.truemetallurgy.blockentity.QuenchingBarrelBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Block entity types. Forge and anvil types are shared across tiers. */
public final class ModBlockEntities {
    private ModBlockEntities() {}

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
        DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, TrueMetallurgy.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ForgeHearthBlockEntity>> FORGE =
        BLOCK_ENTITIES.register("forge", () -> BlockEntityType.Builder.of(
            ForgeHearthBlockEntity::new,
            ModBlocks.PRIMITIVE_FORGE.get(), ModBlocks.IRON_FORGE.get(),
            ModBlocks.STEEL_FORGE.get(), ModBlocks.MASTER_FORGE.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BellowsBlockEntity>> BELLOWS =
        BLOCK_ENTITIES.register("bellows", () -> BlockEntityType.Builder.of(
            BellowsBlockEntity::new, ModBlocks.BELLOWS.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlacksmithAnvilBlockEntity>> ANVIL =
        BLOCK_ENTITIES.register("anvil", () -> BlockEntityType.Builder.of(
            BlacksmithAnvilBlockEntity::new,
            ModBlocks.BASIC_ANVIL.get(), ModBlocks.IRON_ANVIL.get(),
            ModBlocks.STEEL_ANVIL.get(), ModBlocks.MASTER_ANVIL.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<QuenchingBarrelBlockEntity>> QUENCH_BARREL =
        BLOCK_ENTITIES.register("quenching_barrel", () -> BlockEntityType.Builder.of(
            QuenchingBarrelBlockEntity::new, ModBlocks.QUENCHING_BARREL.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GrindingWheelBlockEntity>> GRINDING_WHEEL =
        BLOCK_ENTITIES.register("grinding_wheel", () -> BlockEntityType.Builder.of(
            GrindingWheelBlockEntity::new, ModBlocks.GRINDING_WHEEL.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AssemblyTableBlockEntity>> ASSEMBLY_TABLE =
        BLOCK_ENTITIES.register("assembly_table", () -> BlockEntityType.Builder.of(
            AssemblyTableBlockEntity::new, ModBlocks.ASSEMBLY_TABLE.get()).build(null));
}

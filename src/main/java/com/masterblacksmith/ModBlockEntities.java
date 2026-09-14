package com.masterblacksmith;

import com.masterblacksmith.blockentity.AssemblyTableBlockEntity;
import com.masterblacksmith.blockentity.BellowsBlockEntity;
import com.masterblacksmith.blockentity.BlacksmithAnvilBlockEntity;
import com.masterblacksmith.blockentity.ForgeHearthBlockEntity;
import com.masterblacksmith.blockentity.GrindingWheelBlockEntity;
import com.masterblacksmith.blockentity.MetalShelfBlockEntity;
import com.masterblacksmith.blockentity.QuenchingBarrelBlockEntity;
import com.masterblacksmith.blockentity.ToolRackBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Block entity types for the whole workshop. */
public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MasterBlacksmith.MOD_ID);

    public static final RegistryObject<BlockEntityType<ForgeHearthBlockEntity>> FORGE_HEARTH =
            BLOCK_ENTITIES.register("forge_hearth", () -> BlockEntityType.Builder
                    .of(ForgeHearthBlockEntity::new,
                            ModBlocks.FORGE_HEARTH_PRIMITIVE.get(), ModBlocks.FORGE_HEARTH_IRON.get(),
                            ModBlocks.FORGE_HEARTH_STEEL.get(), ModBlocks.FORGE_HEARTH_MASTER.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<BellowsBlockEntity>> BELLOWS =
            BLOCK_ENTITIES.register("bellows", () -> BlockEntityType.Builder
                    .of(BellowsBlockEntity::new, ModBlocks.BELLOWS.get()).build(null));

    public static final RegistryObject<BlockEntityType<BlacksmithAnvilBlockEntity>> ANVIL =
            BLOCK_ENTITIES.register("anvil", () -> BlockEntityType.Builder
                    .of(BlacksmithAnvilBlockEntity::new,
                            ModBlocks.ANVIL_BASIC.get(), ModBlocks.ANVIL_IRON.get(),
                            ModBlocks.ANVIL_STEEL.get(), ModBlocks.ANVIL_MASTER.get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<QuenchingBarrelBlockEntity>> BARREL =
            BLOCK_ENTITIES.register("quenching_barrel", () -> BlockEntityType.Builder
                    .of(QuenchingBarrelBlockEntity::new, ModBlocks.QUENCHING_BARREL.get()).build(null));

    public static final RegistryObject<BlockEntityType<GrindingWheelBlockEntity>> GRINDING_WHEEL =
            BLOCK_ENTITIES.register("grinding_wheel", () -> BlockEntityType.Builder
                    .of(GrindingWheelBlockEntity::new, ModBlocks.GRINDING_WHEEL.get()).build(null));

    public static final RegistryObject<BlockEntityType<AssemblyTableBlockEntity>> ASSEMBLY_TABLE =
            BLOCK_ENTITIES.register("assembly_table", () -> BlockEntityType.Builder
                    .of(AssemblyTableBlockEntity::new, ModBlocks.ASSEMBLY_TABLE.get()).build(null));

    public static final RegistryObject<BlockEntityType<ToolRackBlockEntity>> TOOL_RACK =
            BLOCK_ENTITIES.register("tool_rack", () -> BlockEntityType.Builder
                    .of(ToolRackBlockEntity::new, ModBlocks.TOOL_RACK.get()).build(null));

    public static final RegistryObject<BlockEntityType<MetalShelfBlockEntity>> METAL_SHELF =
            BLOCK_ENTITIES.register("metal_shelf", () -> BlockEntityType.Builder
                    .of(MetalShelfBlockEntity::new, ModBlocks.METAL_SHELF.get()).build(null));
}

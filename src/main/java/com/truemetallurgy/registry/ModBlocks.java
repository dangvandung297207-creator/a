package com.truemetallurgy.registry;

import com.truemetallurgy.TrueMetallurgy;
import com.truemetallurgy.block.AssemblyTableBlock;
import com.truemetallurgy.block.BellowsBlock;
import com.truemetallurgy.block.BlacksmithAnvilBlock;
import com.truemetallurgy.block.AnvilTier;
import com.truemetallurgy.block.ForgeHearthBlock;
import com.truemetallurgy.block.ForgeTier;
import com.truemetallurgy.block.GrindingWheelBlock;
import com.truemetallurgy.block.QuenchingBarrelBlock;
import com.truemetallurgy.block.ToolRackBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** All workstation and decoration blocks. */
public final class ModBlocks {
    private ModBlocks() {}

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, TrueMetallurgy.MOD_ID);

    private static BlockBehaviour.Properties forgeProps(ForgeTier tier) {
        return BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_BROWN)
            .strength(4.0F, 1200.0F)
            .sound(SoundType.STONE)
            .requiresCorrectToolForDrops()
            .lightLevel(state -> ForgeHearthBlock.heatLight(state));
    }

    private static BlockBehaviour.Properties anvilProps() {
        return BlockBehaviour.Properties.of()
            .mapColor(MapColor.METAL)
            .strength(5.0F, 1200.0F)
            .sound(SoundType.ANVIL)
            .requiresCorrectToolForDrops()
            .noOcclusion();
    }

    public static final DeferredHolder<Block, ForgeHearthBlock> PRIMITIVE_FORGE = BLOCKS.register("primitive_forge",
        () -> new ForgeHearthBlock(forgeProps(ForgeTier.PRIMITIVE), ForgeTier.PRIMITIVE));
    public static final DeferredHolder<Block, ForgeHearthBlock> IRON_FORGE = BLOCKS.register("iron_forge",
        () -> new ForgeHearthBlock(forgeProps(ForgeTier.IRON), ForgeTier.IRON));
    public static final DeferredHolder<Block, ForgeHearthBlock> STEEL_FORGE = BLOCKS.register("steel_forge",
        () -> new ForgeHearthBlock(forgeProps(ForgeTier.STEEL), ForgeTier.STEEL));
    public static final DeferredHolder<Block, ForgeHearthBlock> MASTER_FORGE = BLOCKS.register("master_forge",
        () -> new ForgeHearthBlock(forgeProps(ForgeTier.MASTER), ForgeTier.MASTER));

    public static final DeferredHolder<Block, BellowsBlock> BELLOWS = BLOCKS.register("bellows",
        () -> new BellowsBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.WOOD).strength(2.0F).sound(SoundType.WOOD).noOcclusion()));

    public static final DeferredHolder<Block, BlacksmithAnvilBlock> BASIC_ANVIL = BLOCKS.register("basic_anvil",
        () -> new BlacksmithAnvilBlock(anvilProps(), AnvilTier.BASIC));
    public static final DeferredHolder<Block, BlacksmithAnvilBlock> IRON_ANVIL = BLOCKS.register("iron_anvil",
        () -> new BlacksmithAnvilBlock(anvilProps(), AnvilTier.IRON));
    public static final DeferredHolder<Block, BlacksmithAnvilBlock> STEEL_ANVIL = BLOCKS.register("steel_anvil",
        () -> new BlacksmithAnvilBlock(anvilProps(), AnvilTier.STEEL));
    public static final DeferredHolder<Block, BlacksmithAnvilBlock> MASTER_ANVIL = BLOCKS.register("master_anvil",
        () -> new BlacksmithAnvilBlock(anvilProps(), AnvilTier.MASTER));

    public static final DeferredHolder<Block, QuenchingBarrelBlock> QUENCHING_BARREL = BLOCKS.register("quenching_barrel",
        () -> new QuenchingBarrelBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.WOOD).strength(2.5F).sound(SoundType.WOOD).noOcclusion()));

    public static final DeferredHolder<Block, GrindingWheelBlock> GRINDING_WHEEL = BLOCKS.register("grinding_wheel",
        () -> new GrindingWheelBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.STONE).strength(3.5F).sound(SoundType.STONE).noOcclusion()));

    public static final DeferredHolder<Block, AssemblyTableBlock> ASSEMBLY_TABLE = BLOCKS.register("assembly_table",
        () -> new AssemblyTableBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.WOOD).strength(2.5F).sound(SoundType.WOOD).noOcclusion()));

    public static final DeferredHolder<Block, Block> STEEL_BLOCK = BLOCKS.register("steel_block",
        () -> new Block(BlockBehaviour.Properties.of()
            .mapColor(MapColor.METAL).strength(6.0F, 1200.0F)
            .sound(SoundType.METAL).requiresCorrectToolForDrops()));

    public static final DeferredHolder<Block, ToolRackBlock> TOOL_RACK = BLOCKS.register("tool_rack",
        () -> new ToolRackBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.WOOD).strength(2.0F).sound(SoundType.WOOD).noOcclusion()));
}

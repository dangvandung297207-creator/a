package com.masterblacksmith;

import com.masterblacksmith.block.AssemblyTableBlock;
import com.masterblacksmith.block.BellowsBlock;
import com.masterblacksmith.block.BlacksmithAnvilBlock;
import com.masterblacksmith.block.ForgeHearthBlock;
import com.masterblacksmith.block.ForgeTier;
import com.masterblacksmith.block.GrindingWheelBlock;
import com.masterblacksmith.block.MetalShelfBlock;
import com.masterblacksmith.block.QuenchingBarrelBlock;
import com.masterblacksmith.block.ToolRackBlock;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

/** All workshop, ore and storage blocks. */
public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, MasterBlacksmith.MOD_ID);

    // --- Building / refractory ---
    public static final RegistryObject<Block> REFRACTORY_BRICK_BLOCK = registerBlock("refractory_brick_block",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).strength(3.5F, 6.0F)
                    .sound(SoundType.STONE).requiresCorrectToolForDrops()));

    // --- Forge hearths (4 tiers) ---
    public static final RegistryObject<Block> FORGE_HEARTH_PRIMITIVE = registerBlock("forge_hearth_primitive",
            () -> new ForgeHearthBlock(ForgeTier.PRIMITIVE, hearthProps()));
    public static final RegistryObject<Block> FORGE_HEARTH_IRON = registerBlock("forge_hearth_iron",
            () -> new ForgeHearthBlock(ForgeTier.IRON, hearthProps()));
    public static final RegistryObject<Block> FORGE_HEARTH_STEEL = registerBlock("forge_hearth_steel",
            () -> new ForgeHearthBlock(ForgeTier.STEEL, hearthProps()));
    public static final RegistryObject<Block> FORGE_HEARTH_MASTER = registerBlock("forge_hearth_master",
            () -> new ForgeHearthBlock(ForgeTier.MASTER, hearthProps()));

    private static BlockBehaviour.Properties hearthProps() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN).strength(4.0F, 10.0F)
                .sound(SoundType.STONE).requiresCorrectToolForDrops()
                .lightLevel(state -> Math.min(15, state.getValue(ForgeHearthBlock.HEAT) * 3));
    }

    // --- Workshop ---
    public static final RegistryObject<Block> BELLOWS = registerBlock("bellows",
            () -> new BellowsBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0F)
                    .sound(SoundType.WOOD).noOcclusion()));
    public static final RegistryObject<Block> ANVIL_BASIC = registerBlock("anvil_basic",
            () -> new BlacksmithAnvilBlock(0, anvilProps()));
    public static final RegistryObject<Block> ANVIL_IRON = registerBlock("anvil_iron",
            () -> new BlacksmithAnvilBlock(1, anvilProps()));
    public static final RegistryObject<Block> ANVIL_STEEL = registerBlock("anvil_steel",
            () -> new BlacksmithAnvilBlock(2, anvilProps()));
    public static final RegistryObject<Block> ANVIL_MASTER = registerBlock("anvil_master",
            () -> new BlacksmithAnvilBlock(3, anvilProps()));

    private static BlockBehaviour.Properties anvilProps() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F, 12.0F)
                .sound(SoundType.ANVIL).requiresCorrectToolForDrops().noOcclusion()
                .lightLevel(state -> state.getValue(BlacksmithAnvilBlock.HOT) ? 7 : 0);
    }

    public static final RegistryObject<Block> QUENCHING_BARREL = registerBlock("quenching_barrel",
            () -> new QuenchingBarrelBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD)
                    .strength(2.0F).sound(SoundType.WOOD).noOcclusion()));
    public static final RegistryObject<Block> GRINDING_WHEEL = registerBlock("grinding_wheel",
            () -> new GrindingWheelBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE)
                    .strength(3.0F).sound(SoundType.STONE).noOcclusion()));
    public static final RegistryObject<Block> ASSEMBLY_TABLE = registerBlock("assembly_table",
            () -> new AssemblyTableBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD)
                    .strength(2.5F).sound(SoundType.WOOD).noOcclusion()));
    public static final RegistryObject<Block> TOOL_RACK = registerBlock("tool_rack",
            () -> new ToolRackBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD)
                    .strength(1.5F).sound(SoundType.WOOD).noOcclusion()));
    public static final RegistryObject<Block> METAL_SHELF = registerBlock("metal_shelf",
            () -> new MetalShelfBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL)
                    .strength(3.0F).sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion()));

    // --- Ores / metal storage ---
    public static final RegistryObject<Block> TIN_ORE = registerBlock("tin_ore",
            () -> new DropExperienceBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE)
                    .strength(3.0F, 3.0F).requiresCorrectToolForDrops(), UniformInt.of(0, 2)));
    public static final RegistryObject<Block> DEEPSLATE_TIN_ORE = registerBlock("deepslate_tin_ore",
            () -> new DropExperienceBlock(BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE)
                    .strength(4.5F, 3.0F).requiresCorrectToolForDrops().sound(SoundType.DEEPSLATE), UniformInt.of(0, 2)));
    public static final RegistryObject<Block> STARFALL_ORE = registerBlock("starfall_ore",
            () -> new DropExperienceBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK)
                    .strength(6.0F, 6.0F).requiresCorrectToolForDrops()
                    .lightLevel(state -> 4), UniformInt.of(3, 7)));
    public static final RegistryObject<Block> TIN_BLOCK = registerBlock("tin_block",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(4.0F, 6.0F)
                    .sound(SoundType.METAL).requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> RAW_TIN_BLOCK = registerBlock("raw_tin_block",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.RAW_IRON).strength(4.0F, 6.0F)
                    .sound(SoundType.METAL).requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> BRONZE_BLOCK = registerBlock("bronze_block",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).strength(4.5F, 6.0F)
                    .sound(SoundType.METAL).requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> STEEL_BLOCK = registerBlock("steel_block",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F, 7.0F)
                    .sound(SoundType.METAL).requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> STARFALL_BLOCK = registerBlock("starfall_block",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(6.0F, 9.0F)
                    .sound(SoundType.METAL).requiresCorrectToolForDrops().lightLevel(state -> 6)));

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> sup) {
        RegistryObject<T> block = BLOCKS.register(name, sup);
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
        return block;
    }
}

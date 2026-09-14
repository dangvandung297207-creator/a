package com.truemetallurgy.datagen;

import com.truemetallurgy.TrueMetallurgy;
import com.truemetallurgy.block.BellowsBlock;
import com.truemetallurgy.block.ForgeHearthBlock;
import com.truemetallurgy.block.GrindingWheelBlock;
import com.truemetallurgy.block.QuenchingBarrelBlock;
import com.truemetallurgy.registry.ModBlocks;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

/** Blockstate + block model generation. */
public class TMBlockStates extends BlockStateProvider {
    public TMBlockStates(PackOutput output, ExistingFileHelper files) {
        super(output, TrueMetallurgy.MOD_ID, files);
    }

    @Override
    protected void registerStatesAndModels() {
        forgeBlock(ModBlocks.PRIMITIVE_FORGE.get(), "primitive");
        forgeBlock(ModBlocks.IRON_FORGE.get(), "iron");
        forgeBlock(ModBlocks.STEEL_FORGE.get(), "steel");
        forgeBlock(ModBlocks.MASTER_FORGE.get(), "master");

        // Bellows: facing x pump stage.
        getVariantBuilder(ModBlocks.BELLOWS.get()).forAllStates(state -> {
            int stage = state.getValue(BellowsBlock.STAGE);
            Direction facing = state.getValue(BellowsBlock.FACING);
            return ConfiguredModel.builder()
                .modelFile(existing("bellows_" + stage))
                .rotationY(facingRotation(facing))
                .build();
        });

        anvilBlock(ModBlocks.BASIC_ANVIL.get(), "basic");
        anvilBlock(ModBlocks.IRON_ANVIL.get(), "iron");
        anvilBlock(ModBlocks.STEEL_ANVIL.get(), "steel");
        anvilBlock(ModBlocks.MASTER_ANVIL.get(), "master");

        // Quenching barrel: liquid variants.
        getVariantBuilder(ModBlocks.QUENCHING_BARREL.get()).forAllStates(state ->
            ConfiguredModel.builder()
                .modelFile(existing("quenching_barrel_" + state.getValue(QuenchingBarrelBlock.LIQUID).getSerializedName()))
                .build());

        // Grinding wheel: facing x spin stage.
        getVariantBuilder(ModBlocks.GRINDING_WHEEL.get()).forAllStates(state -> {
            int spin = state.getValue(GrindingWheelBlock.SPIN);
            Direction facing = state.getValue(GrindingWheelBlock.FACING);
            return ConfiguredModel.builder()
                .modelFile(existing("grinding_wheel_" + spin))
                .rotationY(facingRotation(facing))
                .build();
        });

        horizontalBlock(ModBlocks.ASSEMBLY_TABLE.get(), existing("assembly_table"));
        horizontalBlock(ModBlocks.TOOL_RACK.get(), existing("tool_rack"));
        simpleBlock(ModBlocks.STEEL_BLOCK.get());
    }

    private void forgeBlock(Block block, String tier) {
        ResourceLocation side = modLoc("block/forge_" + tier + "_side");
        ResourceLocation top = modLoc("block/forge_" + tier + "_top");
        getVariantBuilder(block).forAllStates(state -> {
            int heat = state.getValue(ForgeHearthBlock.HEAT);
            Direction facing = state.getValue(ForgeHearthBlock.FACING);
            return ConfiguredModel.builder()
                .modelFile(models().cube("forge_" + tier + "_" + heat, side, top,
                    modLoc("block/forge_front_" + heat), side, side, side).texture("particle", side))
                .rotationY(facingRotation(facing))
                .build();
        });
    }

    private void anvilBlock(Block block, String tier) {
        horizontalBlock(block, existing("anvil_" + tier));
    }

    private ModelFile existing(String name) {
        return models().getExistingFile(modLoc("block/" + name));
    }

    private static int facingRotation(Direction facing) {
        return ((facing.get2DDataValue() + 2) % 4) * 90;
    }
}

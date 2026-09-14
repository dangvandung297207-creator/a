package com.masterblacksmith.worldgen;

import com.masterblacksmith.MasterBlacksmith;
import com.masterblacksmith.ModBlocks;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/** A small surface ruin: floor, forge, anvil, barrel and a looted chest. */
public class RuinedSmithyFeature extends Feature<NoneFeatureConfiguration> {
    private static final ResourceLocation CHEST_LOOT =
            new ResourceLocation(MasterBlacksmith.MOD_ID, "chests/ruined_smithy");

    public RuinedSmithyFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level();
        BlockPos origin = ctx.origin();
        RandomSource rand = ctx.random();

        // Floor 7 x 5.
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                BlockPos p = origin.offset(dx, 0, dz);
                if (rand.nextFloat() < 0.12F) continue;
                level.setBlock(p, rand.nextFloat() < 0.7F
                        ? Blocks.COBBLESTONE.defaultBlockState()
                        : ModBlocks.REFRACTORY_BRICK_BLOCK.get().defaultBlockState(), 2);
            }
        }
        // Back wall stubs.
        for (int dx = -3; dx <= 3; dx++) {
            if (rand.nextFloat() < 0.35F) continue;
            BlockPos p = origin.offset(dx, 1, -2);
            level.setBlock(p, Blocks.COBBLESTONE.defaultBlockState(), 2);
            if (Math.abs(dx) <= 1 && rand.nextFloat() < 0.6F) {
                level.setBlock(p.above(), Blocks.COBBLESTONE.defaultBlockState(), 2);
            }
        }
        // Corner pillars.
        for (int[] c : new int[][]{{-3, 2}, {3, 2}, {-3, -2}, {3, -2}}) {
            int h = 1 + rand.nextInt(3);
            for (int i = 1; i <= h; i++) {
                BlockPos p = origin.offset(c[0], i, c[1]);
                level.setBlock(p, (rand.nextFloat() < 0.3F ? Blocks.MOSSY_COBBLESTONE : Blocks.COBBLESTONE).defaultBlockState(), 2);
            }
        }
        // The old forge against the back wall.
        BlockPos forgePos = origin.offset(-1, 1, -1);
        level.setBlock(forgePos, ModBlocks.FORGE_HEARTH_PRIMITIVE.get().defaultBlockState(), 2);
        // An anvil that survived.
        BlockPos anvilPos = origin.offset(1, 1, 0);
        level.setBlock(anvilPos, ModBlocks.ANVIL_BASIC.get().defaultBlockState(), 2);
        // A dry barrel.
        BlockPos barrelPos = origin.offset(2, 1, -1);
        level.setBlock(barrelPos, ModBlocks.QUENCHING_BARREL.get().defaultBlockState(), 2);
        // Smith's chest.
        BlockPos chestPos = origin.offset(-2, 1, 1);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState()
                .setValue(ChestBlock.FACING, net.minecraft.core.Direction.SOUTH), 2);
        if (level.getBlockEntity(chestPos) instanceof RandomizableContainerBlockEntity chest) {
            chest.setLootTable(CHEST_LOOT, rand.nextLong());
        }
        return true;
    }
}

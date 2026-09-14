package com.truemetallurgy.worldgen;

import com.mojang.serialization.Codec;
import com.truemetallurgy.entity.BlacksmithEntity;
import com.truemetallurgy.registry.ModBlocks;
import com.truemetallurgy.registry.ModEntities;
import com.truemetallurgy.util.TMUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * A small ruined smithy: stone floor, corner posts, a dead forge, an anvil,
 * a barrel and a looted chest. Sometimes the old smith never left.
 */
public class AbandonedForgeFeature extends Feature<NoneFeatureConfiguration> {
    public AbandonedForgeFeature() {
        this(NoneFeatureConfiguration.CODEC);
    }

    public AbandonedForgeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        if (origin.getY() < 63) return false;
        if (!level.getBlockState(origin.below()).isSolid()) return false;

        // Stone floor 5x5.
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                BlockPos p = origin.offset(dx, -1, dz);
                BlockState floor = random.nextFloat() < 0.25
                    ? Blocks.MOSSY_COBBLESTONE.defaultBlockState()
                    : random.nextFloat() < 0.3
                        ? Blocks.COBBLESTONE.defaultBlockState()
                        : Blocks.STONE_BRICKS.defaultBlockState();
                level.setBlock(p, floor, 2);
            }
        }
        // Corner posts.
        for (int sx : new int[]{-2, 2}) {
            for (int sz : new int[]{-2, 2}) {
                BlockPos post = origin.offset(sx, 0, sz);
                if (random.nextFloat() < 0.85) {
                    setIfEmpty(level, post, Blocks.OAK_LOG.defaultBlockState());
                    if (random.nextFloat() < 0.6) {
                        setIfEmpty(level, post.above(), Blocks.OAK_LOG.defaultBlockState());
                    }
                }
            }
        }
        // Dead forge at the back.
        BlockPos forgePos = origin.offset(0, 0, -1);
        setIfEmpty(level, forgePos, ModBlocks.PRIMITIVE_FORGE.get().defaultBlockState()
            .setValue(com.truemetallurgy.block.ForgeHearthBlock.FACING, Direction.SOUTH));
        // Anvil + barrel.
        setIfEmpty(level, origin.offset(-1, 0, 1), ModBlocks.BASIC_ANVIL.get().defaultBlockState());
        setIfEmpty(level, origin.offset(2, 0, 0), ModBlocks.QUENCHING_BARREL.get().defaultBlockState());
        setIfEmpty(level, origin.offset(2, 0, 1), ModBlocks.TOOL_RACK.get().defaultBlockState()
            .setValue(com.truemetallurgy.block.ToolRackBlock.FACING, Direction.WEST));
        // Loot chest.
        BlockPos chestPos = origin.offset(1, 0, 1);
        if (level.isEmptyBlock(chestPos)) {
            level.setBlock(chestPos, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.NORTH), 2);
            if (level.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
                chest.setLootTable(ResourceKey.create(Registries.LOOT_TABLE, TMUtil.rl("chests/abandoned_forge")),
                    random.nextLong());
            }
        }
        // The old smith sometimes stayed behind.
        if (random.nextFloat() < 0.4) {
            BlacksmithEntity smith = ModEntities.BLACKSMITH.get().create(level.getLevel());
            if (smith != null) {
                smith.moveTo(origin.getX() + 0.5, origin.getY(), origin.getZ() + 0.5, random.nextFloat() * 360.0F, 0.0F);
                smith.finalizeSpawn(level, level.getCurrentDifficultyAt(origin), MobSpawnType.STRUCTURE, null);
                smith.setPersistenceRequired();
                level.addFreshEntityWithPassengers(smith);
            }
        }
        return true;
    }

    private static void setIfEmpty(WorldGenLevel level, BlockPos pos, BlockState state) {
        if (level.isEmptyBlock(pos)) {
            level.setBlock(pos, state, 2);
        }
    }
}

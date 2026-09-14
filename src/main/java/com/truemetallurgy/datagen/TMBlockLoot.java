package com.truemetallurgy.datagen;

import com.truemetallurgy.registry.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

import java.util.Set;

/** Every workstation drops itself. */
public class TMBlockLoot extends BlockLootSubProvider {
    public TMBlockLoot(HolderLookup.Provider provider) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), provider);
    }

    @Override
    protected void generate() {
        dropSelf(ModBlocks.PRIMITIVE_FORGE.get());
        dropSelf(ModBlocks.IRON_FORGE.get());
        dropSelf(ModBlocks.STEEL_FORGE.get());
        dropSelf(ModBlocks.MASTER_FORGE.get());
        dropSelf(ModBlocks.BELLOWS.get());
        dropSelf(ModBlocks.BASIC_ANVIL.get());
        dropSelf(ModBlocks.IRON_ANVIL.get());
        dropSelf(ModBlocks.STEEL_ANVIL.get());
        dropSelf(ModBlocks.MASTER_ANVIL.get());
        dropSelf(ModBlocks.QUENCHING_BARREL.get());
        dropSelf(ModBlocks.GRINDING_WHEEL.get());
        dropSelf(ModBlocks.ASSEMBLY_TABLE.get());
        dropSelf(ModBlocks.STEEL_BLOCK.get());
        dropSelf(ModBlocks.TOOL_RACK.get());
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries().stream().map(e -> (Block) e.value()).toList();
    }
}

package com.truemetallurgy.datagen;

import com.truemetallurgy.TrueMetallurgy;
import com.truemetallurgy.registry.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

/** Mining/tool tags for workstations. */
public class TMBlockTags extends BlockTagsProvider {
    public TMBlockTags(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, ExistingFileHelper files) {
        super(output, lookup, TrueMetallurgy.MOD_ID, files);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .add(ModBlocks.PRIMITIVE_FORGE.get(), ModBlocks.IRON_FORGE.get(),
                ModBlocks.STEEL_FORGE.get(), ModBlocks.MASTER_FORGE.get(),
                ModBlocks.BASIC_ANVIL.get(), ModBlocks.IRON_ANVIL.get(),
                ModBlocks.STEEL_ANVIL.get(), ModBlocks.MASTER_ANVIL.get(),
                ModBlocks.GRINDING_WHEEL.get(), ModBlocks.STEEL_BLOCK.get());
        tag(BlockTags.MINEABLE_WITH_AXE)
            .add(ModBlocks.BELLOWS.get(), ModBlocks.QUENCHING_BARREL.get(),
                ModBlocks.ASSEMBLY_TABLE.get(), ModBlocks.TOOL_RACK.get());
        tag(BlockTags.NEEDS_STONE_TOOL)
            .add(ModBlocks.PRIMITIVE_FORGE.get(), ModBlocks.IRON_FORGE.get(),
                ModBlocks.BASIC_ANVIL.get(), ModBlocks.IRON_ANVIL.get(),
                ModBlocks.GRINDING_WHEEL.get());
        tag(BlockTags.NEEDS_IRON_TOOL)
            .add(ModBlocks.STEEL_FORGE.get(), ModBlocks.MASTER_FORGE.get(),
                ModBlocks.STEEL_ANVIL.get(), ModBlocks.MASTER_ANVIL.get(),
                ModBlocks.STEEL_BLOCK.get());
    }
}

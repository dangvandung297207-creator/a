package com.truemetallurgy.datagen;

import com.truemetallurgy.TrueMetallurgy;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Data generation entry point ({@code ./gradlew runData}). Client assets go
 * with {@code includeClient}, server data with {@code includeServer}.
 * Hand-authored data (custom alloy/assembly recipes, worldgen, loot
 * modifiers, sounds, advancements) lives in {@code src/main/resources}.
 */
public final class TMDataGen {
    private TMDataGen() {}

    public static void gatherData(GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();
        PackOutput output = gen.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookup = event.getLookupProvider();
        ExistingFileHelper files = event.getExistingFileHelper();

        gen.addProvider(event.includeClient(), new TMBlockStates(output, files));
        gen.addProvider(event.includeClient(), new TMItemModels(output, files));
        gen.addProvider(event.includeClient(), new TMLang(output));

        gen.addProvider(event.includeServer(), new TMRecipes(output, lookup));
        gen.addProvider(event.includeServer(), new LootTableProvider(output, Collections.emptySet(),
            List.of(new LootTableProvider.SubProviderEntry(TMBlockLoot::new, LootContextParamSets.BLOCK)), lookup));
        TMBlockTags blockTags = new TMBlockTags(output, lookup, files);
        gen.addProvider(event.includeServer(), blockTags);
        gen.addProvider(event.includeServer(), new TMItemTags(output, lookup, blockTags.contentsGetter(), files));
    }
}

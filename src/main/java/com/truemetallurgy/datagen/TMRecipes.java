package com.truemetallurgy.datagen;

import com.truemetallurgy.registry.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.concurrent.CompletableFuture;

/**
 * Crafting + furnace recipes. Custom forge-alloy and assembly recipes are
 * hand-authored data files (see {@code data/true_metallurgy/recipes}).
 */
public class TMRecipes extends RecipeProvider {
    public TMRecipes(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        super(output, lookup);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        // ---------- tools ----------
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.TONGS.get())
            .pattern("I I").pattern(" I ").pattern(" I ")
            .define('I', Items.IRON_INGOT)
            .unlockedBy("has_iron", has(Items.IRON_INGOT)).save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.PRIMITIVE_HAMMER.get())
            .pattern("CC ").pattern("CC ").pattern(" S ")
            .define('C', Items.COBBLESTONE).define('S', Items.STICK)
            .unlockedBy("has_cobble", has(Items.COBBLESTONE)).save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.COPPER_HAMMER.get())
            .pattern("CC ").pattern("CC ").pattern(" S ")
            .define('C', Items.COPPER_INGOT).define('S', Items.STICK)
            .unlockedBy("has_copper", has(Items.COPPER_INGOT)).save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.IRON_HAMMER.get())
            .pattern("II ").pattern("II ").pattern(" S ")
            .define('I', Items.IRON_INGOT).define('S', Items.STICK)
            .unlockedBy("has_iron", has(Items.IRON_INGOT)).save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.STEEL_HAMMER.get())
            .pattern("BB ").pattern("BB ").pattern(" S ")
            .define('B', ModItems.STEEL_BILLET.get()).define('S', Items.STICK)
            .unlockedBy("has_steel", has(ModItems.STEEL_BILLET.get())).save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.HARDENED_HAMMER.get())
            .pattern("BB ").pattern("BB ").pattern(" S ")
            .define('B', ModItems.HARDENED_BILLET.get()).define('S', Items.STICK)
            .unlockedBy("has_hardened", has(ModItems.HARDENED_BILLET.get())).save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.MASTERWORK_HAMMER.get())
            .pattern("HHH").pattern("HS ").pattern(" S ")
            .define('H', ModItems.HARDENED_BILLET.get()).define('S', Items.STICK)
            .unlockedBy("has_hardened", has(ModItems.HARDENED_BILLET.get())).save(output);

        // ---------- workstations ----------
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.PRIMITIVE_FORGE.get())
            .pattern("BBB").pattern("BFB").pattern("BBB")
            .define('B', Items.STONE_BRICKS).define('F', Items.FURNACE)
            .unlockedBy("has_furnace", has(Items.FURNACE)).save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.IRON_FORGE.get())
            .pattern("III").pattern("IFI").pattern("III")
            .define('I', Items.IRON_BLOCK).define('F', ModItems.PRIMITIVE_FORGE.get())
            .unlockedBy("has_forge", has(ModItems.PRIMITIVE_FORGE.get())).save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.STEEL_FORGE.get())
            .pattern("IBI").pattern("BFB").pattern("IBI")
            .define('I', Items.IRON_BLOCK).define('B', ModItems.STEEL_BILLET.get())
            .define('F', ModItems.IRON_FORGE.get())
            .unlockedBy("has_steel", has(ModItems.STEEL_BILLET.get())).save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.MASTER_FORGE.get())
            .pattern("HOH").pattern("OFO").pattern("HOH")
            .define('H', ModItems.HARDENED_BILLET.get()).define('O', Items.OBSIDIAN)
            .define('F', ModItems.STEEL_FORGE.get())
            .unlockedBy("has_hardened", has(ModItems.HARDENED_BILLET.get())).save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.BELLOWS.get())
            .pattern("PPP").pattern("PLP").pattern("PIP")
            .define('P', ItemTags.PLANKS).define('L', Items.LEATHER).define('I', Items.IRON_INGOT)
            .unlockedBy("has_leather", has(Items.LEATHER)).save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.BASIC_ANVIL.get())
            .pattern("III").pattern(" I ").pattern("CCC")
            .define('I', Items.IRON_INGOT).define('C', Items.COBBLESTONE)
            .unlockedBy("has_iron", has(Items.IRON_INGOT)).save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.IRON_ANVIL.get())
            .pattern("III").pattern("IAI").pattern("III")
            .define('I', Items.IRON_INGOT).define('A', ModItems.BASIC_ANVIL.get())
            .unlockedBy("has_anvil", has(ModItems.BASIC_ANVIL.get())).save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.STEEL_ANVIL.get())
            .pattern("SSS").pattern("SAS").pattern("SSS")
            .define('S', ModItems.STEEL_BILLET.get()).define('A', ModItems.IRON_ANVIL.get())
            .unlockedBy("has_steel", has(ModItems.STEEL_BILLET.get())).save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.MASTER_ANVIL.get())
            .pattern("HHH").pattern("HAH").pattern("OOO")
            .define('H', ModItems.HARDENED_BILLET.get()).define('A', ModItems.STEEL_ANVIL.get())
            .define('O', Items.OBSIDIAN)
            .unlockedBy("has_hardened", has(ModItems.HARDENED_BILLET.get())).save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.QUENCHING_BARREL.get())
            .pattern("PIP").pattern("P P").pattern("PIP")
            .define('P', ItemTags.PLANKS).define('I', Items.IRON_INGOT)
            .unlockedBy("has_iron", has(Items.IRON_INGOT)).save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.GRINDING_WHEEL.get())
            .pattern("S S").pattern("SXS").pattern("S S")
            .define('S', Items.STONE).define('X', Items.STICK)
            .unlockedBy("has_stone", has(Items.STONE)).save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.ASSEMBLY_TABLE.get())
            .pattern("PPP").pattern("PTP").pattern("PPP")
            .define('P', ItemTags.PLANKS).define('T', Items.CRAFTING_TABLE)
            .unlockedBy("has_table", has(Items.CRAFTING_TABLE)).save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.TOOL_RACK.get())
            .pattern("S S").pattern("PPP").pattern("S S")
            .define('S', Items.STICK).define('P', ItemTags.PLANKS)
            .unlockedBy("has_stick", has(Items.STICK)).save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModItems.STEEL_BLOCK.get())
            .pattern("BB").pattern("BB")
            .define('B', ModItems.STEEL_BILLET.get())
            .unlockedBy("has_steel", has(ModItems.STEEL_BILLET.get())).save(output);

        // ---------- metallurgy pipeline ----------
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.CRUSHED_IRON_ORE.get())
            .requires(Items.RAW_IRON)
            .unlockedBy("has_raw", has(Items.RAW_IRON)).save(output, "crushed_iron_ore");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.CRUSHED_COPPER_ORE.get())
            .requires(Items.RAW_COPPER)
            .unlockedBy("has_raw", has(Items.RAW_COPPER)).save(output, "crushed_copper_ore");
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(ModItems.CRUSHED_IRON_ORE.get()),
            RecipeCategory.MISC, ModItems.IRON_BLOOM.get(), 0.7F, 200)
            .unlockedBy("has_crushed", has(ModItems.CRUSHED_IRON_ORE.get())).save(output, "iron_bloom_smelting");
        SimpleCookingRecipeBuilder.blasting(Ingredient.of(ModItems.CRUSHED_IRON_ORE.get()),
            RecipeCategory.MISC, ModItems.IRON_BLOOM.get(), 0.7F, 100)
            .unlockedBy("has_crushed", has(ModItems.CRUSHED_IRON_ORE.get())).save(output, "iron_bloom_blasting");
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(ModItems.CRUSHED_COPPER_ORE.get()),
            RecipeCategory.MISC, ModItems.COPPER_BLOOM.get(), 0.7F, 200)
            .unlockedBy("has_crushed", has(ModItems.CRUSHED_COPPER_ORE.get())).save(output, "copper_bloom_smelting");
        SimpleCookingRecipeBuilder.blasting(Ingredient.of(ModItems.CRUSHED_COPPER_ORE.get()),
            RecipeCategory.MISC, ModItems.COPPER_BLOOM.get(), 0.7F, 100)
            .unlockedBy("has_crushed", has(ModItems.CRUSHED_COPPER_ORE.get())).save(output, "copper_bloom_blasting");
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Items.COAL),
            RecipeCategory.MISC, ModItems.COKE.get(), 0.5F, 200)
            .unlockedBy("has_coal", has(Items.COAL)).save(output, "coke_smelting");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.QUENCH_OIL.get(), 2)
            .requires(Items.COAL).requires(Items.CHARCOAL).requires(Items.DRIED_KELP).requires(Items.GLASS_BOTTLE)
            .unlockedBy("has_coal", has(Items.COAL)).save(output, "quench_oil");

        // ---------- handles & fittings ----------
        handle(output, ModItems.OAK_HANDLE.get(), Items.OAK_PLANKS, "oak");
        handle(output, ModItems.SPRUCE_HANDLE.get(), Items.SPRUCE_PLANKS, "spruce");
        handle(output, ModItems.BIRCH_HANDLE.get(), Items.BIRCH_PLANKS, "birch");
        handle(output, ModItems.DARK_OAK_HANDLE.get(), Items.DARK_OAK_PLANKS, "dark_oak");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.BAMBOO_HANDLE.get(), 2)
            .pattern("B").pattern("B")
            .define('B', Items.BAMBOO)
            .unlockedBy("has_bamboo", has(Items.BAMBOO)).save(output, "bamboo_handle");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.REINFORCED_HANDLE.get())
            .requires(ModItems.OAK_HANDLE.get()).requires(Items.IRON_INGOT)
            .unlockedBy("has_handle", has(ModItems.OAK_HANDLE.get())).save(output, "reinforced_handle");
        wrap(output, ModItems.OAK_HANDLE.get(), "oak");
        wrap(output, ModItems.SPRUCE_HANDLE.get(), "spruce");
        wrap(output, ModItems.BIRCH_HANDLE.get(), "birch");
        wrap(output, ModItems.DARK_OAK_HANDLE.get(), "dark_oak");
        wrap(output, ModItems.BAMBOO_HANDLE.get(), "bamboo");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.BONE_HANDLE.get(), 2)
            .pattern("B").pattern("B")
            .define('B', Items.BONE)
            .unlockedBy("has_bone", has(Items.BONE)).save(output, "bone_handle");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.LONG_SHAFT.get())
            .pattern("S").pattern("S")
            .define('S', Items.STICK)
            .unlockedBy("has_stick", has(Items.STICK)).save(output, "long_shaft");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.BINDING.get(), 2)
            .requires(Items.LEATHER).requires(Items.STRING)
            .unlockedBy("has_leather", has(Items.LEATHER)).save(output, "binding");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.IRON_GUARD.get())
            .pattern(" I ").pattern("III").pattern(" I ")
            .define('I', Items.IRON_INGOT)
            .unlockedBy("has_iron", has(Items.IRON_INGOT)).save(output, "iron_guard");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.STEEL_GUARD.get())
            .pattern(" B ").pattern("BBB").pattern(" B ")
            .define('B', ModItems.STEEL_BILLET.get())
            .unlockedBy("has_steel", has(ModItems.STEEL_BILLET.get())).save(output, "steel_guard");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.IRON_POMMEL.get())
            .pattern(" I ").pattern("I I")
            .define('I', Items.IRON_INGOT)
            .unlockedBy("has_iron", has(Items.IRON_INGOT)).save(output, "iron_pommel");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.STEEL_POMMEL.get())
            .pattern(" B ").pattern("B B")
            .define('B', ModItems.STEEL_BILLET.get())
            .unlockedBy("has_steel", has(ModItems.STEEL_BILLET.get())).save(output, "steel_pommel");

        // ---------- journal ----------
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.JOURNAL.get())
            .requires(Items.BOOK).requires(Items.LEATHER)
            .unlockedBy("has_book", has(Items.BOOK)).save(output, "blacksmith_journal");
    }

    private static void handle(RecipeOutput output, net.minecraft.world.level.ItemLike result,
            net.minecraft.world.level.ItemLike planks, String name) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, result, 2)
            .pattern("P").pattern("P")
            .define('P', planks)
            .unlockedBy("has_planks", has(planks)).save(output, name + "_handle");
    }

    private static void wrap(RecipeOutput output, net.minecraft.world.level.ItemLike handle, String name) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.LEATHER_WRAPPED_HANDLE.get())
            .requires(handle).requires(Items.LEATHER).requires(Items.STRING)
            .unlockedBy("has_handle", has(handle)).save(output, "wrapped_" + name + "_handle");
    }
}

package com.truemetallurgy.registry;

import com.truemetallurgy.TrueMetallurgy;
import com.truemetallurgy.recipe.AlloyRecipe;
import com.truemetallurgy.recipe.AssemblyRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Custom recipe types: forge alloying and assembly-table crafting. */
public final class ModRecipeTypes {
    private ModRecipeTypes() {}

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
        DeferredRegister.create(Registries.RECIPE_TYPE, TrueMetallurgy.MOD_ID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
        DeferredRegister.create(Registries.RECIPE_SERIALIZER, TrueMetallurgy.MOD_ID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<AlloyRecipe>> ALLOY =
        RECIPE_TYPES.register("alloy", () -> new RecipeType<AlloyRecipe>() {
            @Override public String toString() { return TrueMetallurgy.MOD_ID + ":alloy"; }
        });
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<AlloyRecipe>> ALLOY_SERIALIZER =
        RECIPE_SERIALIZERS.register("alloy", AlloyRecipe.Serializer::new);

    public static final DeferredHolder<RecipeType<?>, RecipeType<AssemblyRecipe>> ASSEMBLY =
        RECIPE_TYPES.register("assembly", () -> new RecipeType<AssemblyRecipe>() {
            @Override public String toString() { return TrueMetallurgy.MOD_ID + ":assembly"; }
        });
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<AssemblyRecipe>> ASSEMBLY_SERIALIZER =
        RECIPE_SERIALIZERS.register("assembly", AssemblyRecipe.Serializer::new);
}

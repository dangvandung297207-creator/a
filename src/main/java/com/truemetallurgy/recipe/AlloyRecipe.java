package com.truemetallurgy.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.truemetallurgy.registry.ModRecipeTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

/**
 * Forge alloying: primary metal + carbon at high temperature become an
 * alloyed bloom. Order-insensitive, temperature-gated.
 */
public record AlloyRecipe(Ingredient primary, Ingredient carbon, int minTemp, int time, ItemStack result) implements Recipe<AlloyRecipe.Input> {
    public record Input(ItemStack a, ItemStack b) implements RecipeInput {
        @Override public ItemStack getItem(int index) { return index == 0 ? a : b; }
        @Override public int size() { return 2; }
    }

    @Override
    public boolean matches(Input input, Level level) {
        return (primary.test(input.a()) && carbon.test(input.b()))
            || (primary.test(input.b()) && carbon.test(input.a()));
    }

    @Override
    public ItemStack assemble(Input input, HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return result;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.ALLOY_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.ALLOY.get();
    }

    public int minTemp() { return minTemp; }
    public int time() { return time; }

    public static class Serializer implements RecipeSerializer<AlloyRecipe> {
        public static final MapCodec<AlloyRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC.fieldOf("primary").forGetter(AlloyRecipe::primary),
            Ingredient.CODEC.fieldOf("carbon").forGetter(AlloyRecipe::carbon),
            Codec.INT.fieldOf("min_temp").forGetter(AlloyRecipe::minTemp),
            Codec.INT.fieldOf("time").forGetter(AlloyRecipe::time),
            ItemStack.CODEC.fieldOf("result").forGetter(AlloyRecipe::result)
        ).apply(instance, AlloyRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, AlloyRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, AlloyRecipe::primary,
            Ingredient.CONTENTS_STREAM_CODEC, AlloyRecipe::carbon,
            ByteBufCodecs.VAR_INT, AlloyRecipe::minTemp,
            ByteBufCodecs.VAR_INT, AlloyRecipe::time,
            ItemStack.STREAM_CODEC, AlloyRecipe::result,
            AlloyRecipe::new);

        @Override
        public MapCodec<AlloyRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, AlloyRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}

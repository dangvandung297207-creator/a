package com.truemetallurgy.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.truemetallurgy.assembly.AssemblyLogic;
import com.truemetallurgy.registry.ModItems;
import com.truemetallurgy.registry.ModRecipeTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;


/**
 * Assembly-table recipe. Ingredients are data-driven; the finished item is
 * computed from the blade's recorded history, so JSON stays simple while
 * every output stays unique.
 */
public record AssemblyRecipe(String category, Ingredient blade, Ingredient slotB, Ingredient slotC,
        Ingredient slotD, boolean needsBlueprint) implements Recipe<AssemblyRecipe.Input> {

    public record Input(ItemStack blade, ItemStack b, ItemStack c, ItemStack d, ItemStack blueprint) implements RecipeInput {
        @Override public ItemStack getItem(int index) {
            return switch (index) {
                case 0 -> blade;
                case 1 -> b;
                case 2 -> c;
                case 3 -> d;
                default -> blueprint;
            };
        }
        @Override public int size() { return 5; }
    }

    @Override
    public boolean matches(Input input, Level level) {
        if (!blade.test(input.blade())) return false;
        if (!slotB.isEmpty() && !slotB.test(input.b())) return false;
        if (!slotC.isEmpty() && !slotC.test(input.c())) return false;
        if (!slotD.isEmpty() && !slotD.test(input.d())) return false;
        return true;
    }

    @Override
    public ItemStack assemble(Input input, HolderLookup.Provider registries) {
        return getResultItem(registries);
    }

    /** Server-side assembly with full identity computation. */
    public AssemblyLogic.Result assembleLogic(Input input, ServerPlayer player) {
        return AssemblyLogic.assemble(category, input.blade(), input.b(), input.c(), input.d(), input.blueprint(), player);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return new ItemStack(switch (category) {
            case "axe" -> ModItems.IRON_AXE.get();
            case "pickaxe" -> ModItems.IRON_PICKAXE.get();
            case "spear" -> ModItems.IRON_SPEAR.get();
            default -> ModItems.IRON_SWORD.get();
        });
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.ASSEMBLY_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.ASSEMBLY.get();
    }

    public int categoryIndex() {
        return switch (category) {
            case "axe" -> 1;
            case "pickaxe" -> 2;
            case "spear" -> 3;
            default -> 0;
        };
    }

    public static class Serializer implements RecipeSerializer<AssemblyRecipe> {
        private static Ingredient empty() {
            return Ingredient.EMPTY;
        }

        public static final MapCodec<AssemblyRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("category").forGetter(AssemblyRecipe::category),
            Ingredient.CODEC.fieldOf("blade").forGetter(AssemblyRecipe::blade),
            Ingredient.CODEC.optionalFieldOf("slot_b", empty()).forGetter(AssemblyRecipe::slotB),
            Ingredient.CODEC.optionalFieldOf("slot_c", empty()).forGetter(AssemblyRecipe::slotC),
            Ingredient.CODEC.optionalFieldOf("slot_d", empty()).forGetter(AssemblyRecipe::slotD),
            Codec.BOOL.optionalFieldOf("needs_blueprint", false).forGetter(AssemblyRecipe::needsBlueprint)
        ).apply(instance, AssemblyRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, AssemblyRecipe> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, AssemblyRecipe::category,
            Ingredient.CONTENTS_STREAM_CODEC, AssemblyRecipe::blade,
            Ingredient.CONTENTS_STREAM_CODEC, AssemblyRecipe::slotB,
            Ingredient.CONTENTS_STREAM_CODEC, AssemblyRecipe::slotC,
            Ingredient.CONTENTS_STREAM_CODEC, AssemblyRecipe::slotD,
            ByteBufCodecs.BOOL, AssemblyRecipe::needsBlueprint,
            AssemblyRecipe::new);

        @Override
        public MapCodec<AssemblyRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, AssemblyRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}

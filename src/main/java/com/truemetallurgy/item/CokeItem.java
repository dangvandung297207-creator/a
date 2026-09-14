package com.truemetallurgy.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.Nullable;

/** Purified forge fuel. Burns hot in furnaces and forge alike. */
public class CokeItem extends Item {
    public CokeItem() {
        super(new Properties());
    }

    @Override
    public int getBurnTime(ItemStack stack, @Nullable RecipeType<?> recipeType) {
        return 3200;
    }
}


package com.fabrica.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import java.util.List;

public record ProcessingRecipeInput(List<ItemStack> items) implements RecipeInput {
    @Override
    public ItemStack getItem(int index) {
        return items.get(index);
    }

    @Override
    public int size() {
        return items.size();
    }
}
package com.fabrica.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

import java.util.List;

// Вход рецепта машины: фиксированный список стаков по слотам машины.
// Размер входа должен совпадать с количеством слотов, участвующих в рецепте.
public record ProcessingInput(List<ItemStack> stacks) implements RecipeInput {

    @Override
    public ItemStack getItem(int index) {
        return stacks.get(index);
    }

    @Override
    public int size() {
        return stacks.size();
    }

    @Override
    public boolean isEmpty() {
        return stacks.stream().allMatch(ItemStack::isEmpty);
    }
}

package com.fabrica.recipe;

import com.fabrica.registry.ModRecipes;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.level.Level;

import java.util.List;

public class ProcessingRecipe implements Recipe<ProcessingRecipeInput> {
    private final MachineType machineType;
    private final NonNullList<Ingredient> ingredients;
    private final List<ProcessingOutput> outputs;
    private final int processTime;
    private final long energyCost;

    public ProcessingRecipe(MachineType machineType, NonNullList<Ingredient> ingredients, List<ProcessingOutput> outputs, int processTime, long energyCost) {
        this.machineType = machineType;
        this.ingredients = ingredients;
        this.outputs = outputs;
        this.processTime = processTime;
        this.energyCost = energyCost;
    }

    @Override
    public boolean matches(ProcessingRecipeInput input, Level level) {
        if (input.size() < ingredients.size()) return false;
        for (int i = 0; i < ingredients.size(); i++) {
            if (!ingredients.get(i).test(input.getItem(i))) return false;
        }
        return true;
    }

    @Override
    public ItemStack assemble(ProcessingRecipeInput input) {
        return outputs.isEmpty() ? ItemStack.EMPTY : outputs.get(0).stack().copy();
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public RecipeSerializer<? extends Recipe<ProcessingRecipeInput>> getSerializer() {
        return ModRecipes.PROCESSING_SERIALIZER;
    }

    @Override
    public RecipeType<? extends Recipe<ProcessingRecipeInput>> getType() {
        return ModRecipes.PROCESSING_TYPE;
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of();
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return ModRecipes.FABRICA_CATEGORY;
    }

    public MachineType getMachineType() { return machineType; }
    public NonNullList<Ingredient> getIngredients() { return ingredients; }
    public List<ProcessingOutput> getOutputs() { return outputs; }
    public int getProcessTime() { return processTime; }
    public long getEnergyCost() { return energyCost; }
}

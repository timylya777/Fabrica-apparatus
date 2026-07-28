package com.fabrica.recipe;

import com.fabrica.FabricaMod;
import com.fabrica.registry.ModRecipes;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.level.Level;

import java.util.List;

public class FurnaceRecipe implements Recipe<SingleRecipeInput> {

    private final Ingredient ingredient;
    private final ItemStack result;
    private final int cookTime;
    private final int energyCost;

    public FurnaceRecipe(Ingredient ingredient, ItemStack result, int cookTime, int energyCost) {
        this.ingredient = ingredient;
        this.result = result;
        this.cookTime = cookTime;
        this.energyCost = energyCost;
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return ingredient.test(input.item());
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input) {
        return result.copy();
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
    public RecipeSerializer<? extends Recipe<SingleRecipeInput>> getSerializer() {
        return ModRecipes.FURNACE_SERIALIZER;
    }

    @Override
    public RecipeType<? extends Recipe<SingleRecipeInput>> getType() {
        return ModRecipes.FURNACE_TYPE;
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

    public Ingredient ingredient() {
        return ingredient;
    }

    public ItemStack result() {
        return result;
    }

    public int cookTime() {
        return cookTime;
    }

    public int energyCost() {
        return energyCost;
    }
}

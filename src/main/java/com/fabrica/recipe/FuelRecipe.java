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

public class FuelRecipe implements Recipe<SingleRecipeInput> {

    private final Ingredient ingredient;
    private final int burnTime;
    private final int energyPerTick;

    public FuelRecipe(Ingredient ingredient, int burnTime, int energyPerTick) {
        this.ingredient = ingredient;
        this.burnTime = burnTime;
        this.energyPerTick = energyPerTick;
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return ingredient.test(input.item());
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input) {
        return ItemStack.EMPTY;
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
        return ModRecipes.FUEL_SERIALIZER;
    }

    @Override
    public RecipeType<? extends Recipe<SingleRecipeInput>> getType() {
        return ModRecipes.FUEL_TYPE;
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

    public int burnTime() {
        return burnTime;
    }

    public int energyPerTick() {
        return energyPerTick;
    }
}

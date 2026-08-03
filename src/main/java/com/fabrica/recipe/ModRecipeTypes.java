package com.fabrica.recipe;

import com.fabrica.FabricaMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeType;

// Реестр типов рецептов машин: мацерация и сплавка.
public final class ModRecipeTypes {

    public static final RecipeType<MaceratingRecipe> MACERATING = register("macerating");
    public static final RecipeType<AlloyingRecipe> ALLOYING = register("alloying");
    public static final RecipeType<AnvilRecipe> ANVIL = register("anvil");

    // Категория для книги рецептов (машины не используют книгу, но тип обязателен).
    public static final RecipeBookCategory MACHINE_CATEGORY = Registry.register(
            BuiltInRegistries.RECIPE_BOOK_CATEGORY, FabricaMod.id("machine"), new RecipeBookCategory());

    @SuppressWarnings("unchecked")
    private static <T extends Recipe<?>> RecipeType<T> register(String id) {
        return (RecipeType<T>) Registry.register(
                BuiltInRegistries.RECIPE_TYPE, FabricaMod.id(id), new RecipeType<T>() {
                });
    }

    // Регистрация выполнена статическими полями; вызов нужен как точка старта.
    public static void register() {
    }

    private ModRecipeTypes() {
    }
}

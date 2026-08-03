package com.fabrica.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.List;

// Рецепт наковальни: кузнечная наковальня работает НЕ на энергии, а на
// «человечках» (фигурках) — рабочий в слоте фигурки тратит свою прочность
// при каждой операции. Скорость операции зависит от типа фигурки (см.
// FigureItem.getSpeedMultiplier()). Формат JSON:
//   {"type": "fabrica_apparatus:anvil",
//    "ingredients": [{"id": "fabrica_apparatus:copper_ingot"}],
//    "result": {"id": "fabrica_apparatus:copper_plate", "count": 1},
//    "time": 200, "damage": 1}
public class AnvilRecipe implements Recipe<ProcessingInput> {

    protected final List<Ingredient> ingredients;
    protected final ItemStackTemplate result;
    protected final int time;
    protected final int damage;

    public AnvilRecipe(List<Ingredient> ingredients, ItemStackTemplate result, int time, int damage) {
        this.ingredients = ingredients;
        this.result = result;
        this.time = time;
        this.damage = damage;
    }

    public static final MapCodec<AnvilRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC.listOf().fieldOf("ingredients").forGetter(recipe -> recipe.ingredients),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(recipe -> recipe.result),
            Codec.INT.fieldOf("time").forGetter(recipe -> recipe.time),
            Codec.INT.optionalFieldOf("damage", 1).forGetter(recipe -> recipe.damage)
    ).apply(instance, AnvilRecipe::new));

    @Override
    public boolean matches(ProcessingInput input, Level level) {
        if (input.size() < ingredients.size()) return false;
        for (int i = 0; i < ingredients.size(); i++) {
            if (!ingredients.get(i).test(input.getItem(i))) return false;
        }
        return true;
    }

    @Override
    public ItemStack assemble(ProcessingInput input) {
        return result.create();
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
    public PlacementInfo placementInfo() {
        return PlacementInfo.create(ingredients);
    }

    @Override
    public RecipeType<? extends Recipe<ProcessingInput>> getType() {
        return ModRecipeTypes.ANVIL;
    }

    @Override
    public RecipeSerializer<? extends Recipe<ProcessingInput>> getSerializer() {
        return ModRecipeSerializers.ANVIL;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return ModRecipeTypes.MACHINE_CATEGORY;
    }

    public List<Ingredient> getIngredientsList() {
        return ingredients;
    }

    public ItemStackTemplate getResultTemplate() {
        return result;
    }

    public int getTime() {
        return time;
    }

    public int getDamage() {
        return damage;
    }
}
package com.fabrica.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;

import java.util.List;

// База рецепта машины: ингредиенты по слотам, результат (шаблон, конвертируется
// в стек в момент сборки), время в тиках и расход энергии (AP/тик). Конкретные
// типы (мацерация, сплавка) наследуют поведение, различаясь лишь типом рецепта
// и сериализатором.
public abstract class AbstractMachineRecipe implements Recipe<ProcessingInput> {

    protected final List<Ingredient> ingredients;
    protected final ItemStackTemplate result;
    protected final int time;
    protected final long eu;

    protected AbstractMachineRecipe(List<Ingredient> ingredients, ItemStackTemplate result, int time, long eu) {
        this.ingredients = ingredients;
        this.result = result;
        this.time = time;
        this.eu = eu;
    }

    // Совпадение: каждый ингредиент тестируется по своему слоту входа.
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

    public List<Ingredient> getIngredientsList() {
        return ingredients;
    }

    public ItemStackTemplate getResultTemplate() {
        return result;
    }

    public int getTime() {
        return time;
    }

    public long getEu() {
        return eu;
    }
}

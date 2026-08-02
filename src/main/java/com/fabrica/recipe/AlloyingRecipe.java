package com.fabrica.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.List;

// Рецепт сплавки (в электропечи): два ингредиента -> сплав.
public class AlloyingRecipe extends AbstractMachineRecipe {

    public AlloyingRecipe(List<Ingredient> ingredients, ItemStackTemplate result, int time, long eu) {
        super(ingredients, result, time, eu);
    }

    // Формат JSON: {"type": "fabrica_apparatus:alloying",
    //  "ingredients": [a, b], "result": {"id": "...", "count": n}, "time": n, "eu": n}
    public static final MapCodec<AlloyingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC.listOf().fieldOf("ingredients").forGetter(AbstractMachineRecipe::getIngredientsList),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(AbstractMachineRecipe::getResultTemplate),
            Codec.INT.fieldOf("time").forGetter(AbstractMachineRecipe::getTime),
            Codec.LONG.optionalFieldOf("eu", 8L).forGetter(AbstractMachineRecipe::getEu)
    ).apply(instance, AlloyingRecipe::new));

    @Override
    public RecipeType<? extends Recipe<ProcessingInput>> getType() {
        return ModRecipeTypes.ALLOYING;
    }

    @Override
    public RecipeSerializer<? extends Recipe<ProcessingInput>> getSerializer() {
        return ModRecipeSerializers.ALLOYING;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return ModRecipeTypes.MACHINE_CATEGORY;
    }
}

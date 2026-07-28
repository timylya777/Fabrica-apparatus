package com.fabrica.registry;

import com.fabrica.FabricaMod;
import com.fabrica.recipe.FuelRecipe;
import com.fabrica.recipe.FurnaceRecipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public class ModRecipes {

    public static final RecipeType<FurnaceRecipe> FURNACE_TYPE = RecipeType.register(FabricaMod.MOD_ID + ":furnace");
    public static final RecipeType<FuelRecipe> FUEL_TYPE = RecipeType.register(FabricaMod.MOD_ID + ":fuel");

    public static final RecipeBookCategory FABRICA_CATEGORY = new RecipeBookCategory();

    public static final RecipeSerializer<FurnaceRecipe> FURNACE_SERIALIZER = new RecipeSerializer<>(
        RecordCodecBuilder.<FurnaceRecipe>mapCodec(instance ->
            instance.group(
                Ingredient.CODEC.fieldOf("ingredient").forGetter(FurnaceRecipe::ingredient),
                ItemStack.CODEC.fieldOf("result").forGetter(FurnaceRecipe::result),
                Codec.INT.fieldOf("cookTime").forGetter(FurnaceRecipe::cookTime),
                Codec.INT.fieldOf("energyCost").forGetter(FurnaceRecipe::energyCost)
            ).apply(instance, FurnaceRecipe::new)
        ),
        StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, FurnaceRecipe::ingredient,
            ItemStack.STREAM_CODEC, FurnaceRecipe::result,
            ByteBufCodecs.INT, FurnaceRecipe::cookTime,
            ByteBufCodecs.INT, FurnaceRecipe::energyCost,
            FurnaceRecipe::new
        )
    );

    public static final RecipeSerializer<FuelRecipe> FUEL_SERIALIZER = new RecipeSerializer<>(
        RecordCodecBuilder.<FuelRecipe>mapCodec(instance ->
            instance.group(
                Ingredient.CODEC.fieldOf("ingredient").forGetter(FuelRecipe::ingredient),
                Codec.INT.fieldOf("burnTime").forGetter(FuelRecipe::burnTime),
                Codec.INT.fieldOf("energyPerTick").forGetter(FuelRecipe::energyPerTick)
            ).apply(instance, FuelRecipe::new)
        ),
        StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, FuelRecipe::ingredient,
            ByteBufCodecs.INT, FuelRecipe::burnTime,
            ByteBufCodecs.INT, FuelRecipe::energyPerTick,
            FuelRecipe::new
        )
    );

    public static void register() {
        Registry.register(BuiltInRegistries.RECIPE_TYPE, FabricaMod.id("furnace"), FURNACE_TYPE);
        Registry.register(BuiltInRegistries.RECIPE_TYPE, FabricaMod.id("fuel"), FUEL_TYPE);
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, FabricaMod.id("furnace"), FURNACE_SERIALIZER);
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, FabricaMod.id("fuel"), FUEL_SERIALIZER);
    }
}

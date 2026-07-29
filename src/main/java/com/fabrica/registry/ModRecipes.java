
package com.fabrica.registry;

import com.fabrica.FabricaMod;
import com.fabrica.recipe.FuelRecipe;
import com.fabrica.recipe.FurnaceRecipe;
import com.fabrica.recipe.ProcessingRecipe;

import com.mojang.serialization.Codec;
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
    public static final RecipeType<FurnaceRecipe> FURNACE_TYPE = registerType("furnace");
    public static final RecipeType<FuelRecipe> FUEL_TYPE = registerType("fuel");
    public static final RecipeType<ProcessingRecipe> PROCESSING_TYPE = registerType("processing");

    public static final RecipeBookCategory FABRICA_CATEGORY = new RecipeBookCategory();

    public static final RecipeSerializer<FurnaceRecipe> FURNACE_SERIALIZER = new RecipeSerializer<>(
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Ingredient.CODEC.fieldOf("ingredient").forGetter(FurnaceRecipe::ingredient),
                    ItemStack.CODEC.fieldOf("result").forGetter(FurnaceRecipe::result),
                    Codec.INT.fieldOf("cookTime").forGetter(FurnaceRecipe::cookTime),
                    Codec.INT.fieldOf("energyCost").forGetter(FurnaceRecipe::energyCost)
            ).apply(instance, FurnaceRecipe::new)),
            StreamCodec.composite(
                    Ingredient.CONTENTS_STREAM_CODEC, FurnaceRecipe::ingredient,
                    ItemStack.STREAM_CODEC, FurnaceRecipe::result,
                    ByteBufCodecs.INT, FurnaceRecipe::cookTime,
                    ByteBufCodecs.INT, FurnaceRecipe::energyCost,
                    FurnaceRecipe::new
            )
    );

    public static final RecipeSerializer<FuelRecipe> FUEL_SERIALIZER = new RecipeSerializer<>(
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Ingredient.CODEC.fieldOf("ingredient").forGetter(FuelRecipe::ingredient),
                    Codec.INT.fieldOf("burnTime").forGetter(FuelRecipe::burnTime),
                    Codec.INT.fieldOf("energyPerTick").forGetter(FuelRecipe::energyPerTick)
            ).apply(instance, FuelRecipe::new)),
            StreamCodec.composite(
                    Ingredient.CONTENTS_STREAM_CODEC, FuelRecipe::ingredient,
                    ByteBufCodecs.INT, FuelRecipe::burnTime,
                    ByteBufCodecs.INT, FuelRecipe::energyPerTick,
                    FuelRecipe::new
            )
    );

    public static final RecipeSerializer<ProcessingRecipe> PROCESSING_SERIALIZER = new RecipeSerializer<>(
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    com.fabrica.recipe.MachineType.CODEC.fieldOf("machine_type").forGetter(ProcessingRecipe::getMachineType),
                    Ingredient.CODEC.listOf().xmap(ModRecipes::listToNonNullList, list -> list).fieldOf("ingredients").forGetter(ProcessingRecipe::getIngredients),
                    com.fabrica.recipe.ProcessingOutput.CODEC.listOf().fieldOf("outputs").forGetter(ProcessingRecipe::getOutputs),
                    Codec.INT.fieldOf("process_time").forGetter(ProcessingRecipe::getProcessTime),
                    Codec.LONG.fieldOf("energy_cost").forGetter(ProcessingRecipe::getEnergyCost)
            ).apply(instance, ProcessingRecipe::new)),
            StreamCodec.composite(
                    ByteBufCodecs.fromCodec(com.fabrica.recipe.MachineType.CODEC), ProcessingRecipe::getMachineType,
                    Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()).<net.minecraft.core.NonNullList<Ingredient>>map(
                            ModRecipes::listToNonNullList,
                            list -> list
                    ), ProcessingRecipe::getIngredients,
                    com.fabrica.recipe.ProcessingOutput.STREAM_CODEC.apply(ByteBufCodecs.list()), ProcessingRecipe::getOutputs,
                    ByteBufCodecs.INT, ProcessingRecipe::getProcessTime,
                    ByteBufCodecs.VAR_LONG, ProcessingRecipe::getEnergyCost,
                    ProcessingRecipe::new
            )
    );

    private static <T extends net.minecraft.world.item.crafting.Recipe<?>> RecipeType<T> registerType(String name) {
        net.minecraft.resources.Identifier id = FabricaMod.id(name);
        RecipeType<T> type = new RecipeType<T>() {
            @Override
            public String toString() { return name; }
        };
        return Registry.register(BuiltInRegistries.RECIPE_TYPE, id, type);
    }

    private static <T> net.minecraft.core.NonNullList<T> listToNonNullList(java.util.List<T> list) {
        net.minecraft.core.NonNullList<T> n = net.minecraft.core.NonNullList.create();
        n.addAll(list);
        return n;
    }

    public static void register() {
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, FabricaMod.id("furnace"), FURNACE_SERIALIZER);
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, FabricaMod.id("fuel"), FUEL_SERIALIZER);
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, FabricaMod.id("processing"), PROCESSING_SERIALIZER);
    }
}
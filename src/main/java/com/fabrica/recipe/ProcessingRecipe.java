
package com.fabrica.recipe;

import com.fabrica.registry.ModRecipes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
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
    public ItemStack assemble(ProcessingRecipeInput input, net.minecraft.core.RegistryAccess registryAccess) {
        return outputs.isEmpty() ? ItemStack.EMPTY : outputs.get(0).stack().copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(net.minecraft.core.RegistryAccess registryAccess) {
        return outputs.isEmpty() ? ItemStack.EMPTY : outputs.get(0).stack().copy();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.PROCESSING_SERIALIZER;
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.PROCESSING_TYPE;
    }

    public MachineType getMachineType() { return machineType; }
    public NonNullList<Ingredient> getIngredients() { return ingredients; }
    public List<ProcessingOutput> getOutputs() { return outputs; }
    public int getProcessTime() { return processTime; }
    public long getEnergyCost() { return energyCost; }

    public static class Serializer implements RecipeSerializer<ProcessingRecipe> {
        public static final MapCodec<ProcessingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                MachineType.CODEC.fieldOf("machine_type").forGetter(ProcessingRecipe::getMachineType),
                Ingredient.CODEC.listOf().xmap(NonNullList::create, list -> list).fieldOf("ingredients").forGetter(ProcessingRecipe::getIngredients),
                ProcessingOutput.CODEC.listOf().fieldOf("outputs").forGetter(ProcessingRecipe::getOutputs),
                Codec.INT.fieldOf("process_time").forGetter(ProcessingRecipe::getProcessTime),
                Codec.LONG.fieldOf("energy_cost").forGetter(ProcessingRecipe::getEnergyCost)
        ).apply(instance, ProcessingRecipe::new));

        public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ProcessingRecipe> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.enumConst(MachineType.class), ProcessingRecipe::getMachineType,
                Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()).xmap(NonNullList::create, list -> list), ProcessingRecipe::getIngredients,
                ProcessingOutput.STREAM_CODEC.apply(ByteBufCodecs.list()), ProcessingRecipe::getOutputs,
                ByteBufCodecs.INT, ProcessingRecipe::getProcessTime,
                ByteBufCodecs.VAR_LONG, ProcessingRecipe::getEnergyCost,
                ProcessingRecipe::new
        );

        @Override
        public MapCodec<ProcessingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<net.minecraft.network.FriendlyByteBuf, ProcessingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
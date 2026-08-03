package com.fabrica.recipe;

import com.fabrica.FabricaMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

// Сериализаторы рецептов машин: кодек для JSON и потоковый кодек для сети.
public final class ModRecipeSerializers {

    public static final RecipeSerializer<MaceratingRecipe> MACERATING = register(
            "macerating",
            new RecipeSerializer<>(MaceratingRecipe.CODEC,
                    ByteBufCodecs.fromCodecWithRegistries(MaceratingRecipe.CODEC.codec()))
    );

    public static final RecipeSerializer<AlloyingRecipe> ALLOYING = register(
            "alloying",
            new RecipeSerializer<>(AlloyingRecipe.CODEC,
                    ByteBufCodecs.fromCodecWithRegistries(AlloyingRecipe.CODEC.codec()))
    );

    public static final RecipeSerializer<AnvilRecipe> ANVIL = register(
            "anvil",
            new RecipeSerializer<>(AnvilRecipe.CODEC,
                    ByteBufCodecs.fromCodecWithRegistries(AnvilRecipe.CODEC.codec()))
    );

    private static <T extends Recipe<?>> RecipeSerializer<T> register(String id, RecipeSerializer<T> serializer) {
        return Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, FabricaMod.id(id), serializer);
    }

    // Регистрация выполнена статическими полями; вызов нужен как точка старта.
    public static void register() {
    }

    private ModRecipeSerializers() {
    }
}

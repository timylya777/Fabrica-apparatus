package com.fabrica.item;

import java.util.function.Function;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

public class ModItems {

    // ЗАМЕНИТЕ "fabrica" на реальный ID вашего мода из файла fabric.mod.json
    private static final String MOD_ID = "fabrica_apparatus";

    /**
     * Универсальный метод для регистрации предметов.
     */
    public static <T extends Item> T register(
            ResourceKey<Item> key,
            Function<Item.Properties, T> factory,
            Item.Properties properties
    ) {
        T item = factory.apply(properties.setId(key));
        Registry.register(BuiltInRegistries.ITEM, key, item);
        return item;
    }

    // 1. Создаём ключи регистрации (ResourceKey)
    public static final ResourceKey<Item> IRON_FIGURE_KEY = ResourceKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath(MOD_ID, "iron_figure")
    );
    public static final ResourceKey<Item> COPPER_FIGURE_KEY = ResourceKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath(MOD_ID, "copper_figure")
    );
    public static final ResourceKey<Item> CLAY_FIGURE_KEY = ResourceKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath(MOD_ID, "clay_figure")
    );
    public static final ResourceKey<Item> BRICK_FIGURE_KEY = ResourceKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath(MOD_ID, "brick_figure")
    );
    public static final ResourceKey<Item> TERRACOTTA_FIGURE_KEY = ResourceKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath(MOD_ID, "terracotta_figure")
    );

    // 2. Регистрируем предметы
    public static final Item IRON_FIGURE = register(
            IRON_FIGURE_KEY,
            Item::new,
            new Item.Properties()
    );
    public static final Item COPPER_FIGURE = register(
            COPPER_FIGURE_KEY,
            Item::new,
            new Item.Properties()
    );
    public static final Item CLAY_FIGURE = register(
            CLAY_FIGURE_KEY,
            Item::new,
            new Item.Properties()
    );
    public static final Item BRICK_FIGURE = register(
            BRICK_FIGURE_KEY,
            Item::new,
            new Item.Properties()
    );
    public static final Item TERRACOTTA_FIGURE = register(
            TERRACOTTA_FIGURE_KEY,
            Item::new,
            new Item.Properties()
    );

    /**
     * Метод инициализации. 
     * Вызовите ModItems.register() в вашем главном классе (ModInitializer).
     */
    public static void register() {
        // Добавляем предмет во вкладку "Ингредиенты" (Ingredients)
        var ingredientsKey = ResourceKey.create(
                Registries.CREATIVE_MODE_TAB,
                Identifier.withDefaultNamespace("ingredients")
        );
        CreativeModeTabEvents.modifyOutputEvent(ingredientsKey)
                .register(output -> {
                    output.accept(IRON_FIGURE);
                    output.accept(COPPER_FIGURE);
                    output.accept(CLAY_FIGURE);
                    output.accept(BRICK_FIGURE);
                    output.accept(TERRACOTTA_FIGURE);
                });
        
    }
}
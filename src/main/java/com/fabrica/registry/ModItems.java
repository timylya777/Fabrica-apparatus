package com.fabrica.registry;

import java.util.function.Function;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

public class ModItems {

    private static final String MOD_ID = "fabrica_apparatus";

    public static <T extends Item> T register(
            ResourceKey<Item> key,
            Function<Item.Properties, T> factory,
            Item.Properties properties
    ) {
        T item = factory.apply(properties.setId(key));
        Registry.register(BuiltInRegistries.ITEM, key, item);
        return item;
    }

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

    public static void register() {
    }
}

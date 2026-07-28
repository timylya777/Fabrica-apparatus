package com.fabrica.item;

import java.util.function.Function;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

public class ModItems {

    public static <T extends Item> T register(
            net.minecraft.resources.ResourceKey<Item> key,
            Function<Item.Properties, T> factory,
            Item.Properties properties
    ) {

        T item = factory.apply(properties.setId(key));

        Registry.register(BuiltInRegistries.ITEM, key, item);

        return item;
    }

    public static final Item IRON_FIGURE =
            register(ModItemIds.IRON_FIGURE, Item::new, new Item.Properties());

    public static void register() {

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS)
                .register(entries -> entries.accept(IRON_FIGURE));

    }
}
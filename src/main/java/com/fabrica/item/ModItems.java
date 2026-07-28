package com.fabrica.item;

import com.fabrica.FabricaMod;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

public class ModItems {

    public static final Item IRON_FIGURE = Registry.register(
        BuiltInRegistries.ITEM,
        FabricaMod.id("iron_figure"),
        new Item(new Item.Properties())
    );

    public static void register() {
    }
}
package com.fabrica.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

public class ModBlockItems {

    public static final Item MACHINE_CASING = Registry.register(
            BuiltInRegistries.ITEM,
            ModBlockIds.MACHINE_CASING_ITEM,
            new BlockItem(
                    ModBlocks.MACHINE_CASING,
                    new Item.Properties().setId(ModBlockIds.MACHINE_CASING_ITEM)
            )
    );

    public static final Item COAL_GENERATOR = Registry.register(
            BuiltInRegistries.ITEM,
            ModBlockIds.COAL_GENERATOR_ITEM,
            new BlockItem(
                    ModBlocks.COAL_GENERATOR,
                    new Item.Properties().setId(ModBlockIds.COAL_GENERATOR_ITEM)
            )
    );

    public static final Item ELECTRIC_FURNACE = Registry.register(
            BuiltInRegistries.ITEM,
            ModBlockIds.ELECTRIC_FURNACE_ITEM,
            new BlockItem(
                    ModBlocks.ELECTRIC_FURNACE,
                    new Item.Properties().setId(ModBlockIds.ELECTRIC_FURNACE_ITEM)
            )
    );

    public static void register() {
    }
}

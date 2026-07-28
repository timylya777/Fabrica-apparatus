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

    public static void register() {
    }
}

package com.fabrica.block;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
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
        var ingredientsKey = ResourceKey.create(
                Registries.CREATIVE_MODE_TAB,
                Identifier.withDefaultNamespace("ingredients")
        );
        CreativeModeTabEvents.modifyOutputEvent(ingredientsKey)
                .register(output -> output.accept(MACHINE_CASING));
    }
}
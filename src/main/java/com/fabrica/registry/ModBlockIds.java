package com.fabrica.registry;

import com.fabrica.FabricaMod;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ModBlockIds {

    public static final ResourceKey<Block> MACHINE_CASING =
            ResourceKey.create(
                    Registries.BLOCK,
                    FabricaMod.id("machine_casing")
            );

    public static final ResourceKey<Item> MACHINE_CASING_ITEM =
            ResourceKey.create(
                    Registries.ITEM,
                    FabricaMod.id("machine_casing")
            );

    public static final ResourceKey<Block> COAL_GENERATOR =
            ResourceKey.create(
                    Registries.BLOCK,
                    FabricaMod.id("coal_generator")
            );

    public static final ResourceKey<Item> COAL_GENERATOR_ITEM =
            ResourceKey.create(
                    Registries.ITEM,
                    FabricaMod.id("coal_generator")
            );

    public static final ResourceKey<Block> ELECTRIC_FURNACE =
            ResourceKey.create(
                    Registries.BLOCK,
                    FabricaMod.id("electric_furnace")
            );

    public static final ResourceKey<Item> ELECTRIC_FURNACE_ITEM =
            ResourceKey.create(
                    Registries.ITEM,
                    FabricaMod.id("electric_furnace")
            );
}

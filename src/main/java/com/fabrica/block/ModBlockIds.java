package com.fabrica.block;

import com.fabrica.FabricaMod;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ModBlockIds {

    public static final ResourceKey<Block> MACHINE_CASING =
            ResourceKey.create(
                    Registries.BLOCK,
                    Identifier.fromNamespaceAndPath(FabricaMod.MOD_ID, "machine_casing")
            );

    public static final ResourceKey<Item> MACHINE_CASING_ITEM =
            ResourceKey.create(
                    Registries.ITEM,
                    Identifier.fromNamespaceAndPath(FabricaMod.MOD_ID, "machine_casing")
            );
}
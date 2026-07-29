
package com.fabrica.registry;

import com.fabrica.FabricaMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

public class ModBlockItems {
    public static final BlockItem COAL_GENERATOR = register("coal_generator", new BlockItem(ModBlocks.COAL_GENERATOR, new Item.Properties()));
    public static final BlockItem ELECTRIC_FURNACE = register("electric_furnace", new BlockItem(ModBlocks.ELECTRIC_FURNACE, new Item.Properties()));
    public static final BlockItem ENERGY_CABLE = register("energy_cable", new BlockItem(ModBlocks.ENERGY_CABLE, new Item.Properties()));

    public static final BlockItem CRUSHER_BASIC = register("crusher_basic", new BlockItem(ModBlocks.CRUSHER_BASIC, new Item.Properties()));
    public static final BlockItem CRUSHER_ADVANCED = register("crusher_advanced", new BlockItem(ModBlocks.CRUSHER_ADVANCED, new Item.Properties()));
    public static final BlockItem CRUSHER_ELITE = register("crusher_elite", new BlockItem(ModBlocks.CRUSHER_ELITE, new Item.Properties()));
    public static final BlockItem CRUSHER_ULTIMATE = register("crusher_ultimate", new BlockItem(ModBlocks.CRUSHER_ULTIMATE, new Item.Properties()));

    private static BlockItem register(String name, BlockItem item) {
        return Registry.register(BuiltInRegistries.ITEM, FabricaMod.id(name), item);
    }

    public static void register() {}
}
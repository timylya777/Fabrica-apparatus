package com.fabrica.registry;

import com.fabrica.FabricaMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

public class ModBlockItems {
    public static final BlockItem COAL_GENERATOR = register("coal_generator", ModBlocks.COAL_GENERATOR);
    public static final BlockItem ELECTRIC_FURNACE = register("electric_furnace", ModBlocks.ELECTRIC_FURNACE);
    public static final BlockItem ENERGY_CABLE = register("energy_cable", ModBlocks.ENERGY_CABLE);

    public static final BlockItem CRUSHER_BASIC = register("crusher_basic", ModBlocks.CRUSHER_BASIC);
    public static final BlockItem CRUSHER_ADVANCED = register("crusher_advanced", ModBlocks.CRUSHER_ADVANCED);
    public static final BlockItem CRUSHER_ELITE = register("crusher_elite", ModBlocks.CRUSHER_ELITE);
    public static final BlockItem CRUSHER_ULTIMATE = register("crusher_ultimate", ModBlocks.CRUSHER_ULTIMATE);

    private static BlockItem register(String name, net.minecraft.world.level.block.Block block) {
        ResourceKey<Item> key = ResourceKey.create(BuiltInRegistries.ITEM.key(), FabricaMod.id(name));
        Item.Properties properties = new Item.Properties().setId(key);
        BlockItem item = new BlockItem(block, properties);
        return Registry.register(BuiltInRegistries.ITEM, key, item);
    }

    public static void register() {}
}

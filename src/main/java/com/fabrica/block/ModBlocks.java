package com.fabrica.block;

import com.fabrica.FabricaMod;
import com.fabrica.api.energy.EnergyTier;
import com.fabrica.block.machine.furnace.ElectricFurnaceBlock;
import com.fabrica.block.machine.generator.GeneratorBlock;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class ModBlocks {

    private static Block register(
        String id,
        BlockBehaviour.Properties properties,
        ResourceKey<CreativeModeTab> creativeTab
    ) {
        Identifier identifier = Identifier.fromNamespaceAndPath(FabricaMod.MOD_ID, id);
        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, identifier);
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, identifier);

        Block block = new FabricaBlock(properties.setId(blockKey));
        Registry.register(BuiltInRegistries.BLOCK, blockKey, block);

        Item blockItem = Registry.register(
                BuiltInRegistries.ITEM,
                itemKey,
                new BlockItem(block, new Item.Properties().setId(itemKey))
        );

        CreativeModeTabEvents.modifyOutputEvent(creativeTab)
                .register(output -> output.accept(blockItem));

        return block;
    }

    private static GeneratorBlock registerGenerator(
        String id,
        BlockBehaviour.Properties properties,
        ResourceKey<CreativeModeTab> creativeTab,
        long capacity,
        EnergyTier tier,
        long productionRate
    ) {
        Identifier identifier = Identifier.fromNamespaceAndPath(FabricaMod.MOD_ID, id);
        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, identifier);
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, identifier);

        GeneratorBlock block = new GeneratorBlock(properties.setId(blockKey), capacity, tier, productionRate);
        Registry.register(BuiltInRegistries.BLOCK, blockKey, block);

        Item blockItem = Registry.register(
                BuiltInRegistries.ITEM,
                itemKey,
                new BlockItem(block, new Item.Properties().setId(itemKey))
        );

        CreativeModeTabEvents.modifyOutputEvent(creativeTab)
                .register(output -> output.accept(blockItem));

        return block;
    }

    private static ElectricFurnaceBlock registerElectricFurnace(
        String id,
        BlockBehaviour.Properties properties,
        ResourceKey<CreativeModeTab> creativeTab,
        long capacity,
        EnergyTier tier,
        long consumptionRate
    ) {
        Identifier identifier = Identifier.fromNamespaceAndPath(FabricaMod.MOD_ID, id);
        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, identifier);
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, identifier);

        ElectricFurnaceBlock block = new ElectricFurnaceBlock(properties.setId(blockKey), capacity, tier, consumptionRate);
        Registry.register(BuiltInRegistries.BLOCK, blockKey, block);

        Item blockItem = Registry.register(
                BuiltInRegistries.ITEM,
                itemKey,
                new BlockItem(block, new Item.Properties().setId(itemKey))
        );

        CreativeModeTabEvents.modifyOutputEvent(creativeTab)
                .register(output -> output.accept(blockItem));

        return block;
    }

    public static final Block MACHINE_CASING = register(
        "machine_casing",
        BlockBehaviour.Properties.of()
                .strength(5.0F)
                .sound(SoundType.METAL),
        ResourceKey.create(
                Registries.CREATIVE_MODE_TAB,
                Identifier.withDefaultNamespace("ingredients")
        )
    );

    public static final GeneratorBlock GENERATOR = registerGenerator(
        "generator",
        BlockBehaviour.Properties.of()
                .strength(3.0F)
                .sound(SoundType.METAL),
        ResourceKey.create(
                Registries.CREATIVE_MODE_TAB,
                Identifier.withDefaultNamespace("ingredients")
        ),
        4000,
        EnergyTier.LV,
        100
    );

    public static final ElectricFurnaceBlock ELECTRIC_FURNACE = registerElectricFurnace(
        "electric_furnace",
        BlockBehaviour.Properties.of()
                .strength(3.0F)
                .sound(SoundType.METAL),
        ResourceKey.create(
                Registries.CREATIVE_MODE_TAB,
                Identifier.withDefaultNamespace("ingredients")
        ),
        2000,
        EnergyTier.LV,
        20
    );

    public static void register() {
    }
}

package com.fabrica.block.custom;

import com.fabrica.FabricaMod;
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

public class CableBlocks extends Block {

    // Конструктор — передаём настройки в родительский класс Block
    public CableBlocks(BlockBehaviour.Properties settings) {
        super(settings);
    }

    // Регистрация кабелей (аналогично ModBlocks)
    private static CableBlocks register(
        String id,
        BlockBehaviour.Properties properties,
        ResourceKey<CreativeModeTab> creativeTab
    ) {
        Identifier identifier = Identifier.fromNamespaceAndPath(FabricaMod.MOD_ID, id);
        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, identifier);
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, identifier);

        CableBlocks block = new CableBlocks(properties.setId(blockKey));
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

    // Кабели
    public static final CableBlocks CABLE = register(
        "cable",
        BlockBehaviour.Properties.of()
                .strength(1.0F)
                .sound(SoundType.METAL)
                .noOcclusion(),
        ResourceKey.create(
                Registries.CREATIVE_MODE_TAB,
                Identifier.withDefaultNamespace("ingredients")
        )
    );

    public static void register() {
    }
}
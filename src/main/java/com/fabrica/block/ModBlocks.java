package com.fabrica.block;

import com.fabrica.FabricaMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class ModBlocks {

    public static final Block MACHINE_CASING = register(
            "machine_casing",
            new Block(
                    BlockBehaviour.Properties.of()
                            .strength(5.0F)
                            .sound(SoundType.METAL)
            )
    );

    // BLOCK REGISTERGING IN ONE METHOD
    private static Block register(String id, Block block) {
        Identifier identifier = Identifier.fromNamespaceAndPath(FabricaMod.MOD_ID, id);

        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, identifier);
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, identifier);

        Registry.register(
                BuiltInRegistries.BLOCK,
                blockKey,
                block
        );

        Registry.register(
                BuiltInRegistries.ITEM,
                itemKey,
                new BlockItem(
                        block,
                        new Item.Properties().setId(itemKey)
                )
        );

        return block;
    }

    public static void register() {
    }
}
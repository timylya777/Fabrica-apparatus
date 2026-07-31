package com.fabrica.cable;

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
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.Set;

public final class FabricaCables {

    private static final ResourceKey<CreativeModeTab> TAB = ResourceKey.create(
        Registries.CREATIVE_MODE_TAB,
        Identifier.withDefaultNamespace("ingredients")
    );

    public static final CableBlock CABLE_BLOCK = registerCableBlock();

    public static final BlockEntityType<CableBlockEntity> CABLE_BE = Registry.register(
        BuiltInRegistries.BLOCK_ENTITY_TYPE,
        FabricaMod.id("cable"),
        new BlockEntityType<>(CableBlockEntity::new, Set.of(CABLE_BLOCK))
    );

    public static final Item CABLE_ITEM = registerCableItem();

    private static CableBlock registerCableBlock() {
        Identifier id = FabricaMod.id("cable");
        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, id);

        CableBlock block = new CableBlock(
            BlockBehaviour.Properties.of()
                .strength(1.0F)
                .sound(SoundType.METAL)
                .noOcclusion()
                .setId(blockKey)
        );
        Registry.register(BuiltInRegistries.BLOCK, blockKey, block);

        return block;
    }

    private static Item registerCableItem() {
        Identifier id = FabricaMod.id("cable");
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id);
        Item item = new BlockItem(CABLE_BLOCK, new Item.Properties().setId(itemKey));
        Registry.register(BuiltInRegistries.ITEM, itemKey, item);
        CreativeModeTabEvents.modifyOutputEvent(TAB)
            .register(output -> output.accept(item));
        return item;
    }

    public static void register() {
    }

    private FabricaCables() {
    }
}

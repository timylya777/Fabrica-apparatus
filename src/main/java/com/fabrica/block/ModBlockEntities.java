package com.fabrica.block;

import com.fabrica.FabricaMod;
import com.fabrica.block.machine.consumer.ConsumerBlockEntity;
import com.fabrica.block.machine.generator.GeneratorBlockEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.Set;

public final class ModBlockEntities {

    public static final BlockEntityType<GeneratorBlockEntity> GENERATOR = Registry.register(
        BuiltInRegistries.BLOCK_ENTITY_TYPE,
        FabricaMod.id("generator"),
        new BlockEntityType<>(GeneratorBlockEntity::new, Set.of(ModBlocks.GENERATOR))
    );

    public static final BlockEntityType<ConsumerBlockEntity> CONSUMER = Registry.register(
        BuiltInRegistries.BLOCK_ENTITY_TYPE,
        FabricaMod.id("consumer"),
        new BlockEntityType<>(ConsumerBlockEntity::new, Set.of(ModBlocks.CONSUMER))
    );

    public static void register() {
    }

    private ModBlockEntities() {
    }
}

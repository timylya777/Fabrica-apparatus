package com.fabrica.block;

import com.fabrica.FabricaMod;
import com.fabrica.block.machine.furnace.ElectricFurnaceBlockEntity;
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

    public static final BlockEntityType<ElectricFurnaceBlockEntity> ELECTRIC_FURNACE = Registry.register(
        BuiltInRegistries.BLOCK_ENTITY_TYPE,
        FabricaMod.id("electric_furnace"),
        new BlockEntityType<>(ElectricFurnaceBlockEntity::new, Set.of(ModBlocks.ELECTRIC_FURNACE))
    );

    public static void register() {
    }

    private ModBlockEntities() {
    }
}

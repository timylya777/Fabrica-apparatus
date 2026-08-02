package com.fabrica.block;

import com.fabrica.FabricaMod;
import com.fabrica.block.machine.furnace.ElectricFurnaceBlockEntity;
import com.fabrica.block.machine.generator.GeneratorBlockEntity;
import com.fabrica.block.me.MeDriveBlockEntity;
import com.fabrica.block.me.MeGridBlockEntity;
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

    public static final BlockEntityType<MeDriveBlockEntity> ME_DRIVE = Registry.register(
        BuiltInRegistries.BLOCK_ENTITY_TYPE,
        FabricaMod.id("me_drive"),
        new BlockEntityType<>(MeDriveBlockEntity::new, Set.of(ModBlocks.ME_DRIVE))
    );

    public static final BlockEntityType<MeGridBlockEntity> ME_GRID = Registry.register(
        BuiltInRegistries.BLOCK_ENTITY_TYPE,
        FabricaMod.id("me_grid"),
        new BlockEntityType<>(MeGridBlockEntity::new, Set.of(ModBlocks.ME_GRID))
    );

    public static void register() {
    }

    private ModBlockEntities() {
    }
}

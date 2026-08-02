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

// Реестр типов BlockEntity: каждый тип привязывает конструктор сущности к блокам.
public final class ModBlockEntities {

    // Генератор: сжигает топливо и вырабатывает энергию.
    public static final BlockEntityType<GeneratorBlockEntity> GENERATOR = Registry.register(
        BuiltInRegistries.BLOCK_ENTITY_TYPE,
        FabricaMod.id("generator"),
        new BlockEntityType<>(GeneratorBlockEntity::new, Set.of(ModBlocks.GENERATOR))
    );

    // Электропечь: потребляет энергию для переплавки предметов.
    public static final BlockEntityType<ElectricFurnaceBlockEntity> ELECTRIC_FURNACE = Registry.register(
        BuiltInRegistries.BLOCK_ENTITY_TYPE,
        FabricaMod.id("electric_furnace"),
        new BlockEntityType<>(ElectricFurnaceBlockEntity::new, Set.of(ModBlocks.ELECTRIC_FURNACE))
    );

    // Дисковод ME: хранит диски и предоставляет MeStorage.
    public static final BlockEntityType<MeDriveBlockEntity> ME_DRIVE = Registry.register(
        BuiltInRegistries.BLOCK_ENTITY_TYPE,
        FabricaMod.id("me_drive"),
        new BlockEntityType<>(MeDriveBlockEntity::new, Set.of(ModBlocks.ME_DRIVE))
    );

    // ME-сетка: точка доступа к общей MeNetwork.
    public static final BlockEntityType<MeGridBlockEntity> ME_GRID = Registry.register(
        BuiltInRegistries.BLOCK_ENTITY_TYPE,
        FabricaMod.id("me_grid"),
        new BlockEntityType<>(MeGridBlockEntity::new, Set.of(ModBlocks.ME_GRID))
    );

    // Заглушка: регистрация типов происходит при статической инициализации класса.
    public static void register() {
    }

    private ModBlockEntities() {
    }
}

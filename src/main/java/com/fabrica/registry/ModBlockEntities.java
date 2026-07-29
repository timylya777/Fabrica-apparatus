package com.fabrica.registry;

import com.fabrica.FabricaMod;
import com.fabrica.block.entity.CoalGeneratorBlockEntity;
import com.fabrica.block.entity.CrusherBlockEntity;
import com.fabrica.block.entity.ElectricFurnaceBlockEntity;
import com.fabrica.block.entity.EnergyCableBlockEntity;
import com.fabrica.energy.MachineTier;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.Set;

public class ModBlockEntities {
    public static final BlockEntityType<CoalGeneratorBlockEntity> COAL_GENERATOR = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE, FabricaMod.id("coal_generator"),
            new BlockEntityType<>(CoalGeneratorBlockEntity::new, Set.of(ModBlocks.COAL_GENERATOR))
    );

    public static final BlockEntityType<ElectricFurnaceBlockEntity> ELECTRIC_FURNACE = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE, FabricaMod.id("electric_furnace"),
            new BlockEntityType<>(ElectricFurnaceBlockEntity::new, Set.of(ModBlocks.ELECTRIC_FURNACE))
    );

    public static final BlockEntityType<EnergyCableBlockEntity> ENERGY_CABLE = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE, FabricaMod.id("energy_cable"),
            new BlockEntityType<>(EnergyCableBlockEntity::new, Set.of(ModBlocks.ENERGY_CABLE))
    );

    public static final BlockEntityType<CrusherBlockEntity> CRUSHER = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE, FabricaMod.id("crusher"),
            new BlockEntityType<>((pos, state) -> new CrusherBlockEntity(pos, state, MachineTier.BASIC),
                    Set.of(ModBlocks.CRUSHER_BASIC, ModBlocks.CRUSHER_ADVANCED, ModBlocks.CRUSHER_ELITE, ModBlocks.CRUSHER_ULTIMATE))
    );

    public static void register() {}
}

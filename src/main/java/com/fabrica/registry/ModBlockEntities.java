
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

public class ModBlockEntities {
    public static final BlockEntityType<CoalGeneratorBlockEntity> COAL_GENERATOR = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE, FabricaMod.id("coal_generator"),
            BlockEntityType.Builder.of(CoalGeneratorBlockEntity::new, ModBlocks.COAL_GENERATOR).build(null)
    );

    public static final BlockEntityType<ElectricFurnaceBlockEntity> ELECTRIC_FURNACE = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE, FabricaMod.id("electric_furnace"),
            BlockEntityType.Builder.of(ElectricFurnaceBlockEntity::new, ModBlocks.ELECTRIC_FURNACE).build(null)
    );

    public static final BlockEntityType<EnergyCableBlockEntity> ENERGY_CABLE = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE, FabricaMod.id("energy_cable"),
            BlockEntityType.Builder.of(EnergyCableBlockEntity::new, ModBlocks.ENERGY_CABLE).build(null)
    );

    public static final BlockEntityType<CrusherBlockEntity> CRUSHER = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE, FabricaMod.id("crusher"),
            BlockEntityType.Builder.of((pos, state) -> new CrusherBlockEntity(pos, state, MachineTier.BASIC), 
                    ModBlocks.CRUSHER_BASIC, ModBlocks.CRUSHER_ADVANCED, ModBlocks.CRUSHER_ELITE, ModBlocks.CRUSHER_ULTIMATE).build(null)
    );

    public static void register() {}
}
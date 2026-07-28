package com.fabrica.registry;

import com.fabrica.block.machine.CoalGeneratorBlock;
import com.fabrica.block.machine.ElectricFurnaceBlock;
import com.fabrica.block.machine.MachineCasingBlock;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class ModBlocks {

    public static final Block MACHINE_CASING = Registry.register(
            BuiltInRegistries.BLOCK,
            ModBlockIds.MACHINE_CASING,
            new MachineCasingBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ModBlockIds.MACHINE_CASING)
                            .strength(5.0F)
                            .sound(SoundType.METAL)
            )
    );

    public static final Block COAL_GENERATOR = Registry.register(
            BuiltInRegistries.BLOCK,
            ModBlockIds.COAL_GENERATOR,
            new CoalGeneratorBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ModBlockIds.COAL_GENERATOR)
                            .strength(5.0F)
                            .sound(SoundType.METAL)
            )
    );

    public static final Block ELECTRIC_FURNACE = Registry.register(
            BuiltInRegistries.BLOCK,
            ModBlockIds.ELECTRIC_FURNACE,
            new ElectricFurnaceBlock(
                    BlockBehaviour.Properties.of()
                            .setId(ModBlockIds.ELECTRIC_FURNACE)
                            .strength(5.0F)
                            .sound(SoundType.METAL)
            )
    );

    public static void register() {
    }
}

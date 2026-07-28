package com.fabrica.registry;

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

    public static void register() {
    }
}

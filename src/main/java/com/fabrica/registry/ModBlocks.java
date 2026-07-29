
package com.fabrica.registry;

import com.fabrica.FabricaMod;
import com.fabrica.block.machine.CoalGeneratorBlock;
import com.fabrica.block.machine.CrusherBlock;
import com.fabrica.block.machine.ElectricFurnaceBlock;
import com.fabrica.block.machine.EnergyCableBlock;
import com.fabrica.energy.MachineTier;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class ModBlocks {
    public static final Block COAL_GENERATOR = register("coal_generator", new CoalGeneratorBlock(BlockBehaviour.Properties.of().strength(3.0f).sound(SoundType.METAL)));
    public static final Block ELECTRIC_FURNACE = register("electric_furnace", new ElectricFurnaceBlock(BlockBehaviour.Properties.of().strength(3.0f).sound(SoundType.METAL)));
    public static final Block ENERGY_CABLE = register("energy_cable", new EnergyCableBlock(BlockBehaviour.Properties.of().strength(2.0f).sound(SoundType.METAL).noOcclusion()));

    public static final Block CRUSHER_BASIC = register("crusher_basic", new CrusherBlock(BlockBehaviour.Properties.of().strength(3.0f).sound(SoundType.METAL), MachineTier.BASIC));
    public static final Block CRUSHER_ADVANCED = register("crusher_advanced", new CrusherBlock(BlockBehaviour.Properties.of().strength(3.5f).sound(SoundType.METAL), MachineTier.ADVANCED));
    public static final Block CRUSHER_ELITE = register("crusher_elite", new CrusherBlock(BlockBehaviour.Properties.of().strength(4.0f).sound(SoundType.METAL), MachineTier.ELITE));
    public static final Block CRUSHER_ULTIMATE = register("crusher_ultimate", new CrusherBlock(BlockBehaviour.Properties.of().strength(5.0f).sound(SoundType.METAL), MachineTier.ULTIMATE));

    private static Block register(String name, Block block) {
        return Registry.register(BuiltInRegistries.BLOCK, FabricaMod.id(name), block);
    }

    public static void register() {}
}
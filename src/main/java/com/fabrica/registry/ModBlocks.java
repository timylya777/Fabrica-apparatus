package com.fabrica.registry;

import com.fabrica.FabricaMod;
import com.fabrica.block.machine.CoalGeneratorBlock;
import com.fabrica.block.machine.CrusherBlock;
import com.fabrica.block.machine.ElectricFurnaceBlock;
import com.fabrica.block.machine.EnergyCableBlock;
import com.fabrica.energy.MachineTier;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Function;

public class ModBlocks {
    public static final Block COAL_GENERATOR = register("coal_generator", CoalGeneratorBlock::new,
            BlockBehaviour.Properties.of().strength(3.0f).sound(SoundType.METAL));
    public static final Block ELECTRIC_FURNACE = register("electric_furnace", ElectricFurnaceBlock::new,
            BlockBehaviour.Properties.of().strength(3.0f).sound(SoundType.METAL));
    public static final Block ENERGY_CABLE = register("energy_cable", EnergyCableBlock::new,
            BlockBehaviour.Properties.of().strength(2.0f).sound(SoundType.METAL).noOcclusion());

    public static final Block CRUSHER_BASIC = register("crusher_basic",
            props -> new CrusherBlock(props, MachineTier.BASIC),
            BlockBehaviour.Properties.of().strength(3.0f).sound(SoundType.METAL));
    public static final Block CRUSHER_ADVANCED = register("crusher_advanced",
            props -> new CrusherBlock(props, MachineTier.ADVANCED),
            BlockBehaviour.Properties.of().strength(3.5f).sound(SoundType.METAL));
    public static final Block CRUSHER_ELITE = register("crusher_elite",
            props -> new CrusherBlock(props, MachineTier.ELITE),
            BlockBehaviour.Properties.of().strength(4.0f).sound(SoundType.METAL));
    public static final Block CRUSHER_ULTIMATE = register("crusher_ultimate",
            props -> new CrusherBlock(props, MachineTier.ULTIMATE),
            BlockBehaviour.Properties.of().strength(5.0f).sound(SoundType.METAL));

    private static <T extends Block> T register(String name, Function<BlockBehaviour.Properties, T> factory, BlockBehaviour.Properties properties) {
        ResourceKey<Block> key = ResourceKey.create(BuiltInRegistries.BLOCK.key(), FabricaMod.id(name));
        properties.setId(key);
        T block = factory.apply(properties);
        return Registry.register(BuiltInRegistries.BLOCK, key, block);
    }

    public static void register() {}
}

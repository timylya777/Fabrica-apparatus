package com.fabrica.block.machine.generator;

import com.fabrica.api.energy.EnergyTier;
import com.fabrica.block.machine.MachineBlock;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class GeneratorBlock extends MachineBlock {
    public static final MapCodec<GeneratorBlock> CODEC = simpleCodec(GeneratorBlock::new);

    private final long capacity;
    private final EnergyTier tier;
    private final long productionRate;

    public GeneratorBlock(BlockBehaviour.Properties properties) {
        this(properties, 0, EnergyTier.LV, 0);
    }

    public GeneratorBlock(BlockBehaviour.Properties properties, long capacity, EnergyTier tier, long productionRate) {
        super(properties);
        this.capacity = capacity;
        this.tier = tier;
        this.productionRate = productionRate;
    }

    @Override
    public MapCodec<? extends GeneratorBlock> codec() {
        return CODEC;
    }

    public long getCapacity() { return capacity; }
    public EnergyTier getTier() { return tier; }
    public long getProductionRate() { return productionRate; }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GeneratorBlockEntity(pos, state, capacity, tier, productionRate);
    }
}

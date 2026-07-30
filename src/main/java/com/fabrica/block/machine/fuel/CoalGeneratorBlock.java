package com.fabrica.block.machine.fuel;

import com.fabrica.api.energy.EnergyTier;
import com.fabrica.block.machine.MachineBlock;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class CoalGeneratorBlock extends MachineBlock {
    public static final MapCodec<CoalGeneratorBlock> CODEC = simpleCodec(CoalGeneratorBlock::new);

    private final long capacity;
    private final EnergyTier tier;
    private final long productionRate;

    public CoalGeneratorBlock(BlockBehaviour.Properties properties) {
        this(properties, 0, EnergyTier.LV, 0);
    }

    public CoalGeneratorBlock(BlockBehaviour.Properties properties, long capacity, EnergyTier tier, long productionRate) {
        super(properties);
        this.capacity = capacity;
        this.tier = tier;
        this.productionRate = productionRate;
    }

    @Override
    public MapCodec<? extends CoalGeneratorBlock> codec() {
        return CODEC;
    }

    @Override
    public MapCodec<? extends CoalGeneratorBlock> codec() {
        return CODEC;
    }

    public long getCapacity() { return capacity; }
    public EnergyTier getTier() { return tier; }
    public long getProductionRate() { return productionRate; }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CoalGeneratorBlockEntity(pos, state, capacity, tier, productionRate);
    }
}

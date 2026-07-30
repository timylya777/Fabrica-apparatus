package com.fabrica.block.machine.generator;

import com.fabrica.api.energy.EnergyTier;
import com.fabrica.block.machine.MachineBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class GeneratorBlock extends MachineBlock {
    private final long capacity;
    private final EnergyTier tier;
    private final long productionRate;

    public GeneratorBlock(BlockBehaviour.Properties properties, long capacity, EnergyTier tier, long productionRate) {
        super(properties);
        this.capacity = capacity;
        this.tier = tier;
        this.productionRate = productionRate;
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

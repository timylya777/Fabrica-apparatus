package com.fabrica.block.machine.consumer;

import com.fabrica.api.energy.EnergyTier;
import com.fabrica.block.machine.MachineBlock;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ConsumerBlock extends MachineBlock {
    public static final MapCodec<ConsumerBlock> CODEC = simpleCodec(ConsumerBlock::new);

    private final long capacity;
    private final EnergyTier tier;
    private final long consumptionRate;

    public ConsumerBlock(BlockBehaviour.Properties properties) {
        this(properties, 0, EnergyTier.LV, 0);
    }

    public ConsumerBlock(BlockBehaviour.Properties properties, long capacity, EnergyTier tier, long consumptionRate) {
        super(properties);
        this.capacity = capacity;
        this.tier = tier;
        this.consumptionRate = consumptionRate;
    }

    @Override
    public MapCodec<? extends ConsumerBlock> codec() {
        return CODEC;
    }

    public long getCapacity() { return capacity; }
    public EnergyTier getTier() { return tier; }
    public long getConsumptionRate() { return consumptionRate; }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ConsumerBlockEntity(pos, state, capacity, tier, consumptionRate);
    }
}

package com.fabrica.block.machine.consumer;

import com.fabrica.api.energy.EnergyConsumer;
import com.fabrica.api.energy.EnergyTier;
import com.fabrica.block.machine.EnergyMachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class ConsumerBlockEntity extends EnergyMachineBlockEntity implements EnergyConsumer {
    private final long consumptionRate;
    private final EnergyTier consumeTier;

    public ConsumerBlockEntity(BlockPos pos, BlockState state) {
        super(com.fabrica.block.ModBlockEntities.CONSUMER, pos, state, 0, EnergyTier.LV);
        if (state.getBlock() instanceof ConsumerBlock cb) {
            this.consumptionRate = cb.getConsumptionRate();
            this.consumeTier = cb.getTier();
            this.energyStorage = new com.fabrica.api.energy.EnergyStorageComponent(cb.getCapacity(), cb.getTier()) {
                @Override
                protected void onEnergyChanged() { setChanged(); }
            };
        } else {
            this.consumptionRate = 0;
            this.consumeTier = EnergyTier.LV;
        }
    }

    public ConsumerBlockEntity(BlockPos pos, BlockState state, long capacity, EnergyTier tier, long consumptionRate) {
        super(com.fabrica.block.ModBlockEntities.CONSUMER, pos, state, capacity, tier);
        this.consumptionRate = consumptionRate;
        this.consumeTier = tier;
    }

    @Override
    public long getEnergyDemand() {
        return Math.min(consumptionRate, energyStorage.getCapacity() - energyStorage.getEnergy());
    }

    @Override
    public void receiveEnergy(long amount) {
        energyStorage.addEnergy(amount);
    }

    @Override
    public EnergyTier getConsumeTier() {
        return consumeTier;
    }

    @Override
    public EnergyConsumer getEnergyConsumer() {
        return this;
    }

    @Override
    public void serverTick() {
    }
}

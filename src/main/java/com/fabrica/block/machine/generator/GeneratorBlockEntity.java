package com.fabrica.block.machine.generator;

import com.fabrica.api.energy.EnergyProducer;
import com.fabrica.api.energy.EnergyTier;
import com.fabrica.block.machine.EnergyMachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class GeneratorBlockEntity extends EnergyMachineBlockEntity implements EnergyProducer {
    private final long productionRate;
    private final EnergyTier produceTier;

    public GeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(com.fabrica.block.ModBlockEntities.GENERATOR, pos, state, 0, EnergyTier.LV);
        if (state.getBlock() instanceof GeneratorBlock gb) {
            this.productionRate = gb.getProductionRate();
            this.produceTier = gb.getTier();
            this.energyStorage = new com.fabrica.api.energy.EnergyStorageComponent(gb.getCapacity(), gb.getTier()) {
                @Override
                protected void onEnergyChanged() { setChanged(); }
            };
        } else {
            this.productionRate = 0;
            this.produceTier = EnergyTier.LV;
        }
    }

    public GeneratorBlockEntity(BlockPos pos, BlockState state, long capacity, EnergyTier tier, long productionRate) {
        super(com.fabrica.block.ModBlockEntities.GENERATOR, pos, state, capacity, tier);
        this.productionRate = productionRate;
        this.produceTier = tier;
    }

    @Override
    public long produceEnergy() {
        long produced = Math.min(productionRate, energyStorage.getCapacity() - energyStorage.getEnergy());
        if (produced > 0) {
            energyStorage.addEnergy(produced);
        }
        return produced;
    }

    @Override
    public EnergyTier getProduceTier() {
        return produceTier;
    }

    @Override
    public EnergyProducer getEnergyProducer() {
        return this;
    }

    @Override
    public void serverTick() {
        produceEnergy();
    }
}

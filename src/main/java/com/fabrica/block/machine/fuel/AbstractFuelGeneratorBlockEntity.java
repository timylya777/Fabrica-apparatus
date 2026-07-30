package com.fabrica.block.machine.fuel;

import com.fabrica.api.energy.EnergyTier;
import com.fabrica.api.energy.EnergyProducer;
import com.fabrica.block.machine.EnergyMachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public abstract class AbstractFuelGeneratorBlockEntity extends EnergyMachineBlockEntity implements EnergyProducer {

    protected final SimpleContainer fuelInventory;
    protected final long productionRate;
    protected final EnergyTier produceTier;

    protected int burnTime = 0;
    protected int totalBurnTime = 0;

    public AbstractFuelGeneratorBlockEntity(
        BlockEntityType<?> type, BlockPos pos, BlockState state,
        long capacity, EnergyTier tier, long productionRate
    ) {
        super(type, pos, state, capacity, tier);
        this.productionRate = productionRate;
        this.produceTier = tier;
        this.fuelInventory = new SimpleContainer(1) {
            @Override
            public void setChanged() {
                AbstractFuelGeneratorBlockEntity.this.setChanged();
            }
        };
    }

    @Override
    public long produceEnergy() {
        if (burnTime <= 0) {
            ItemStack fuel = fuelInventory.getItem(0);
            if (!fuel.isEmpty()) {
                int fuelBurnTime = getFuelBurnTime(fuel);
                if (fuelBurnTime > 0) {
                    fuel.shrink(1);
                    burnTime = fuelBurnTime;
                    totalBurnTime = fuelBurnTime;
                }
            }
        }

        if (burnTime > 0) {
            burnTime--;
            long produced = Math.min(productionRate, energyStorage.getCapacity() - energyStorage.getEnergy());
            if (produced > 0) {
                energyStorage.addEnergy(produced);
            }
            return produced;
        }

        return 0;
    }

    protected abstract int getFuelBurnTime(ItemStack fuel);

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

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("burnTime", burnTime);
        output.putInt("totalBurnTime", totalBurnTime);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.burnTime = input.getIntOr("burnTime", 0);
        this.totalBurnTime = input.getIntOr("totalBurnTime", 0);
    }
}

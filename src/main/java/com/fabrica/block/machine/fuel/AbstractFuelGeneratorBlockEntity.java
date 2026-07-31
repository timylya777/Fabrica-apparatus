package com.fabrica.block.machine.fuel;

import com.fabrica.api.energy.CableTier;
import com.fabrica.api.energy.EnergyApiLookup;
import com.fabrica.api.energy.EnergyConsumer;
import com.fabrica.api.energy.EnergyTier;
import com.fabrica.api.energy.EnergyProducer;
import com.fabrica.api.energy.IEnergyConnectable;
import com.fabrica.block.machine.EnergyMachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public abstract class AbstractFuelGeneratorBlockEntity extends EnergyMachineBlockEntity implements EnergyProducer {

    protected final SimpleContainer fuelInventory;
    protected long productionRate;
    protected EnergyTier produceTier;

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
        if (energyStorage.isFull()) {
            return 0;
        }

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

    protected int getFuelBurnTime(ItemStack fuel) {
        if (level == null) return 0;
        return level.fuelValues().burnDuration(fuel);
    }

    public SimpleContainer getFuelInventory() {
        return fuelInventory;
    }

    public boolean isBurning() {
        return burnTime > 0;
    }

    public int getBurnTime() {
        return burnTime;
    }

    public int getTotalBurnTime() {
        return totalBurnTime;
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
        pushToNeighbors();
    }

    private void pushToNeighbors() {
        if (level == null || level.isClientSide()) return;
        long available = energyStorage.getEnergy();
        if (available <= 0) return;
        for (Direction dir : Direction.values()) {
            if (available <= 0) break;
            BlockPos neighborPos = worldPosition.relative(dir);
            BlockState neighborState = level.getBlockState(neighborPos);
            if (!(neighborState.getBlock() instanceof IEnergyConnectable connectable)) continue;
            if (!connectable.canConnectEnergy(neighborPos, neighborState, dir.getOpposite())) continue;
            EnergyConsumer consumer = EnergyApiLookup.CONSUMER.find(level, neighborPos, dir.getOpposite());
            if (consumer == null) continue;
            long demand = consumer.getEnergyDemand();
            if (demand <= 0) continue;
            long toSend = Math.min(available, Math.min(CableTier.COPPER_LV.maxTransfer(), demand));
            if (toSend <= 0) continue;
            consumer.receiveEnergy(toSend);
            available -= toSend;
            energyStorage.removeEnergy(toSend);
        }
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

package com.fabrica.block.machine.process;

import com.fabrica.api.energy.EnergyConsumer;
import com.fabrica.api.energy.EnergyTier;
import com.fabrica.api.machine.FabricationRecipe;
import com.fabrica.block.machine.EnergyMachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.List;

public abstract class AbstractProcessingMachineBlockEntity extends EnergyMachineBlockEntity implements EnergyConsumer {

    protected final SimpleContainer inputInventory;
    protected final SimpleContainer outputInventory;
    protected final long consumptionRate;
    protected final EnergyTier consumeTier;

    protected int processProgress = 0;
    protected int processTotal = 0;
    protected FabricationRecipe currentRecipe = null;

    public AbstractProcessingMachineBlockEntity(
        BlockEntityType<?> type, BlockPos pos, BlockState state,
        long capacity, EnergyTier tier, long consumptionRate
    ) {
        super(type, pos, state, capacity, tier);
        this.consumptionRate = consumptionRate;
        this.consumeTier = tier;
        this.inputInventory = new SimpleContainer(1) {
            @Override
            public void setChanged() {
                AbstractProcessingMachineBlockEntity.this.setChanged();
            }
        };
        this.outputInventory = new SimpleContainer(1) {
            @Override
            public void setChanged() {
                AbstractProcessingMachineBlockEntity.this.setChanged();
            }
        };
    }

    @Override
    public long getEnergyDemand() {
        if (canProcess()) {
            return consumptionRate;
        }
        return 0;
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

    protected boolean canProcess() {
        ItemStack input = inputInventory.getItem(0);
        if (input.isEmpty()) return false;

        FabricationRecipe recipe = findRecipe(input);
        if (recipe == null) return false;

        ItemStack output = recipe.getOutput();
        ItemStack currentOutput = outputInventory.getItem(0);
        if (currentOutput.isEmpty()) return true;
        if (!ItemStack.isSameItemSameComponents(currentOutput, output)) return false;
        return currentOutput.getCount() + output.getCount() <= currentOutput.getMaxStackSize();
    }

    @Override
    public void serverTick() {
        if (energyStorage.getEnergy() <= 0) return;

        if (currentRecipe == null || !currentRecipe.matches(inputInventory.getItem(0))) {
            currentRecipe = findRecipe(inputInventory.getItem(0));
            processProgress = 0;
            processTotal = currentRecipe != null ? currentRecipe.getProcessTime() : 0;
        }

        if (currentRecipe == null) return;

        long energyNeeded = Math.min(consumptionRate, energyStorage.getEnergy());
        if (energyNeeded <= 0) return;

        energyStorage.removeEnergy(energyNeeded);
        processProgress++;

        if (processProgress >= processTotal) {
            finishProcessing();
        }
    }

    protected void finishProcessing() {
        if (currentRecipe == null) return;

        ItemStack output = currentRecipe.getOutput().copy();
        ItemStack currentOutput = outputInventory.getItem(0);
        if (currentOutput.isEmpty()) {
            outputInventory.setItem(0, output);
        } else {
            currentOutput.grow(output.getCount());
        }

        inputInventory.getItem(0).shrink(1);
        processProgress = 0;
        currentRecipe = null;
    }

    protected abstract List<FabricationRecipe> getRecipes();

    protected FabricationRecipe findRecipe(ItemStack input) {
        if (input.isEmpty()) return null;
        for (FabricationRecipe recipe : getRecipes()) {
            if (recipe.matches(input)) return recipe;
        }
        return null;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("processProgress", processProgress);
        output.putInt("processTotal", processTotal);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.processProgress = input.getIntOr("processProgress", 0);
        this.processTotal = input.getIntOr("processTotal", 0);
    }
}

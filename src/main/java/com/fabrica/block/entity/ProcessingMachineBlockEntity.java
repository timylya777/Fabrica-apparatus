
package com.fabrica.block.entity;

import com.fabrica.FabricaMod;
import com.fabrica.energy.APConsumer;
import com.fabrica.energy.MachineTier;
import com.fabrica.menu.ProcessingMachineMenu;
import com.fabrica.recipe.MachineType;
import com.fabrica.recipe.ProcessingOutput;
import com.fabrica.recipe.ProcessingRecipe;
import com.fabrica.recipe.ProcessingRecipeInput;
import com.fabrica.registry.ModRecipes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.List;
import java.util.Optional;

public abstract class ProcessingMachineBlockEntity extends OverloadableMachineBlockEntity implements APConsumer, WorldlyContainer {
    protected final MachineType machineType;
    protected final MachineTier tier;
    protected final NonNullList<ItemStack> inventory;
    protected final int inputSlots;
    protected final int outputSlots;
    protected int progress = 0;
    protected int currentProcessTime = 0;
    protected ProcessingRecipe cachedRecipe = null;
    protected int ticksSinceRecheck = 0;
    protected static final int RECIPE_RECHECK_INTERVAL = 20;

    protected final ContainerData propertyDelegate = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> currentProcessTime;
                case 2 -> overloadLevel;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> progress = value;
                case 1 -> currentProcessTime = value;
                case 2 -> overloadLevel = value;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    };

    public ProcessingMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, MachineType machineType, MachineTier tier, int inputSlots, int outputSlots) {
        super(type, pos, state);
        this.machineType = machineType;
        this.tier = tier;
        this.inputSlots = inputSlots;
        this.outputSlots = outputSlots;
        this.inventory = NonNullList.withSize(inputSlots + outputSlots, ItemStack.EMPTY);
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        if (level != null && !level.isClientSide()) {
            FabricaMod.getManager(level).onBlockAdded(getBlockPos(), this);
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (getLevel() != null && !getLevel().isClientSide()) {
            FabricaMod.getManager(getLevel()).onBlockRemoved(getBlockPos());
        }
    }

    @Override
    public void serverTick(Level level, BlockPos pos, BlockState state) {
        if (this.isRemoved() || !isOperatingNormally()) {
            decay();
            return;
        }

        if (ticksSinceRecheck >= RECIPE_RECHECK_INTERVAL || cachedRecipe == null) {
            List<ItemStack> inputs = inventory.subList(0, inputSlots);
            Optional<RecipeHolder<ProcessingRecipe>> recipeOpt = level.getServer().getRecipeManager()
                    .getRecipeFor(ModRecipes.PROCESSING_TYPE, new ProcessingRecipeInput(inputs), level);
            
            if (recipeOpt.isPresent()) {
                cachedRecipe = recipeOpt.get().value();
                currentProcessTime = tier.getProcessingTicks(cachedRecipe.getProcessTime());
                ticksSinceRecheck = 0;
            } else {
                cachedRecipe = null;
                currentProcessTime = 0;
                progress = 0;
                return;
            }
        }
        ticksSinceRecheck++;

        if (cachedRecipe == null) return;

        if (!canAcceptOutput(cachedRecipe.getOutputs())) {
            decay();
            return;
        }

        long energyRequired = tier.getEnergyPerOperation(cachedRecipe.getEnergyCost()) / currentProcessTime;
        if (energyRequired == 0) energyRequired = 1;

        if (energyStorage.amount >= energyRequired) {
            energyStorage.amount -= energyRequired;
            progress++;
            this.overloadLevel = Math.max(0, this.overloadLevel - 1);
            setChanged();

            if (progress >= currentProcessTime) {
                craftRecipe(cachedRecipe);
                progress = 0;
                cachedRecipe = null;
                currentProcessTime = 0;
                ticksSinceRecheck = RECIPE_RECHECK_INTERVAL;
            }
        } else {
            decay();
        }
    }

    protected boolean canAcceptOutput(List<ProcessingOutput> outputs) {
        for (ProcessingOutput output : outputs) {
            if (output.chance() < 1.0f && getLevel() != null && getLevel().getRandom().nextFloat() >= output.chance()) {
                continue;
            }
            ItemStack outStack = output.stack();
            if (outStack.isEmpty()) continue;

            boolean foundSlot = false;
            for (int slot = inputSlots; slot < inventory.size(); slot++) {
                ItemStack slotStack = inventory.get(slot);
                if (slotStack.isEmpty()) {
                    foundSlot = true;
                    break;
                }
                if (ItemStack.isSameItemSameComponents(slotStack, outStack) && slotStack.getCount() + outStack.getCount() <= slotStack.getMaxStackSize()) {
                    foundSlot = true;
                    break;
                }
            }
            if (!foundSlot) return false;
        }
        return true;
    }

    protected void craftRecipe(ProcessingRecipe recipe) {
        for (int i = 0; i < inputSlots; i++) {
            ItemStack stack = inventory.get(i);
            if (!stack.isEmpty()) {
                stack.shrink(1);
            }
        }

        for (ProcessingOutput output : recipe.getOutputs()) {
            if (getLevel() != null && getLevel().getRandom().nextFloat() >= output.chance()) {
                continue;
            }
            ItemStack outStack = output.stack().copy();
            if (outStack.isEmpty()) continue;

            for (int slot = inputSlots; slot < inventory.size(); slot++) {
                ItemStack slotStack = inventory.get(slot);
                if (slotStack.isEmpty()) {
                    inventory.set(slot, outStack.copy());
                    break;
                }
                if (ItemStack.isSameItemSameComponents(slotStack, outStack) && slotStack.getCount() + outStack.getCount() <= slotStack.getMaxStackSize()) {
                    slotStack.grow(outStack.getCount());
                    break;
                }
            }
        }
        setChanged();
    }

    protected void decay() {
        progress = Math.max(0, progress - 2);
        this.overloadLevel = Math.max(0, this.overloadLevel - 1);
        setChanged();
    }

    @Override
    protected boolean isOperatingNormally() {
        return this.overloadLevel < this.maxOverload;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        if (side == Direction.DOWN) {
            int[] outputs = new int[outputSlots];
            for (int i = 0; i < outputSlots; i++) outputs[i] = inputSlots + i;
            return outputs;
        } else {
            int[] inputs = new int[inputSlots];
            for (int i = 0; i < inputSlots; i++) inputs[i] = i;
            return inputs;
        }
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction dir) {
        return slot < inputSlots;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
        return slot >= inputSlots;
    }

    @Override
    protected NonNullList<ItemStack> getMachineInventory() {
        return inventory;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        ContainerHelper.loadAllItems(input, inventory);
        this.progress = input.getIntOr("Progress", 0);
        this.currentProcessTime = input.getIntOr("CurrentProcessTime", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, inventory);
        output.putInt("Progress", progress);
        output.putInt("CurrentProcessTime", currentProcessTime);
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ProcessingMachineMenu(null, containerId, inventory, this, propertyDelegate, inputSlots, outputSlots);
    }
}
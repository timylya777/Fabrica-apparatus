package com.fabrica.block.entity;

import com.fabrica.FabricaMod;
import com.fabrica.energy.APConsumer;
import com.fabrica.menu.ElectricFurnaceMenu;
import com.fabrica.recipe.FurnaceRecipe;
import com.fabrica.registry.ModBlockEntities;
import com.fabrica.registry.ModRecipes;

import team.reborn.energy.api.base.SimpleEnergyStorage;

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
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class ElectricFurnaceBlockEntity extends OverloadableMachineBlockEntity implements APConsumer, WorldlyContainer {

    private final NonNullList<ItemStack> inventory = NonNullList.withSize(2, ItemStack.EMPTY);
    private int progress = 0;
    private int totalCookTime = 0;

    public ElectricFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ELECTRIC_FURNACE, pos, state);
        this.energyStorage = new SimpleEnergyStorage(5000, 32, 0) {
            @Override
            protected void onFinalCommit() {
                setChanged();
            }
        };
    }

    @Override
    public SimpleEnergyStorage getStorage() {
        return this.energyStorage;
    }

    @Override
    protected NonNullList<ItemStack> getMachineInventory() {
        return inventory;
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
        if (this.isRemoved()) return;

        ItemStack input = inventory.get(0);
        ItemStack output = inventory.get(1);

        var recipe = level.getServer().getRecipeManager()
            .getRecipeFor(ModRecipes.FURNACE_TYPE, new SingleRecipeInput(input), level)
            .orElse(null);

        if (recipe != null) {
            ItemStack result = recipe.value().result().copy();
            int cookTime = recipe.value().cookTime();
            boolean canAcceptOutput = output.isEmpty()
                || (ItemStack.isSameItemSameComponents(output, result) && output.getCount() < output.getMaxStackSize());

            if (canAcceptOutput) {
                if (progress == 0) totalCookTime = cookTime;

                int cost = recipe.value().energyCost();
                if (energyStorage.amount >= cost) {
                    energyStorage.amount -= cost;
                    progress++;
                    this.overloadLevel = Math.max(0, this.overloadLevel - 1);

                    if (progress >= cookTime) {
                        if (output.isEmpty()) {
                            inventory.set(1, result);
                        } else {
                            output.grow(1);
                        }
                        input.shrink(1);
                        progress = 0;
                        totalCookTime = 0;
                    }
                    setChanged();
                } else {
                    progress = Math.max(0, progress - 2);
                    this.overloadLevel = Math.max(0, this.overloadLevel - 1);
                }
            } else {
                progress = Math.max(0, progress - 2);
                this.overloadLevel = Math.max(0, this.overloadLevel - 2);
            }
        } else {
            progress = Math.max(0, progress - 2);
            this.overloadLevel = Math.max(0, this.overloadLevel - 2);
            totalCookTime = 0;
        }
    }

    @Override
    protected boolean isOperatingNormally() {
        return this.overloadLevel < this.maxOverload;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        ContainerHelper.loadAllItems(input, inventory);
        this.progress = input.getIntOr("Progress", 0);
        this.totalCookTime = input.getIntOr("TotalCookTime", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, inventory);
        output.putInt("Progress", progress);
        output.putInt("TotalCookTime", totalCookTime);
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return side == Direction.DOWN ? new int[]{1} : new int[]{0};
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction dir) {
        return slot == 0;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
        return slot == 1;
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ElectricFurnaceMenu(containerId, inventory, this, new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> progress;
                    case 1 -> totalCookTime;
                    case 2 -> (int) energyStorage.amount;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> progress = value;
                    case 1 -> totalCookTime = value;
                    case 2 -> energyStorage.amount = value;
                }
            }

            @Override
            public int getCount() {
                return 3;
            }
        });
    }
}

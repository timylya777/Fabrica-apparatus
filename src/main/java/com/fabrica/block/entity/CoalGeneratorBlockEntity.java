package com.fabrica.block.entity;

import com.fabrica.FabricaMod;
import com.fabrica.energy.APProvider;
import com.fabrica.menu.CoalGeneratorMenu;
import com.fabrica.recipe.FuelRecipe;
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
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class CoalGeneratorBlockEntity extends OverloadableMachineBlockEntity implements APProvider, WorldlyContainer {

    private final NonNullList<ItemStack> inventory = NonNullList.withSize(1, ItemStack.EMPTY);
    private int burnTime = 0;
    private int energyPerTick = 0;

    private static final int MAX_EXTRACT = 32;

    public CoalGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COAL_GENERATOR, pos, state);
        this.energyStorage = new SimpleEnergyStorage(10000, 0, MAX_EXTRACT) {
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

        ItemStack fuelStack = inventory.get(0);

        if (burnTime > 0) {
            long space = energyStorage.capacity - energyStorage.amount;
            if (space >= energyPerTick) {
                energyStorage.amount += energyPerTick;
                burnTime--;
                setChanged();
            } else {
                this.overloadLevel += 2;
            }
        } else if (!fuelStack.isEmpty()) {
            var recipe = level.getRecipeManager()
                .getRecipeFor(ModRecipes.FUEL_TYPE, new SingleRecipeInput(fuelStack), level)
                .orElse(null);
            if (recipe != null) {
                fuelStack.shrink(1);
                burnTime = recipe.value().burnTime();
                energyPerTick = recipe.value().energyPerTick();
                setChanged();
            }
        }
    }

    @Override
    protected boolean isOperatingNormally() {
        return this.overloadLevel < this.maxOverload && (burnTime == 0 || energyStorage.amount < energyStorage.capacity);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        ContainerHelper.loadAllItems(input, inventory);
        this.burnTime = input.getIntOr("BurnTime", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, inventory);
        output.putInt("BurnTime", burnTime);
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return new int[]{0};
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction dir) {
        return slot == 0;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
        return false;
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new CoalGeneratorMenu(containerId, inventory, this, new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> burnTime;
                    case 1 -> overloadLevel;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> burnTime = value;
                    case 1 -> overloadLevel = value;
                }
            }

            @Override
            public int getCount() {
                return 2;
            }
        });
    }
}

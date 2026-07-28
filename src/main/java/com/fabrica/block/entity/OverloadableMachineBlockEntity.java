package com.fabrica.block.entity;

import team.reborn.energy.api.base.SimpleEnergyStorage;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public abstract class OverloadableMachineBlockEntity extends BlockEntity implements Container, MenuProvider {

    protected SimpleEnergyStorage energyStorage;
    protected int overloadLevel = 0;
    protected final int maxOverload = 100;

    public OverloadableMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public SimpleEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    public abstract void serverTick(Level level, BlockPos pos, BlockState state);

    protected boolean isOperatingNormally() {
        return overloadLevel < maxOverload;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        overloadLevel = input.getIntOr("OverloadLevel", 0);
        energyStorage.amount = input.getLongOr("Energy", 0L);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("OverloadLevel", overloadLevel);
        output.putLong("Energy", energyStorage.amount);
    }

    @Override
    public boolean stillValid(Player player) {
        if (getLevel().getBlockEntity(getBlockPos()) != this) return false;
        return player.distanceToSqr(getBlockPos().getX() + 0.5, getBlockPos().getY() + 0.5, getBlockPos().getZ() + 0.5) <= 64.0;
    }

    // Container (inventory) methods — subclasses override with their own item lists
    protected NonNullList<ItemStack> getMachineInventory() {
        return NonNullList.create();
    }

    @Override
    public int getContainerSize() {
        return getMachineInventory().size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : getMachineInventory()) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return getMachineInventory().get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        var stack = getMachineInventory().get(slot);
        if (!stack.isEmpty() && amount > 0) {
            ItemStack result = stack.split(amount);
            setChanged();
            return result;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        var stack = getMachineInventory().get(slot);
        if (!stack.isEmpty()) {
            getMachineInventory().set(slot, ItemStack.EMPTY);
            return stack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        getMachineInventory().set(slot, stack);
        if (!stack.isEmpty() && stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        setChanged();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return true;
    }

    @Override
    public void setChanged() {
        super.setChanged();
    }

    @Override
    public void clearContent() {
        getMachineInventory().clear();
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public abstract AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player);
}

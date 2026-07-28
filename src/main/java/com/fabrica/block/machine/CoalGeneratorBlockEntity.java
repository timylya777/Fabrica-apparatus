package com.fabrica.block.entity.machine;

import com.fabrica.block.entity.OverloadableMachineBlockEntity;
import com.fabrica.energy.EnergyTier;
import com.fabrica.energy.SimpleEnergyStorage;
import com.fabrica.registry.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class CoalGeneratorBlockEntity extends OverloadableMachineBlockEntity implements SidedInventory {
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(1, ItemStack.EMPTY);
    private int burnTime = 0;
    
    // Генератор производит 10 EU/t, но может отдать максимум 32 EU/t (LV тир)
    private static final int ENERGY_PER_TICK = 10;
    private static final int COAL_BURN_TIME = 1600;

    public CoalGeneratorBlockEntity(BlockPos pos, BlockState state) {
        // Ёмкость 10000, это генератор (isGenerator=true), LV тир
        super(ModBlockEntities.COAL_GENERATOR, pos, state, 
              new SimpleEnergyStorage(10000, EnergyTier.LV, true));
    }

    @Override
    protected void serverTick(World world, BlockPos pos, BlockState state) {
        super.serverTick(world, pos, state);
        if (this.isRemoved()) return;

        ItemStack fuel = inventory.get(0);

        if (burnTime > 0) {
            // Пытаемся добавить энергию в буфер
            long added = energyStorage.insertEnergy(ENERGY_PER_TICK, false);
            
            // Если энергия НЕ добавилась (буфер полон или достигнут лимит отдачи)
            if (added < ENERGY_PER_TICK) {
                this.overloadLevel += 2; // Перегрев
            } else {
                burnTime--;
            }
        } else if (fuel.isOf(Items.COAL) || fuel.isOf(Items.CHARCOAL)) {
            fuel.decrement(1);
            burnTime = COAL_BURN_TIME;
        }
        markDirty();
    }

    @Override
    protected boolean isOperatingNormally() {
        return overloadLevel < maxOverload && (burnTime == 0 || !energyStorage.isFull());
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        Inventories.readNbt(nbt, inventory);
        this.burnTime = nbt.getInt("BurnTime");
        this.overloadLevel = nbt.getInt("OverloadLevel");
        this.energyStorage.readNbt(nbt);
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, inventory);
        nbt.putInt("BurnTime", burnTime);
        nbt.putInt("OverloadLevel", overloadLevel);
        this.energyStorage.writeNbt(nbt);
    }

    @Override public DefaultedList<ItemStack> getItems() { return inventory; }
    @Override public int[] getAvailableSlots(Direction side) { return new int[]{0}; }
    @Override public boolean canInsert(int slot, ItemStack stack, Direction dir) { return stack.isOf(Items.COAL) || stack.isOf(Items.CHARCOAL); }
    @Override public boolean canExtract(int slot, ItemStack stack, Direction dir) { return false; }
}
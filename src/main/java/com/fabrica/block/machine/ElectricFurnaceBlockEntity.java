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

public class ElectricFurnaceBlockEntity extends OverloadableMachineBlockEntity implements SidedInventory {
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(2, ItemStack.EMPTY);
    private int progress = 0;
    private static final int MAX_PROGRESS = 200;
    
    // Печка потребляет 5 EU/t, может принять максимум 32 EU/t (LV тир)
    private static final int ENERGY_COST_PER_TICK = 5;

    public ElectricFurnaceBlockEntity(BlockPos pos, BlockState state) {
        // Ёмкость 5000, это машина (isGenerator=false), LV тир
        super(ModBlockEntities.ELECTRIC_FURNACE, pos, state, 
              new SimpleEnergyStorage(5000, EnergyTier.LV, false));
    }

    @Override
    protected void serverTick(World world, BlockPos pos, BlockState state) {
        super.serverTick(world, pos, state);
        if (this.isRemoved()) return;

        ItemStack input = inventory.get(0);
        ItemStack output = inventory.get(1);

        boolean hasRecipe = input.isOf(Items.IRON_ORE);
        boolean canAcceptOutput = output.isEmpty() || (output.isOf(Items.IRON_INGOT) && output.getCount() < output.getMaxCount());

        if (hasRecipe && canAcceptOutput) {
            // Пытаемся извлечь энергию (ограничено maxExtract, но у машины maxExtract=0, поэтому используем внутренний буфер)
            if (energyStorage.getEnergy() >= ENERGY_COST_PER_TICK) {
                // Вручную уменьшаем энергию (т.к. extractEnergy=0 для машин)
                // В будущем это будет handled через internal consumption
                long currentEnergy = energyStorage.getEnergy();
                energyStorage.readNbt(createEnergyNbt(currentEnergy - ENERGY_COST_PER_TICK));
                
                progress++;
                this.overloadLevel = Math.min(maxOverload, this.overloadLevel + 1);
                
                if (progress >= MAX_PROGRESS) {
                    if (output.isEmpty()) {
                        inventory.set(1, new ItemStack(Items.IRON_INGOT));
                    } else {
                        output.increment(1);
                    }
                    input.decrement(1);
                    progress = 0;
                    this.overloadLevel = Math.max(0, this.overloadLevel - 10);
                }
            } else {
                progress = Math.max(0, progress - 2);
            }
        } else {
            progress = Math.max(0, progress - 2);
            this.overloadLevel = Math.max(0, this.overloadLevel - 2);
        }
        markDirty();
    }

    private NbtCompound createEnergyNbt(long energy) {
        NbtCompound nbt = new NbtCompound();
        nbt.putLong("Energy", energy);
        return nbt;
    }

    @Override
    protected boolean isOperatingNormally() {
        return overloadLevel < maxOverload;
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        Inventories.readNbt(nbt, inventory);
        this.progress = nbt.getInt("Progress");
        this.overloadLevel = nbt.getInt("OverloadLevel");
        this.energyStorage.readNbt(nbt);
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, inventory);
        nbt.putInt("Progress", progress);
        nbt.putInt("OverloadLevel", overloadLevel);
        this.energyStorage.writeNbt(nbt);
    }

    @Override public DefaultedList<ItemStack> getItems() { return inventory; }
    @Override public int[] getAvailableSlots(Direction side) { return side == Direction.DOWN ? new int[]{1} : new int[]{0}; }
    @Override public boolean canInsert(int slot, ItemStack stack, Direction dir) { return slot == 0; }
    @Override public boolean canExtract(int slot, ItemStack stack, Direction dir) { return slot == 1; }
}

package com.fabrica.gui;

import com.fabrica.block.ModBlocks;
import com.fabrica.block.machine.furnace.ElectricFurnaceBlockEntity;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ElectricFurnaceMenu extends AbstractContainerMenu {
    private static final int DATA_COUNT = 4;

    private final ContainerData data;
    @Nullable
    private final ElectricFurnaceBlockEntity blockEntity;

    public ElectricFurnaceMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, null, new SimpleContainerData(DATA_COUNT));
    }

    public ElectricFurnaceMenu(int containerId, Inventory inventory, @Nullable ElectricFurnaceBlockEntity blockEntity,
                               ContainerData data) {
        super(ModMenus.ELECTRIC_FURNACE, containerId);
        checkContainerDataCount(data, DATA_COUNT);
        this.blockEntity = blockEntity;
        this.data = data;
        addSlot(new Slot(blockEntity != null ? blockEntity.getInputInventory() : new SimpleContainer(1), 0, 56, 35));
        addSlot(new Slot(blockEntity != null ? blockEntity.getOutputInventory() : new SimpleContainer(1), 0, 116, 35));
        addStandardInventorySlots(inventory, 8, 84);
        addDataSlots(data);
    }

    public long getEnergy() {
        return Integer.toUnsignedLong(data.get(0));
    }

    public long getCapacity() {
        return Integer.toUnsignedLong(data.get(1));
    }

    public int getProgress() {
        return data.get(2);
    }

    public int getTotalTime() {
        return data.get(3);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return blockEntity == null
            || AbstractContainerMenu.stillValid(
                ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()),
                player,
                ModBlocks.ELECTRIC_FURNACE
            );
    }
}

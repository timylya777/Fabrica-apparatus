
package com.fabrica.menu;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ProcessingMachineMenu extends AbstractContainerMenu {
    private final Container container;
    private final ContainerData data;
    private final int inputSlots;
    private final int outputSlots;

    public ProcessingMachineMenu(MenuType<?> menuType, int containerId, Inventory playerInventory, Container container, ContainerData data, int inputSlots, int outputSlots) {
        super(menuType, containerId);
        this.container = container;
        this.data = data;
        this.inputSlots = inputSlots;
        this.outputSlots = outputSlots;

        checkContainerSize(container, inputSlots + outputSlots);
        checkContainerDataCount(data, 3);

        for (int i = 0; i < inputSlots; i++) {
            this.addSlot(new Slot(container, i, 56 + i * 18, 17));
        }
        for (int i = 0; i < outputSlots; i++) {
            this.addSlot(new Slot(container, inputSlots + i, 116 + i * 18, 35));
        }
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }

        this.addDataSlots(data);
    }

    public int getProgress() { return data.get(0); }
    public int getMaxProgress() { return data.get(1); }
    public int getOverloadLevel() { return data.get(2); }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            itemStack = stackInSlot.copy();
            if (index < inputSlots + outputSlots) {
                if (!this.moveItemStackTo(stackInSlot, inputSlots + outputSlots, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stackInSlot, 0, inputSlots + outputSlots, false)) {
                return ItemStack.EMPTY;
            }

            if (stackInSlot.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemStack;
    }
}
package com.fabrica.menu;

import com.fabrica.registry.ModMenuTypes;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ElectricFurnaceMenu extends AbstractContainerMenu {

    private final Container container;
    private final ContainerData data;

    public ElectricFurnaceMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, new SimpleContainer(2), new SimpleContainerData(2));
    }

    public ElectricFurnaceMenu(int containerId, Inventory inventory, Container container, ContainerData data) {
        super(ModMenuTypes.ELECTRIC_FURNACE, containerId);
        this.container = container;
        this.data = data;

        addSlot(new Slot(container, 0, 56, 35));
        addSlot(new Slot(container, 1, 116, 35));

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col, 8 + col * 18, 142));
        }

        addDataSlots(data);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        Slot slot = slots.get(slotIndex);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();

        if (slotIndex == 1) {
            if (!moveItemStackTo(stack, 2, 38, true)) return ItemStack.EMPTY;
        } else if (slotIndex == 0) {
            if (!moveItemStackTo(stack, 2, 38, false)) return ItemStack.EMPTY;
        } else {
            if (!moveItemStackTo(stack, 0, 1, false)) return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();

        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }

    public int getProgress() {
        return data.get(0);
    }

    public int getOverloadLevel() {
        return data.get(1);
    }
}

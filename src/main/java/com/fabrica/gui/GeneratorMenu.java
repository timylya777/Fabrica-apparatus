package com.fabrica.gui;

import com.fabrica.block.ModBlocks;
import com.fabrica.block.machine.generator.GeneratorBlockEntity;
import net.minecraft.world.Container;
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

public class GeneratorMenu extends AbstractContainerMenu {
    private static final int DATA_COUNT = 4;

    private final ContainerData data;
    @Nullable
    private final GeneratorBlockEntity blockEntity;

    public GeneratorMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, null, new SimpleContainerData(DATA_COUNT));
    }

    public GeneratorMenu(int containerId, Inventory inventory, @Nullable GeneratorBlockEntity blockEntity,
                         ContainerData data) {
        super(ModMenus.GENERATOR, containerId);
        checkContainerDataCount(data, DATA_COUNT);
        this.blockEntity = blockEntity;
        this.data = data;
        addSlot(new Slot(blockEntity != null ? blockEntity.getFuelInventory() : new SimpleContainer(1), 0, 56, 53));
        addStandardInventorySlots(inventory, 8, 84);
        addDataSlots(data);
    }

    public long getStoredEnergy() {
        return Integer.toUnsignedLong(data.get(0));
    }

    public long getEnergyCapacity() {
        return Integer.toUnsignedLong(data.get(1));
    }

    public int getBurnTime() {
        return data.get(2);
    }

    public int getTotalBurnTime() {
        return data.get(3);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            stack = slotStack.copy();
            if (index == 0) {
                // fuel slot -> player inventory
                if (!this.moveItemStackTo(slotStack, 1, 37, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // player inventory -> fuel slot
                if (!this.moveItemStackTo(slotStack, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            }
            if (slotStack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            if (slotStack.getCount() == stack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, slotStack);
        }
        return stack;
    }

    @Override
    public boolean stillValid(Player player) {
        return blockEntity == null
            || AbstractContainerMenu.stillValid(
                ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()),
                player,
                ModBlocks.GENERATOR
            );
    }
}

package com.fabrica.gui;

import com.fabrica.block.ModBlocks;
import com.fabrica.block.me.MeDriveBlockEntity;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class MeDriveMenu extends AbstractContainerMenu {
    @Nullable
    private final MeDriveBlockEntity blockEntity;

    public MeDriveMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, null);
    }

    public MeDriveMenu(int containerId, Inventory inventory, @Nullable MeDriveBlockEntity blockEntity) {
        super(ModMenus.ME_DRIVE, containerId);
        this.blockEntity = blockEntity;
        // Слоты 0..7 — диски (2 ряда по 4), слоты 8..43 — инвентарь игрока.
        SimpleContainer disks = blockEntity != null ? blockEntity.getDisks() : new SimpleContainer(8);
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 4; col++) {
                addSlot(new Slot(disks, row * 4 + col, 52 + col * 18, 32 + row * 18));
            }
        }
        addStandardInventorySlots(inventory, 8, 84);
    }

    // Shift+клик: диск — в инвентарь, из инвентаря — в свободный слот диска.
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            stack = slotStack.copy();
            if (index < 8) {
                // disk -> player inventory
                if (!this.moveItemStackTo(slotStack, 8, 44, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // player inventory -> disk slots
                if (!this.moveItemStackTo(slotStack, 0, 8, false)) {
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
                ModBlocks.ME_DRIVE
            );
    }
}

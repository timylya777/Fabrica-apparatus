package com.fabrica.gui;

import com.fabrica.block.machine.anvil.AnvilBlockEntity;
import com.fabrica.item.FigureItem;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class AnvilMenu extends AbstractContainerMenu {
    private static final int DATA_COUNT = 4;
    private final ContainerData data;
    @Nullable
    private final AnvilBlockEntity blockEntity;

    public AnvilMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, null, new SimpleContainerData(DATA_COUNT));
    }

    public AnvilMenu(int containerId, Inventory inventory, @Nullable AnvilBlockEntity blockEntity, ContainerData data) {
        super(ModMenus.ANVIL, containerId);
        checkContainerDataCount(data, DATA_COUNT);
        this.blockEntity = blockEntity;
        this.data = data;
        // Важно: на клиенте blockEntity == null, поэтому для каждого слота нужен
        // СВОЙ пустой контейнер. Если использовать один на всех — предметы после
        // синхронизации будут отображаться сразу во всех ячейках.
        // Слот 0 — фигурка (рабочий), слот 1 — вход (слиток), слот 2 — выход (пластина).
        addSlot(new Slot(blockEntity != null ? blockEntity.getFigureInventory() : new SimpleContainer(1), 0, 26, 35));
        addSlot(new Slot(blockEntity != null ? blockEntity.getInputInventory() : new SimpleContainer(1), 0, 56, 35));
        addSlot(new Slot(blockEntity != null ? blockEntity.getOutputInventory() : new SimpleContainer(1), 0, 116, 35));
        addStandardInventorySlots(inventory, 8, 84);
        addDataSlots(data);
    }

    public int getProgress() { return data.get(0); }
    public int getTotalTime() { return data.get(1); }
    public int getFigureDamage() { return data.get(2); }
    public int getFigureMaxDamage() { return data.get(3); }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            stack = slotStack.copy();
            if (index <= 2) {
                if (!this.moveItemStackTo(slotStack, 3, 39, true)) return ItemStack.EMPTY;
            } else {
                if (slotStack.getItem() instanceof FigureItem) {
                    if (!this.moveItemStackTo(slotStack, 0, 1, false)) return ItemStack.EMPTY;
                } else if (!this.moveItemStackTo(slotStack, 1, 2, false)) {
                    if (!this.moveItemStackTo(slotStack, 0, 1, false)) return ItemStack.EMPTY;
                }
            }
            if (slotStack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
            else slot.setChanged();
        }
        return stack;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
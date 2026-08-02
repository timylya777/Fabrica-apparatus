package com.fabrica.gui;

import com.fabrica.conduit.impl.PipeBlockEntity;
import com.fabrica.conduit.item.ItemNetworkNode;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * The connection settings menu of an item pipe: four filter slots and a
 * white/black list toggle. The configured items are the ones the pipe can
 * extract from ("chip") and insert into the connected inventory.
 */
public class ItemPipeSettingsMenu extends AbstractContainerMenu {
    private static final int DATA_COUNT = 1;

    @Nullable
    private final PipeBlockEntity pipeEntity;
    @Nullable
    private final ItemNetworkNode itemNode;
    @Nullable
    private final Direction direction;
    private final ContainerData data;

    public ItemPipeSettingsMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, null, null, null);
    }

    public ItemPipeSettingsMenu(int containerId, Inventory inventory, @Nullable PipeBlockEntity pipeEntity,
                                @Nullable ItemNetworkNode itemNode, @Nullable Direction direction) {
        super(ModMenus.ITEM_PIPE_SETTINGS, containerId);
        this.pipeEntity = pipeEntity;
        this.itemNode = itemNode;
        this.direction = direction;
        this.data = new SimpleContainerData(DATA_COUNT);
        this.data.set(0, itemNode != null && itemNode.isWhitelist(direction) ? 1 : 0);

        SimpleContainer filterSlots = itemNode != null
                ? new ConnectionFilterContainer(pipeEntity, itemNode, direction)
                : new SimpleContainer(4);
        for (int i = 0; i < 4; i++) {
            addSlot(new Slot(filterSlots, i, 62 + i * 18, 35));
        }
        addStandardInventorySlots(inventory, 8, 84);
        addDataSlots(data);
    }

    public static void open(ServerPlayer player, PipeBlockEntity pipeEntity, ItemNetworkNode itemNode, Direction direction) {
        player.openMenu(new net.minecraft.world.SimpleMenuProvider(
                (containerId, inventory, ignored) -> new ItemPipeSettingsMenu(containerId, inventory, pipeEntity, itemNode, direction),
                net.minecraft.network.chat.Component.translatable("container.fabrica_apparatus.item_pipe_settings")));
    }

    public boolean isWhitelist() {
        return data.get(0) == 1;
    }

    public void setWhitelist(boolean whitelist) {
        if (itemNode != null) {
            if (itemNode.setWhitelist(direction, whitelist)) {
                data.set(0, whitelist ? 1 : 0);
                if (pipeEntity != null) {
                    pipeEntity.setChanged();
                }
            }
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            stack = slotStack.copy();
            if (index < 4) {
                // filter slot -> player inventory
                if (!this.moveItemStackTo(slotStack, 4, 40, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // player inventory -> filter slots
                if (!this.moveItemStackTo(slotStack, 0, 4, false)) {
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
        return pipeEntity == null || pipeEntity.getLevel().getBlockEntity(pipeEntity.getBlockPos()) == pipeEntity
                && pipeEntity.getBlockPos().distToCenterSqr(player.getX(), player.getY(), player.getZ()) < 64.0;
    }

    /**
     * The four filter slots, directly backed by the connection of the pipe.
     */
    private static class ConnectionFilterContainer extends SimpleContainer {
        @Nullable
        private final PipeBlockEntity pipeEntity;
        private final ItemNetworkNode itemNode;
        private final Direction direction;

        ConnectionFilterContainer(@Nullable PipeBlockEntity pipeEntity, ItemNetworkNode itemNode, Direction direction) {
            super(4);
            this.pipeEntity = pipeEntity;
            this.itemNode = itemNode;
            this.direction = direction;
            for (int i = 0; i < 4; i++) {
                setItem(i, itemNode.getFilterStack(direction, i).toStack());
            }
        }

        @Override
        public void setItem(int index, ItemStack stack) {
            super.setItem(index, stack);
            itemNode.setFilterStack(direction, index, ItemVariant.of(stack));
            if (pipeEntity != null) {
                pipeEntity.setChanged();
            }
        }
    }
}

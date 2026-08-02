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
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

// Меню настроек соединения предметной трубы: 4 ghost-фильтра и переключатель
// «белый/чёрный список». Фильтры определяют, что труба извлекает и вставляет.
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
    // data[0]: 1 = белый список, 0 = чёрный список.
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
        // Слоты 0..3 — ghost-фильтры, слоты 4..39 — инвентарь игрока.
        for (int i = 0; i < 4; i++) {
            addSlot(new Slot(filterSlots, i, 62 + i * 18, 35));
        }
        addStandardInventorySlots(inventory, 8, 84);
        addDataSlots(data);
    }

    // Открывает меню настроек соединения у серверного игрока.
    public static void open(ServerPlayer player, PipeBlockEntity pipeEntity, ItemNetworkNode itemNode, Direction direction) {
        player.openMenu(new net.minecraft.world.SimpleMenuProvider(
                (containerId, inventory, ignored) -> new ItemPipeSettingsMenu(containerId, inventory, pipeEntity, itemNode, direction),
                net.minecraft.network.chat.Component.translatable("container.fabrica_apparatus.item_pipe_settings")));
    }

    public boolean isWhitelist() {
        return data.get(0) == 1;
    }

    // Меняет режим фильтра на сервере; при успехе обновляет данные и помечает трубу изменённой.
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

    // Shift+клик из инвентаря: предмет копируется в первый фильтр-слот (без расхода).
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        // Shift+click from the player inventory writes a ghost item into the
        // first filter slot, without consuming it.
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            this.slots.get(0).set(slot.getItem().copyWithCount(1));
            broadcastChanges();
        }
        return ItemStack.EMPTY;
    }

    // Ghost-слоты: клик с предметом записывает его копию (1 шт.), пустой рукой
    // или shift+клик — очищает фильтр; предмет из курсора при этом не тратится.
    @Override
    public void clicked(int slotId, int button, ContainerInput action, Player player) {
        // The filter slots are "ghost" slots: clicking with an item writes it
        // into the filter without consuming it, clicking with an empty hand (or
        // shift+clicking) clears the filter.
        if (slotId >= 0 && slotId < 4) {
            if (action == ContainerInput.PICKUP) {
                Slot filterSlot = this.slots.get(slotId);
                ItemStack carried = getCarried();
                filterSlot.set(carried.isEmpty() ? ItemStack.EMPTY : carried.copyWithCount(1));
                broadcastChanges();
            } else if (action == ContainerInput.QUICK_MOVE) {
                if (this.slots.get(slotId).hasItem()) {
                    this.slots.get(slotId).set(ItemStack.EMPTY);
                    broadcastChanges();
                }
            }
            return;
        }
        super.clicked(slotId, button, action, player);
    }

    @Override
    public boolean stillValid(Player player) {
        return pipeEntity == null || pipeEntity.getLevel().getBlockEntity(pipeEntity.getBlockPos()) == pipeEntity
                && pipeEntity.getBlockPos().distToCenterSqr(player.getX(), player.getY(), player.getZ()) < 64.0;
    }

    // Контейнер фильтров: любое изменение сразу записывается в соединение трубы.
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

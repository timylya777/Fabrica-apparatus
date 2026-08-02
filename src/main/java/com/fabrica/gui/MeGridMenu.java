package com.fabrica.gui;

import com.fabrica.block.ModBlocks;
import com.fabrica.block.me.MeGridBlockEntity;
import com.fabrica.me.MeItemStack;
import com.fabrica.me.MePackets;
import com.fabrica.me.MeStorage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MeGridMenu extends AbstractContainerMenu {
    @Nullable
    private final MeGridBlockEntity blockEntity;
    @Nullable
    private final ServerPlayer owner;

    // Кэш содержимого и статистики хранилища для отображения на клиенте.
    private List<MeItemStack> entries = List.of();
    private long used;
    private long capacity;
    // Последняя отправленная версия списка — чтобы не слать одинаковые пакеты.
    private List<MeItemStack> lastSent = List.of();

    public MeGridMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, null);
    }

    public MeGridMenu(int containerId, Inventory inventory, @Nullable MeGridBlockEntity blockEntity) {
        super(ModMenus.ME_GRID, containerId);
        this.blockEntity = blockEntity;
        this.owner = inventory.player instanceof ServerPlayer serverPlayer ? serverPlayer : null;
        // Только инвентарь игрока: содержимое ME-сети рисуется клиентом отдельно.
        addStandardInventorySlots(inventory, 8, 166);
    }

    @Nullable
    public MeGridBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public List<MeItemStack> getEntries() {
        return entries;
    }

    public long getUsed() {
        return used;
    }

    public long getCapacity() {
        return capacity;
    }

    // Применяет синхронизированное содержимое сети, пришедшее с сервера.
    public void applySync(CompoundTag tag, long used, long capacity) {
        this.entries = MePackets.entriesFromTag(tag);
        this.used = used;
        this.capacity = capacity;
    }

    // Извлекает предметы из ME-сети по запросу клиента и кладёт их в инвентарь игрока.
    public long takeFromGrid(Player player, String query, int index, int count) {
        if (blockEntity == null || index < 0 || count <= 0) {
            return 0;
        }
        MeStorage storage = blockEntity.getMeStorage();
        List<MeItemStack> filtered = MePackets.filterEntries(storage.getEntries(), query);
        if (index >= filtered.size()) {
            return 0;
        }
        MeItemStack entry = filtered.get(index);
        long toTake = Math.min(count, Math.min(entry.count(), 64));
        long taken = storage.extract(entry.item(), toTake);
        if (taken > 0) {
            ItemStack stack = new ItemStack(entry.item(), (int) taken);
            player.getInventory().placeItemBackInInventory(stack);
            broadcastChanges();
        }
        return taken;
    }

    // Вставляет предмет из курсора игрока в ME-сеть.
    public long insertCarried(Player player, int count) {
        if (blockEntity == null) {
            return 0;
        }
        ItemStack carried = getCarried();
        if (carried.isEmpty() || count <= 0) {
            return 0;
        }
        long toInsert = Math.min(count, carried.getCount());
        if (toInsert <= 0) {
            return 0;
        }
        long inserted = blockEntity.getMeStorage().insert(carried.getItem(), toInsert);
        if (inserted > 0) {
            carried.shrink((int) inserted);
        }
        return inserted;
    }

    // Отправляет клиенту содержимое сети, если оно изменилось с прошлого раза.
    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (blockEntity != null && owner != null) {
            List<MeItemStack> current = blockEntity.getMeStorage().getEntries();
            if (!current.equals(lastSent)) {
                lastSent = current;
                MePackets.sendSync(owner, this);
            }
        }
    }

    // Shift+клик: стак из слота инвентаря сразу помещается в ME-сеть.
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (blockEntity == null || index < 0 || index >= slots.size()) {
            return ItemStack.EMPTY;
        }
        Slot slot = slots.get(index);
        ItemStack stack = slot.getItem();
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        long inserted = blockEntity.getMeStorage().insert(stack.getItem(), stack.getCount());
        if (inserted > 0) {
            stack.shrink((int) inserted);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return blockEntity == null
            || AbstractContainerMenu.stillValid(
                ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()),
                player,
                ModBlocks.ME_GRID
            );
    }
}

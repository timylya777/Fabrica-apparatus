package com.fabrica.me;

import com.fabrica.block.me.MeGridBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Storage для Fabric transfer API, представляющий ME-сеть для внешних систем.
 * Привязывается к блоку ME-сетки (MeGridBlockEntity): вставка/извлечение
 * идут напрямую в хранилище сети блока, обходя его без транзакций.
 */
public class MeNetworkItemStorage implements Storage<ItemVariant> {
    /** Блок ME-сетки, к сети которого привязано это хранилище. */
    private final MeGridBlockEntity blockEntity;

    public MeNetworkItemStorage(MeGridBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    /** Вставить предметы в ME-сеть блока сетки. */
    @Override
    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        if (resource.isBlank() || maxAmount <= 0) {
            return 0;
        }
        ItemStack stack = resource.toStack(1);
        return blockEntity.getMeStorage().insert(stack.getItem(), maxAmount);
    }

    /** Извлечь предметы из ME-сети блока сетки. */
    @Override
    public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        if (resource.isBlank() || maxAmount <= 0) {
            return 0;
        }
        ItemStack stack = resource.toStack(1);
        return blockEntity.getMeStorage().extract(stack.getItem(), maxAmount);
    }

    /** Записи сети как StorageView (одна запись на тип предмета). */
    @Override
    public Iterator<StorageView<ItemVariant>> iterator() {
        List<StorageView<ItemVariant>> views = new ArrayList<>();
        for (MeItemStack entry : blockEntity.getMeStorage().getEntries()) {
            views.add(new EntryView(entry));
        }
        return views.iterator();
    }

    /** Представление одной записи сети {предмет, количество} для итерации. */
    private static class EntryView implements StorageView<ItemVariant> {
        private final MeItemStack entry;

        EntryView(MeItemStack entry) {
            this.entry = entry;
        }

        @Override
        public ItemVariant getResource() {
            return ItemVariant.of(entry.item());
        }

        @Override
        public long getAmount() {
            return entry.count();
        }

        @Override
        public long getCapacity() {
            return entry.count();
        }

        @Override
        public boolean isResourceBlank() {
            return false;
        }

        /** Извлечение через StorageView не поддерживается — пустое хранилище сети. */
        @Override
        public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            return 0;
        }
    }
}

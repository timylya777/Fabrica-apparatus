package com.fabrica.me;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext.Result;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MeDriveContainerStorage implements Storage<ItemVariant> {
    private final SimpleContainer container;

    public MeDriveContainerStorage(SimpleContainer container) {
        this.container = container;
    }

    @Override
    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        if (resource.isBlank() || maxAmount <= 0) {
            return 0;
        }
        ItemStack template = resource.toStack(1);
        int maxStack = template.getItem().getDefaultMaxStackSize();
        List<ItemStack> snapshot = capture();
        long inserted = 0;
        for (int i = 0; i < container.getContainerSize() && inserted < maxAmount; i++) {
            ItemStack existing = container.getItem(i);
            if (existing.isEmpty()) {
                int count = (int) Math.min(maxAmount - inserted, maxStack);
                container.setItem(i, template.copyWithCount(count));
                inserted += count;
            } else if (ItemStack.isSameItemSameComponents(existing, template) && existing.getCount() < maxStack) {
                int count = (int) Math.min(maxAmount - inserted, maxStack - existing.getCount());
                existing.grow(count);
                container.setItem(i, existing);
                inserted += count;
            }
        }
        if (inserted > 0 && transaction != null) {
            rollbackOnAbort(transaction, snapshot);
        }
        return inserted;
    }

    @Override
    public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        if (resource.isBlank() || maxAmount <= 0) {
            return 0;
        }
        ItemStack template = resource.toStack(1);
        List<ItemStack> snapshot = capture();
        long extracted = 0;
        for (int i = 0; i < container.getContainerSize() && extracted < maxAmount; i++) {
            ItemStack existing = container.getItem(i);
            if (existing.isEmpty() || !ItemStack.isSameItemSameComponents(existing, template)) {
                continue;
            }
            int count = (int) Math.min(maxAmount - extracted, existing.getCount());
            existing.shrink(count);
            container.setItem(i, existing);
            extracted += count;
        }
        if (extracted > 0 && transaction != null) {
            rollbackOnAbort(transaction, snapshot);
        }
        return extracted;
    }

    @Override
    public Iterator<StorageView<ItemVariant>> iterator() {
        List<StorageView<ItemVariant>> views = new ArrayList<>();
        for (int i = 0; i < container.getContainerSize(); i++) {
            if (!container.getItem(i).isEmpty()) {
                views.add(new SlotView(i));
            }
        }
        return views.iterator();
    }

    private List<ItemStack> capture() {
        List<ItemStack> snapshot = new ArrayList<>(container.getContainerSize());
        for (int i = 0; i < container.getContainerSize(); i++) {
            snapshot.add(container.getItem(i).copy());
        }
        return snapshot;
    }

    private void rollbackOnAbort(TransactionContext transaction, List<ItemStack> snapshot) {
        transaction.addCloseCallback((tx, result) -> {
            if (result == Result.ABORTED) {
                for (int i = 0; i < container.getContainerSize(); i++) {
                    container.setItem(i, snapshot.get(i));
                }
            }
        });
    }

    private class SlotView implements StorageView<ItemVariant> {
        private final int slot;

        SlotView(int slot) {
            this.slot = slot;
        }

        @Override
        public ItemVariant getResource() {
            return ItemVariant.of(container.getItem(slot));
        }

        @Override
        public long getAmount() {
            return container.getItem(slot).getCount();
        }

        @Override
        public long getCapacity() {
            return container.getItem(slot).getItem().getDefaultMaxStackSize();
        }

        @Override
        public boolean isResourceBlank() {
            return container.getItem(slot).isEmpty();
        }

        @Override
        public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            return MeDriveContainerStorage.this.extract(resource, maxAmount, transaction);
        }
    }
}

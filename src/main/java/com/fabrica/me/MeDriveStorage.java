package com.fabrica.me;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MeDriveStorage implements MeStorage {
    private final SimpleContainer disks;

    public MeDriveStorage(SimpleContainer disks) {
        this.disks = disks;
    }

    @Override
    public long insert(Item item, long count) {
        long remaining = count;
        for (int i = 0; i < disks.getContainerSize() && remaining > 0; i++) {
            remaining -= new MeDiskStorage(disks, i).insert(item, remaining);
        }
        return count - remaining;
    }

    @Override
    public long extract(Item item, long count) {
        long remaining = count;
        for (int i = 0; i < disks.getContainerSize() && remaining > 0; i++) {
            remaining -= new MeDiskStorage(disks, i).extract(item, remaining);
        }
        return count - remaining;
    }

    @Override
    public long countOf(Item item) {
        long total = 0;
        for (int i = 0; i < disks.getContainerSize(); i++) {
            total += new MeDiskStorage(disks, i).countOf(item);
        }
        return total;
    }

    @Override
    public long getItemCount() {
        long total = 0;
        for (int i = 0; i < disks.getContainerSize(); i++) {
            total += new MeDiskStorage(disks, i).getItemCount();
        }
        return total;
    }

    @Override
    public long getCapacity() {
        long total = 0;
        for (int i = 0; i < disks.getContainerSize(); i++) {
            total += new MeDiskStorage(disks, i).getCapacity();
        }
        return total;
    }

    @Override
    public List<MeItemStack> getEntries() {
        Map<Item, Long> merged = new LinkedHashMap<>();
        for (int i = 0; i < disks.getContainerSize(); i++) {
            for (MeItemStack entry : new MeDiskStorage(disks, i).getEntries()) {
                merged.merge(entry.item(), entry.count(), Long::sum);
            }
        }
        List<MeItemStack> entries = new java.util.ArrayList<>();
        merged.forEach((item, count) -> entries.add(new MeItemStack(item, count)));
        return MeNetworkStorage.sortEntries(entries);
    }
}

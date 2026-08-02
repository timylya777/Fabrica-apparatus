package com.fabrica.me;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MeNetworkStorage implements MeStorage {
    private final List<MeStorage> storages;

    public MeNetworkStorage(List<MeStorage> storages) {
        this.storages = List.copyOf(storages);
    }

    public static MeStorage empty() {
        return new MeNetworkStorage(List.of());
    }

    @Override
    public long insert(Item item, long count) {
        long remaining = count;
        for (MeStorage storage : storages) {
            if (remaining <= 0) {
                break;
            }
            remaining -= storage.insert(item, remaining);
        }
        return count - remaining;
    }

    @Override
    public long extract(Item item, long count) {
        long remaining = count;
        for (MeStorage storage : storages) {
            if (remaining <= 0) {
                break;
            }
            remaining -= storage.extract(item, remaining);
        }
        return count - remaining;
    }

    @Override
    public long countOf(Item item) {
        long total = 0;
        for (MeStorage storage : storages) {
            total += storage.countOf(item);
        }
        return total;
    }

    @Override
    public long getItemCount() {
        long total = 0;
        for (MeStorage storage : storages) {
            total += storage.getItemCount();
        }
        return total;
    }

    @Override
    public long getCapacity() {
        long total = 0;
        for (MeStorage storage : storages) {
            total += storage.getCapacity();
        }
        return total;
    }

    @Override
    public List<MeItemStack> getEntries() {
        Map<Item, Long> merged = new LinkedHashMap<>();
        for (MeStorage storage : storages) {
            for (MeItemStack entry : storage.getEntries()) {
                merged.merge(entry.item(), entry.count(), Long::sum);
            }
        }
        List<MeItemStack> entries = new ArrayList<>();
        merged.forEach((item, count) -> entries.add(new MeItemStack(item, count)));
        return sortEntries(entries);
    }

    public static List<MeItemStack> sortEntries(List<MeItemStack> entries) {
        entries.sort(Comparator.comparing(entry -> BuiltInRegistries.ITEM.getKey(entry.item()).toString()));
        return entries;
    }
}

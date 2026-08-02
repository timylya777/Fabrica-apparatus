package com.fabrica.me;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Хранилище всей ME-сети: обёртка над списком отдельных MeStorage
 * (диски в приводах), объединяющая их в единое виртуальное хранилище.
 * Операции распределяются по подключённым хранилищам по очереди,
 * getEntries сливает записи и сортирует их.
 */
public class MeNetworkStorage implements MeStorage {
    /** Все хранилища, подключённые к сети. */
    private final List<MeStorage> storages;

    public MeNetworkStorage(List<MeStorage> storages) {
        this.storages = List.copyOf(storages);
    }

    /** Пустое хранилище сети (когда узлов нет). */
    public static MeStorage empty() {
        return new MeNetworkStorage(List.of());
    }

    /** Вставить предметы, раскладывая по хранилищам сети по очереди. */
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

    /** Извлечь предметы, забирая из хранилищ сети по очереди. */
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

    /** Суммарное количество предмета во всей сети. */
    @Override
    public long countOf(Item item) {
        long total = 0;
        for (MeStorage storage : storages) {
            total += storage.countOf(item);
        }
        return total;
    }

    /** Суммарное количество всех предметов в сети. */
    @Override
    public long getItemCount() {
        long total = 0;
        for (MeStorage storage : storages) {
            total += storage.getItemCount();
        }
        return total;
    }

    /** Суммарная ёмкость всех хранилищ сети. */
    @Override
    public long getCapacity() {
        long total = 0;
        for (MeStorage storage : storages) {
            total += storage.getCapacity();
        }
        return total;
    }

    /** Записи сети: слив по предметам из всех хранилищ + сортировка. */
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

    /** Сортировка записей по строковому registry id предмета (для стабильного порядка в GUI). */
    public static List<MeItemStack> sortEntries(List<MeItemStack> entries) {
        entries.sort(Comparator.comparing(entry -> BuiltInRegistries.ITEM.getKey(entry.item()).toString()));
        return entries;
    }
}

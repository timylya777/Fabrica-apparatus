package com.fabrica.me;

import net.minecraft.world.item.Item;

import java.util.List;

/**
 * Интерфейс единого хранилища предметов ME-сети.
 * Абстрагирует работу с предметами по их типу (Item) и количеству,
 * без учёта стаков. Реализации: MeDiskStorage (один диск),
 * MeDriveStorage (все диски в приводе), MeNetworkStorage (объединение хранилищ сети).
 */
public interface MeStorage {
    /** Вставить предметы, вернуть фактически вставленное количество. */
    long insert(Item item, long count);

    /** Извлечь предметы, вернуть фактически извлечённое количество. */
    long extract(Item item, long count);

    /** Сколько всего штук данного предмета хранится. */
    long countOf(Item item);

    /** Суммарное количество всех предметов в хранилище. */
    long getItemCount();

    /** Максимальная ёмкость хранилища (в штуках предметов). */
    long getCapacity();

    /** Список записей хранилища (предмет + количество). */
    List<MeItemStack> getEntries();
}

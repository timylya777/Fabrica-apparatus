package com.fabrica.me;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Хранилище одного ME-диска: реализация MeStorage для конкретного слота
 * контейнера привода (disks). Все операции вставки/извлечения работают
 * напрямую с NBT-данными стака диска, изменяя список записей "Items".
 */
public class MeDiskStorage implements MeStorage {
    /** Контейнер приводов (слоты с дисками). */
    private final SimpleContainer disks;
    /** Номер слота, в котором лежит целевой диск. */
    private final int slot;

    public MeDiskStorage(SimpleContainer disks, int slot) {
        this.disks = disks;
        this.slot = slot;
    }

    /** Стак диска в слоте привода. */
    public ItemStack getDiskStack() {
        return disks.getItem(slot);
    }

    /** Есть ли в слоте ME-диск. */
    public boolean hasDisk() {
        return getDiskStack().getItem() instanceof MeStorageDiskItem;
    }

    /** Ёмкость диска в слоте (0, если диска нет). */
    @Override
    public long getCapacity() {
        if (getDiskStack().getItem() instanceof MeStorageDiskItem diskItem) {
            return diskItem.getTier().getCapacity();
        }
        return 0;
    }

    /** Записи диска из его NBT. */
    @Override
    public List<MeItemStack> getEntries() {
        return hasDisk() ? MeStorageDiskItem.readEntries(getDiskStack()) : List.of();
    }

    /** Сколько штук конкретного предмета лежит на диске. */
    @Override
    public long countOf(Item item) {
        long total = 0;
        for (MeItemStack entry : getEntries()) {
            if (entry.item() == item) {
                total += entry.count();
            }
        }
        return total;
    }

    /** Всего занятых штук на диске. */
    @Override
    public long getItemCount() {
        long total = 0;
        for (MeItemStack entry : getEntries()) {
            total += entry.count();
        }
        return total;
    }

    /** Вставить предметы на диск с учётом свободного места; результат сохраняется в NBT. */
    @Override
    public long insert(Item item, long count) {
        if (!hasDisk() || count <= 0) {
            return 0;
        }
        ListTag list = MeStorageDiskItem.getItemsTag(getDiskStack());
        long capacity = getCapacity();
        long used = MeStorageDiskItem.getUsed(getDiskStack());
        long room = capacity - used;
        if (room <= 0) {
            return 0;
        }
        long toInsert = Math.min(count, room);
        CompoundTag target = null;
        for (Tag raw : list) {
            if (raw instanceof CompoundTag entry && matches(entry, item)) {
                target = entry;
                break;
            }
        }
        if (target != null) {
            // Запись уже есть — просто увеличиваем счётчик.
            target.put("count", LongTag.valueOf(target.getLongOr("count", 0) + toInsert));
        } else {
            // Новый предмет — добавляем запись {id, count}.
            CompoundTag entry = new CompoundTag();
            entry.put("id", StringTag.valueOf(BuiltInRegistries.ITEM.getKey(item).toString()));
            entry.put("count", LongTag.valueOf(toInsert));
            list.add(list.size(), entry);
        }
        save(list);
        return toInsert;
    }

    /** Извлечь предметы с диска; пустые записи удаляются из списка. */
    @Override
    public long extract(Item item, long count) {
        if (!hasDisk() || count <= 0) {
            return 0;
        }
        ListTag list = MeStorageDiskItem.getItemsTag(getDiskStack());
        CompoundTag target = null;
        for (Tag raw : list) {
            if (raw instanceof CompoundTag entry && matches(entry, item)) {
                target = entry;
                break;
            }
        }
        if (target == null) {
            return 0;
        }
        long stored = target.getLongOr("count", 0);
        long toExtract = Math.min(count, stored);
        long remaining = stored - toExtract;
        if (remaining <= 0) {
            list.remove(target);
        } else {
            target.put("count", LongTag.valueOf(remaining));
        }
        save(list);
        return toExtract;
    }

    /** Совпадает ли NBT-запись с указанным предметом (сравнение по registry id). */
    private static boolean matches(CompoundTag entry, Item item) {
        String id = entry.getStringOr("id", "");
        if (id.isEmpty()) {
            return false;
        }
        net.minecraft.resources.Identifier identifier = net.minecraft.resources.Identifier.tryParse(id);
        return identifier != null && BuiltInRegistries.ITEM.getValue(identifier) == item;
    }

    /** Записать изменённый список обратно в NBT стака и вернуть диск в слот. */
    private void save(ListTag list) {
        MeStorageDiskItem.writeEntries(getDiskStack(), list);
        disks.setItem(slot, getDiskStack());
    }
}

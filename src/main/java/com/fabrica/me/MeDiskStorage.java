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

public class MeDiskStorage implements MeStorage {
    private final SimpleContainer disks;
    private final int slot;

    public MeDiskStorage(SimpleContainer disks, int slot) {
        this.disks = disks;
        this.slot = slot;
    }

    public ItemStack getDiskStack() {
        return disks.getItem(slot);
    }

    public boolean hasDisk() {
        return getDiskStack().getItem() instanceof MeStorageDiskItem;
    }

    @Override
    public long getCapacity() {
        if (getDiskStack().getItem() instanceof MeStorageDiskItem diskItem) {
            return diskItem.getTier().getCapacity();
        }
        return 0;
    }

    @Override
    public List<MeItemStack> getEntries() {
        return hasDisk() ? MeStorageDiskItem.readEntries(getDiskStack()) : List.of();
    }

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

    @Override
    public long getItemCount() {
        long total = 0;
        for (MeItemStack entry : getEntries()) {
            total += entry.count();
        }
        return total;
    }

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
            target.put("count", LongTag.valueOf(target.getLongOr("count", 0) + toInsert));
        } else {
            CompoundTag entry = new CompoundTag();
            entry.put("id", StringTag.valueOf(BuiltInRegistries.ITEM.getKey(item).toString()));
            entry.put("count", LongTag.valueOf(toInsert));
            list.add(list.size(), entry);
        }
        save(list);
        return toInsert;
    }

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

    private static boolean matches(CompoundTag entry, Item item) {
        String id = entry.getStringOr("id", "");
        if (id.isEmpty()) {
            return false;
        }
        net.minecraft.resources.Identifier identifier = net.minecraft.resources.Identifier.tryParse(id);
        return identifier != null && BuiltInRegistries.ITEM.getValue(identifier) == item;
    }

    private void save(ListTag list) {
        MeStorageDiskItem.writeEntries(getDiskStack(), list);
        disks.setItem(slot, getDiskStack());
    }
}

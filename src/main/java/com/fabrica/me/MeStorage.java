package com.fabrica.me;

import net.minecraft.world.item.Item;

import java.util.List;

public interface MeStorage {
    long insert(Item item, long count);

    long extract(Item item, long count);

    long countOf(Item item);

    long getItemCount();

    long getCapacity();

    List<MeItemStack> getEntries();
}

package com.fabrica.me;

import net.minecraft.world.item.Item;

public record MeItemStack(Item item, long count) {
    public MeItemStack {
        if (count < 0) {
            throw new IllegalArgumentException("Negative count: " + count);
        }
    }
}

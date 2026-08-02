package com.fabrica.me;

import net.minecraft.world.item.Item;

/**
 * Неизменяемая запись ME-хранилища: тип предмета и его количество в сети.
 * Отличается от обычного ItemStack — количество хранится в long
 * и не привязано к maxStackSize.
 */
public record MeItemStack(Item item, long count) {
    /** Компактный конструктор: защита от отрицательного количества. */
    public MeItemStack {
        if (count < 0) {
            throw new IllegalArgumentException("Negative count: " + count);
        }
    }
}

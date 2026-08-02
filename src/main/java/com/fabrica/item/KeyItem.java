package com.fabrica.item;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

// Ключ-конфигуратор труб: обычный клик по коннектору циклически меняет режим
// ввода/вывода, shift+клик подключает/отключает соединение с машиной.
/**
 * The key used to configure pipes: click a pipe part to cycle the import/export
 * mode, shift+click to add or remove a machine connection. The actual handling
 * happens in PipeBlock, where the exact hit position is available.
 */
public class KeyItem extends Item {
	public KeyItem(Properties settings) {
		super(settings);
	}

	// Сама логика обрабатывается в PipeBlock, где доступно точное попадание
	// по части трубы; здесь предмет лишь пропускает клик дальше.
	@Override
	public InteractionResult useOn(UseOnContext context) {
		return InteractionResult.PASS;
	}
}

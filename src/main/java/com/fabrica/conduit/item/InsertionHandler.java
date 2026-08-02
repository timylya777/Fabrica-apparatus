package com.fabrica.conduit.item;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

@FunctionalInterface
// Обработчик вставки ресурса в приёмник: возвращает фактически вставленное
// количество (может быть меньше amount, если приёмник переполнен).
interface InsertionHandler<T> {
	long insert(T resource, long amount, TransactionContext transaction);
}

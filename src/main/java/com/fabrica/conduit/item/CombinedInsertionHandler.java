package com.fabrica.conduit.item;

import java.util.List;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

// Составной приёмник: перебирает обработчики по порядку, передавая каждому
// остаток (amount - inserted), пока не будет вставлено нужное количество.
class CombinedInsertionHandler<T> implements InsertionHandler<T> {
	private final List<? extends InsertionHandler<T>> handlers;

	CombinedInsertionHandler(List<? extends InsertionHandler<T>> handlers) {
		this.handlers = handlers;
	}

	@Override
	public long insert(T resource, long amount, TransactionContext transaction) {
		long inserted = 0;
		for (var handler : handlers) {
			inserted += handler.insert(resource, amount - inserted, transaction);
			if (inserted >= amount) break;
		}
		return inserted;
	}
}

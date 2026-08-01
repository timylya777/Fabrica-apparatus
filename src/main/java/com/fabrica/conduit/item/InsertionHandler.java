package com.fabrica.conduit.item;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

@FunctionalInterface
interface InsertionHandler<T> {
	long insert(T resource, long amount, TransactionContext transaction);
}

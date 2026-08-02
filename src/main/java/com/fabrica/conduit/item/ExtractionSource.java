package com.fabrica.conduit.item;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

// Источник извлечения на текущий перенос: соединение (с приоритетом и
// фильтром), хранилище, из которого берём предметы, и позиция/сторона блока.
record ExtractionSource(
		ItemNetworkNode.ItemConnection connection,
		Storage<ItemVariant> itemHandler,
		BlockPos queryPos,
		Direction querySide) {}

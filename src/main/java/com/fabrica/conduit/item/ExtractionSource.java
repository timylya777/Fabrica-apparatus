package com.fabrica.conduit.item;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

record ExtractionSource(
		ItemNetworkNode.ItemConnection connection,
		Storage<ItemVariant> itemHandler,
		BlockPos queryPos,
		Direction querySide) {}

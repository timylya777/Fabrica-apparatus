package com.fabrica.conduit.item;

import com.fabrica.conduit.api.PipeNetworkData;
import com.mojang.serialization.MapCodec;

// Отдельных данных у предметной сети нет: любые две трубы одного типа всегда
// соединяются, поэтому рекорд пустой, а кодек всегда отдаёт INSTANCE.
// There is no data for item networks, two pipes of the same type can always connect.
public record ItemNetworkData() implements PipeNetworkData {
	public static final ItemNetworkData INSTANCE = new ItemNetworkData();
	public static final MapCodec<ItemNetworkData> CODEC = MapCodec.unit(INSTANCE);

	@Override
	public ItemNetworkData clone() {
		return new ItemNetworkData();
	}
}

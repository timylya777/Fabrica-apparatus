package com.fabrica.conduit.electricity;

import com.fabrica.conduit.api.PipeNetworkData;
import com.mojang.serialization.MapCodec;

/**
 * Данные электрической сети. Пустой record: у электрических труб НЕТ
 * собственных данных (в отличие от жидкостных, где хранится тип жидкости),
 * поэтому две трубы одного тира всегда могут соединиться друг с другом.
 * INSTANCE — единственный общий экземпляр, CODEC — сериализатор,
 * который не пишет никаких полей (MapCodec.unit).
 */
// There is no data for electricity pipes, two pipes of the same type can always connect
public record ElectricityNetworkData() implements PipeNetworkData {
	public static final ElectricityNetworkData INSTANCE = new ElectricityNetworkData();
	public static final MapCodec<ElectricityNetworkData> CODEC = MapCodec.unit(INSTANCE);

	// Возвращает новую копию данных (необходимо для создания независимых сетей).
	@Override
	public ElectricityNetworkData clone() {
		return new ElectricityNetworkData();
	}
}

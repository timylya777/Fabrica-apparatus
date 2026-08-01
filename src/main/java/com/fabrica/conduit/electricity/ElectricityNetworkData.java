package com.fabrica.conduit.electricity;

import com.fabrica.conduit.api.PipeNetworkData;
import com.mojang.serialization.MapCodec;

// There is no data for electricity pipes, two pipes of the same type can always connect
public record ElectricityNetworkData() implements PipeNetworkData {
	public static final ElectricityNetworkData INSTANCE = new ElectricityNetworkData();
	public static final MapCodec<ElectricityNetworkData> CODEC = MapCodec.unit(INSTANCE);

	@Override
	public ElectricityNetworkData clone() {
		return new ElectricityNetworkData();
	}
}

package com.fabrica.conduit.fluid;

import com.fabrica.conduit.api.PipeNetworkData;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;

public record FluidNetworkData(FluidVariant fluid) implements PipeNetworkData {
	public static final FluidNetworkData INSTANCE = new FluidNetworkData(FluidVariant.blank());
	public static final MapCodec<FluidNetworkData> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			FluidVariant.CODEC.fieldOf("fluid").forGetter(FluidNetworkData::fluid)
	).apply(i, FluidNetworkData::new));

	@Override
	public FluidNetworkData clone() {
		return new FluidNetworkData(fluid);
	}
}

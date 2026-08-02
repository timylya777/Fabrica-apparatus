package com.fabrica.conduit.fluid;

import com.fabrica.conduit.api.PipeNetworkData;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;

/**
 * Данные жидкостной сети: хранит тип жидкости (FluidVariant), текущей в сети.
 * Отвечает за:
 * - знание "какая жидкость течёт по трубам" — сериализуется и используется
 *   для отображения в GUI и синхронизации с клиентом;
 * - INSTANCE — данные по умолчанию (пустая/blank жидкость) для новой трубы;
 * - CODEC — сериализация поля "fluid" (и JSON, и NBT-совместимая).
 * Пока жидкость не выбрана (blank), сеть не передаёт ничего.
 */
public record FluidNetworkData(FluidVariant fluid) implements PipeNetworkData {
	public static final FluidNetworkData INSTANCE = new FluidNetworkData(FluidVariant.blank());
	public static final MapCodec<FluidNetworkData> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			FluidVariant.CODEC.fieldOf("fluid").forGetter(FluidNetworkData::fluid)
	).apply(i, FluidNetworkData::new));

	// Возвращает копию данных для создания независимой новой сети.
	@Override
	public FluidNetworkData clone() {
		return new FluidNetworkData(fluid);
	}
}

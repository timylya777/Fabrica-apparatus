package com.fabrica.client.render.pipe;

import com.fabrica.FabricaMod;
import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.client.model.loading.v1.CustomUnbakedBlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.resources.Identifier;

// Невыпеченная модель трубы: подставляется из blockstate JSON по ключу
// "fabric:type", при выпечке отдаёт общую (shared) модель.
/**
 * The custom unbaked model of the pipe block, dispatched from the blockstate
 * via the "fabric:type" key.
 */
public class PipeUnbakedModel implements CustomUnbakedBlockStateModel {

	// ID модели в blockstate, синглтон и codec для её создания (MapCodec.unit —
	// модель не имеет параметров).
	public static final Identifier TYPE_ID = FabricaMod.id("pipe");
	public static final PipeUnbakedModel INSTANCE = new PipeUnbakedModel();
	public static final MapCodec<PipeUnbakedModel> CODEC = MapCodec.unit(INSTANCE);

	@Override
	public MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
		return CODEC;
	}

	// Выпечка идёт через SharedBlockModel: рендереры и кеши мешей
	// создаются один раз, а не при каждой выпечке блокстейт-модели.
	@Override
	public BlockStateModel bake(ModelBaker modelBaker) {
		return modelBaker.compute(SharedBlockModel.INSTANCE);
	}

	@Override
	public void resolveDependencies(ResolvableModel.Resolver resolver) {
	}
}

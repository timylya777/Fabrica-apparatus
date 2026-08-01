package com.fabrica.client.render.pipe;

import com.fabrica.FabricaMod;
import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.client.model.loading.v1.CustomUnbakedBlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.resources.Identifier;

/**
 * The custom unbaked model of the pipe block, dispatched from the blockstate
 * via the "fabric:type" key.
 */
public class PipeUnbakedModel implements CustomUnbakedBlockStateModel {

	public static final Identifier TYPE_ID = FabricaMod.id("pipe");
	public static final PipeUnbakedModel INSTANCE = new PipeUnbakedModel();
	public static final MapCodec<PipeUnbakedModel> CODEC = MapCodec.unit(INSTANCE);

	@Override
	public MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
		return CODEC;
	}

	@Override
	public BlockStateModel bake(ModelBaker modelBaker) {
		return modelBaker.compute(SharedBlockModel.INSTANCE);
	}

	@Override
	public void resolveDependencies(ResolvableModel.Resolver resolver) {
	}
}

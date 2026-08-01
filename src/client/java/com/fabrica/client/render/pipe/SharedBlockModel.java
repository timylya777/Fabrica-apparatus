package com.fabrica.client.render.pipe;

import com.fabrica.FabricaMod;
import com.fabrica.conduit.api.PipeNetworkType;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.sprite.Material;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * The shared baked pipe block model, so that the pipe renderers (and their mesh
 * caches) are only created once no matter how many times the blockstate model
 * is baked, like MI's SharedBlockModel.
 */
public class SharedBlockModel implements ModelBaker.SharedOperationKey<PipeBlockStateModel> {
	public static final SharedBlockModel INSTANCE = new SharedBlockModel();

	private final Map<PipeRenderer.Factory, PipeRenderer> renderers = new IdentityHashMap<>();

	private SharedBlockModel() {
	}

	@Override
	public PipeBlockStateModel compute(ModelBaker modelBaker) {
		// Bake every renderer exactly once, keyed by their factory.
		for (PipeNetworkType type : PipeNetworkType.getTypes().values()) {
			PipeRenderer.Factory factory = PipeRenderer.get(type);
			if (factory != null) {
				renderers.computeIfAbsent(factory, f -> f.create(modelBaker));
			}
		}
		Material.Baked particleMaterial = modelBaker.materials().get(new Material(FabricaMod.id("block/pipes/item")), () -> "pipe particle");
		return new PipeBlockStateModel(particleMaterial, renderers);
	}
}

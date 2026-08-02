package com.fabrica.client.render.pipe;

import com.fabrica.FabricaMod;
import com.fabrica.conduit.api.PipeNetworkType;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.sprite.Material;

import java.util.IdentityHashMap;
import java.util.Map;

// Общая выпеченная модель труб: рендереры и их кеши мешей создаются только
// один раз, сколько бы раз ни выпекалась блокстейт-модель (как в MI).
/**
 * The shared baked pipe block model, so that the pipe renderers (and their mesh
 * caches) are only created once no matter how many times the blockstate model
 * is baked, like MI's SharedBlockModel.
 */
public class SharedBlockModel implements ModelBaker.SharedOperationKey<PipeBlockStateModel> {
	public static final SharedBlockModel INSTANCE = new SharedBlockModel();

	// Рендереры по фабрикам типов труб; заполняется при первой выпечке.
	private final Map<PipeRenderer.Factory, PipeRenderer> renderers = new IdentityHashMap<>();

	private SharedBlockModel() {
	}

	// Выпекает рендереры всех типов труб ровно один раз (computeIfAbsent
	// пропускает уже созданные) и собирает итоговую блокстейт-модель.
	@Override
	public PipeBlockStateModel compute(ModelBaker modelBaker) {
		// Bake every renderer exactly once, keyed by their factory.
		for (PipeNetworkType type : PipeNetworkType.getTypes().values()) {
			PipeRenderer.Factory factory = PipeRenderer.get(type);
			if (factory != null) {
				renderers.computeIfAbsent(factory, f -> f.create(modelBaker));
			}
		}
		// Спрайт частиц (пыль при разрушении трубы) берём из блочного атласа текстур.
		Material.Baked particleMaterial = modelBaker.materials().get(new Material(FabricaMod.id("block/pipes/item")), () -> "pipe particle");
		return new PipeBlockStateModel(particleMaterial, renderers);
	}
}

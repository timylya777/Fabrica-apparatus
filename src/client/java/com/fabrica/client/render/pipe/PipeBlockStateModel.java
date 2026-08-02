package com.fabrica.client.render.pipe;

import com.fabrica.FabricaMod;
import com.fabrica.conduit.api.PipeEndpointType;
import com.fabrica.conduit.api.PipeNetworkType;
import com.fabrica.conduit.impl.PipeBlockEntity;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.model.FabricBlockStateModel;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Predicate;

// Клиентская модель блока труб: рисует все типы труб, установленные в
// PipeBlockEntity, используя кешированные меши из PipeMeshCache.
/**
 * The block model of the pipe block: renders every pipe type present in the
 * PipeBlockEntity using the meshes from the per-type PipeMeshCache.
 */
public class PipeBlockStateModel implements BlockStateModel, FabricBlockStateModel {

	// Материал частиц (при разрушении блока) и рендереры всех типов труб.
	private final Material.Baked particleMaterial;
	private final Map<PipeRenderer.Factory, PipeRenderer> renderers;

	public PipeBlockStateModel(Material.Baked particleMaterial, Map<PipeRenderer.Factory, PipeRenderer> renderers) {
		this.particleMaterial = particleMaterial;
		this.renderers = renderers;
	}

	// Генерация квадов блока: для каждого установленного типа трубы рендерер
	// рисует в emitter меши соединений на основе данных из BlockEntity.
	@Override
	public void emitQuads(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos, BlockState state,
			RandomSource random, Predicate<Direction> cullTest) {
		PipeBlockEntity be = getBlockEntity(level, pos);
		if (be == null || be.isRemoved()) return;
		SortedMap<PipeNetworkType, PipeEndpointType[]> connections = be.getRenderedConnections();
		if (connections.isEmpty()) return;

		int size = connections.size();
		PipeEndpointType[][] renderedConnections = new PipeEndpointType[size][];
		PipeNetworkType[] types = new PipeNetworkType[size];
		// Мапа отсортирована по типу трубы: переносим соединения в массивы,
		// чтобы рендерер мог обращаться к любому слоту за O(1).
		int slot = 0;
		for (Map.Entry<PipeNetworkType, PipeEndpointType[]> entry : connections.entrySet()) {
			renderedConnections[slot] = entry.getValue();
			types[slot] = entry.getKey();
			slot++;
		}
		for (slot = 0; slot < size; slot++) {
			PipeRenderer renderer = renderers.get(PipeRenderer.get(types[slot]));
			// slot — логический слот трубы в блоке: 0 — центр, 1 — низ, 2 — верх.
			if (renderer != null) {
				renderer.draw(emitter, level, pos, slot, renderedConnections, types[slot].getColor(), be.getCustomData());
			}
		}
	}

	// Ключ кеша геометрии: зависит только от соединений BlockEntity, поэтому
	// одинаковые наборы соединений переиспользуют один и тот же меш.
	@Override
	public Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
		PipeBlockEntity be = getBlockEntity(level, pos);
		if (be == null || be.isRemoved()) return null;
		// The geometry only depends on the rendered connections of the block entity.
		SortedMap<String, String> key = new TreeMap<>();
		for (Map.Entry<PipeNetworkType, PipeEndpointType[]> entry : be.getRenderedConnections().entrySet()) {
			StringBuilder builder = new StringBuilder();
			for (PipeEndpointType conn : entry.getValue()) {
				builder.append(conn == null ? ' ' : (char) ('0' + conn.getId()));
			}
			key.put(entry.getKey().getIdentifier().toString(), builder.toString());
		}
		return key;
	}

	@Override
	public Material.Baked particleMaterial(BlockAndTintGetter level, BlockPos pos, BlockState state) {
		return particleMaterial;
	}

	@Override
	public Material.Baked particleMaterial() {
		return particleMaterial;
	}

	@Override
	public int materialFlags() {
		return 0;
	}

	@Override
	public void collectParts(RandomSource random, List<BlockStateModelPart> parts) {
	}

	private static PipeBlockEntity getBlockEntity(BlockAndTintGetter level, BlockPos pos) {
		return level.getBlockEntity(pos) instanceof PipeBlockEntity be ? be : null;
	}
}

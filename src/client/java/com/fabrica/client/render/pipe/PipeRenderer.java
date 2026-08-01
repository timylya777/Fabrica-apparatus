package com.fabrica.client.render.pipe;

import com.fabrica.conduit.api.PipeEndpointType;
import com.fabrica.conduit.api.PipeNetworkType;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

/**
 * Renders the connections of a single logical pipe slot.
 * One renderer instance is created per pipe type factory.
 */
public interface PipeRenderer {
	static void register(PipeNetworkType type, PipeRenderer.Factory factory) {
		type.renderer = factory;
	}

	static PipeRenderer.Factory get(PipeNetworkType type) {
		return (Factory) type.renderer;
	}

	/**
	 * Draw the connections for a logical slot.
	 *
	 * @param logicalSlot The logical slot, so 0 for center, 1 for lower and 2 for
	 *                    upper.
	 * @param connections For every logical slot, then for every direction, the
	 *                    connection type or null for no connection.
	 * @param color       The tint color (ARGB).
	 */
	void draw(
			QuadEmitter emitter,
			@Nullable BlockAndTintGetter view, @Nullable BlockPos pos,
			int logicalSlot, PipeEndpointType[][] connections,
			int color, @Nullable Object customData);

	@FunctionalInterface
	interface Factory {
		PipeRenderer create(ModelBaker modelBaker);
	}
}

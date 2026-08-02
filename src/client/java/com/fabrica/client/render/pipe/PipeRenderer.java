package com.fabrica.client.render.pipe;

import com.fabrica.conduit.api.PipeEndpointType;
import com.fabrica.conduit.api.PipeNetworkType;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

/**
 * Интерфейс рендера соединений одного логического слота трубы.
 * Реализация получает emitter FRAPI и рисует в него геометрию соединений
 * для заданного слота. Один экземпляр рендерера создаётся на фабрику
 * типа трубы (см. {@link Factory}).
 */
/**
 * Renders the connections of a single logical pipe slot.
 * One renderer instance is created per pipe type factory.
 */
public interface PipeRenderer {
	/** Регистрирует фабрику рендерера для данного типа сети труб. */
	static void register(PipeNetworkType type, PipeRenderer.Factory factory) {
		type.renderer = factory;
	}

	/** Возвращает зарегистрированную фабрику рендерера для типа сети. */
	static PipeRenderer.Factory get(PipeNetworkType type) {
		return (Factory) type.renderer;
	}

	/**
	 * Рисует соединения логического слота трубы в emitter.
	 *
	 * @param logicalSlot Логический слот: 0 — центр, 1 — нижний, 2 — верхний.
	 * @param connections Для каждого логического слота и направления — тип
	 *                    соединения либо null, если соединения нет.
	 * @param color       Цвет тонировки (ARGB).
	 */
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

	/** Фабрика рендереров: создаёт экземпляр рендерера, используя ModelBaker. */
	@FunctionalInterface
	interface Factory {
		PipeRenderer create(ModelBaker modelBaker);
	}
}

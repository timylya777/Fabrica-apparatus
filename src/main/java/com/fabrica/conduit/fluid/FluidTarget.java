package com.fabrica.conduit.fluid;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;

/**
 * Цель передачи жидкости: одно подключение трубы к ёмкости (или другой трубе),
 * зарегистрированное узлом на время тика. Отвечает за хранение:
 * - приоритета подключения (priority) — чем больше, тем раньше цель обработается;
 * - самого хранилища (storage) — куда/откуда передаётся жидкость;
 * - флагов canExtract/canInsert — что разрешает режим подключения (OUT/IN/IN_OUT);
 * - simulationResult — временный результат симуляции, по которому цель
 *   сортируется внутри ведра одинакового приоритета (для честного деления).
 */
/**
 * A target to be used during a transfer operation.
 */
class FluidTarget {
	// Приоритет передачи: сначала обрабатываются цели с большим приоритетом.
	final int priority;
	// Хранилище жидкости соседнего блока (куда вставляем или откуда извлекаем).
	final Storage<FluidVariant> storage;
	// Разрешено ли извлекать жидкость из трубы в это хранилище (режим OUT/IN_OUT).
	final boolean canExtract;
	// Разрешено ли вставлять жидкость из трубы в это хранилище (режим IN/IN_OUT).
	final boolean canInsert;

	// A temporary value used to sort fluid targets
	// Временное значение результата симуляции: по нему цели сортируются
	// внутри ведра с одинаковым приоритетом перед реальной передачей.
	long simulationResult;

	public FluidTarget(int priority, Storage<FluidVariant> storage, boolean canExtract, boolean canInsert) {
		this.priority = priority;
		this.storage = storage;
		this.canExtract = canExtract;
		this.canInsert = canInsert;
	}
}

package com.fabrica.conduit.fluid;

import com.fabrica.conduit.PipeStatsCollector;
import com.fabrica.conduit.api.PipeNetwork;
import com.fabrica.conduit.api.PipeNetworkData;
import com.fabrica.conduit.api.PipeNetworkNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.server.level.ServerLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Жидкостная сеть (объединение соседних жидкостных труб). Отвечает за:
 * - передачу одной жидкости по сети: каждый тик выбирается тип жидкости (первая
 *   непустая ёмкость-источник), затем извлечение из источников в сеть
 *   и вставка из сети в цели с учётом приоритетов подключений;
 * - приоритеты: передача идёт по "вёдрам" (buckets) — сначала цели с большим
 *   приоритетом, внутри ведра объём делится честно (симуляция + сортировка);
 * - ёмкость: каждый узел хранит до nodeCapacity мБ (у жидкостной трубы — 1 ведро);
 * - режимы подключений IN/IN_OUT/OUT каждого узла (вставка/извлечение);
 * - слияние сетей при соединении (merge): если в одной сети нет жидкости —
 *   поглощает данные другой без потерь;
 * - статистику переданного объёма и ёмкости (stats/capacityStats) для GUI.
 */
public class FluidNetwork extends PipeNetwork {
	// Логгер для сообщений об ошибках операций передачи.
	private static final Logger LOGGER = LoggerFactory.getLogger(FluidNetwork.class);

	// Ёмкость одного узла (блока трубы) в мБ (милливедра). Суммарная ёмкость сети
	// = nodeCapacity * количество узлов. Устанавливается при создании сети.
	final int nodeCapacity;
	// Статистика фактически переданной жидкости (мБ/сек) для GUI.
	final PipeStatsCollector stats = new PipeStatsCollector();
	// Статистика суммарной ёмкости сети (для отображения в GUI).
	final PipeStatsCollector capacityStats = new PipeStatsCollector();

	// Создаёт жидкостную сеть с заданным id, данными (включая тип жидкости) и ёмкостью узла.
	public FluidNetwork(int id, FluidNetworkData data, int nodeCapacity) {
		super(id, data);
		this.nodeCapacity = nodeCapacity;
	}

	/**
	 * Главный тик сети. Алгоритм:
	 * 1. Сбор целей: каждый узел добавляет свои подключённые ёмкости (FluidTarget)
	 *    в общий список и при необходимости выбирает жидкость сети
	 *    (gatherTargetsAndPickFluid). Также суммируется запас жидкости по узлам.
	 * 2. Если жидкость ещё не выбрана (blank) — сеть ничего не делает.
	 * 3. Извлечение: из источников выкачивается жидкость в сеть (свободное место).
	 * 4. Вставка: из сети жидкость раздаётся целям-потребителям.
	 *    Обе операции — внутри одной внешней транзакции (всё или ничего).
	 * 5. Оставшийся запас делится поровну между узлами (евклидово деление).
	 * 6. afterTick каждого узла: синхронизация клиенту при смене жидкости.
	 */
	@Override
	public void tick(ServerLevel world) {
		// Gather targets and hopefully set fluid
		List<FluidTarget> targets = new ArrayList<>();
		long networkAmount = 0;
		int loadedNodeCount = 0;
		for (var entry : iterateTickingNodes()) {
			FluidNetworkNode fluidNode = (FluidNetworkNode) entry.getNode();
			fluidNode.gatherTargetsAndPickFluid(world, entry.getPos(), targets);
			// Amount goes after the gather...() call because the gather...() call cleans
			// invalid amounts.
			networkAmount += fluidNode.amount;
			loadedNodeCount++;
		}
		// Суммарная ёмкость сети: по nodeCapacity на каждый загруженный узел.
		long networkCapacity = (long) loadedNodeCount * nodeCapacity;
		FluidVariant fluid = ((FluidNetworkData) data).fluid();

		long extracted = 0, inserted = 0;

		if (!fluid.isBlank()) {
			// Extract from targets into the network
			try (var tx = Transaction.openOuter()) {
				// Извлечение: качаем из источников ровно свободное место сети.
				extracted = transferByPriority(TransferOperation.EXTRACT, targets, fluid, networkCapacity - networkAmount, tx);
				networkAmount += extracted;
				// Insert into the targets from the network
				// Вставка: раздаём весь запас сети потребителям.
				inserted = transferByPriority(TransferOperation.INSERT, targets, fluid, networkAmount, tx);

				networkAmount -= inserted;

				tx.commit();
			}

			// Равномерное распределение оставшейся жидкости между узлами
			// (как аккумуляторы сети; остаток уходит первым узлам).
			for (var entry : iterateTickingNodes()) {
				FluidNetworkNode fluidNode = (FluidNetworkNode) entry.getNode();
				fluidNode.amount = networkAmount / loadedNodeCount;
				networkAmount -= fluidNode.amount;
				loadedNodeCount--;
			}
		}

		stats.addValue(Math.max(extracted, inserted));
		capacityStats.addValue(networkCapacity);

		// После передачи: узлы синхронизируют клиента, если жидкость сети сменилась.
		for (var entry : iterateTickingNodes()) {
			((FluidNetworkNode) entry.getNode()).afterTick(world, entry.getPos());
		}
	}

	/**
	 * Perform a transfer operation for a priority bucket, starting with higher
	 * priority targets.
	 *
	 * @return The amount that was successfully transferred.
	 */
	/**
	 * Выполняет операцию передачи (извлечение/вставку) с учётом приоритетов.
	 * Алгоритм:
	 * 1. Отфильтровываются цели, не разрешающие данную операцию
	 *    (например, при вставке нужны цели с режимом IN/IN_OUT).
	 * 2. Цели сортируются по убыванию приоритета.
	 * 3. Список делится на "вёдра" (buckets) — группы целей с одинаковым
	 *    приоритетом; каждое ведро обрабатывается функцией transferForBucket.
	 *    Сначала самые приоритетные цели, затем всё менее приоритетные,
	 *    пока не будет передана вся сумма maxAmount.
	 *
	 * @return Суммарно переданный объём.
	 */
	private static long transferByPriority(TransferOperation operation, List<FluidTarget> targets, FluidVariant fluid, long maxAmount, TransactionContext transaction) {
		// Only transfer through targets that allow this operation (respects the
		// import/export mode of each connection).
		targets.removeIf(target -> operation == TransferOperation.INSERT ? !target.canInsert : !target.canExtract);
		// Sort by decreasing priority
		targets.sort(Comparator.comparingInt(target -> -target.priority));
		// Transfer for each bucket
		long transferredAmount = 0;
		int bucketStart = 0;
		for (int i = 0; i < targets.size(); ++i) {
			// Граница ведра: последний элемент или у следующего цели другой приоритет.
			if (i == targets.size() - 1 || targets.get(bucketStart).priority != targets.get(i + 1).priority) {
				transferredAmount += transferForBucket(operation, targets.subList(bucketStart, i + 1), fluid, maxAmount - transferredAmount, transaction);
				bucketStart = i + 1;
			}
		}
		return transferredAmount;
	}

	/**
	 * Perform a transfer operation for a priority bucket, so {@code bucket} is a
	 * sublist of targets with the same priority each.
	 *
	 * @return The amount that was successfully transferred.
	 */
	/**
	 * Передаёт жидкость внутри одного ведра целей (одинаковый приоритет).
	 * Алгоритм "честного деления":
	 * 1. Перемешивание ведра — чтобы в среднем все цели получали поровну.
	 * 2. Симуляция операции для каждой цели во вложенной транзакции
	 *    (результат не применяется, сохраняется в simulationResult).
	 * 3. Сортировка по результату симуляции: цели, принявшие/отдавшие больше,
	 *    идут первыми — это даёт равномерное заполнение всех целей.
	 * 4. Реальное выполнение: оставшийся объём делится поровну между оставшимися
	 *    целями (remainingAmount / remainingTargets).
	 *
	 * @return Суммарно переданный объём в этом ведре.
	 */
	private static long transferForBucket(TransferOperation operation, List<FluidTarget> bucket, FluidVariant fluid, long maxAmount, TransactionContext transaction) {
		// Shuffle the bucket for better average transfer when simulation returns the
		// same result every time
		Collections.shuffle(bucket);
		// Simulate the transfer for every target
		int maxAmountInt = (int) Math.min(Integer.MAX_VALUE, maxAmount);
		for (FluidTarget target : bucket) {
			// Вложенная транзакция: симуляция не меняет реальное состояние хранилища.
			try (var nested = Transaction.openNested(transaction)) {
				target.simulationResult = operation.transfer(target.storage, fluid, maxAmountInt, nested);
			}
		}
		// Sort from low result to high result
		bucket.sort(Comparator.comparingLong(target -> target.simulationResult));
		// Actually perform the transfer
		long transferredAmount = 0;
		for (int i = 0; i < bucket.size(); ++i) {
			FluidTarget target = bucket.get(i);
			int remainingTargets = bucket.size() - i;
			long remainingAmount = maxAmount - transferredAmount;
			int targetMaxAmount = (int) Math.min(Integer.MAX_VALUE, remainingAmount / remainingTargets);

			transferredAmount += operation.transfer(target.storage, fluid, targetMaxAmount, transaction);
		}
		return transferredAmount;
	}

	// Два вида операций передачи: INSERT (вставка в хранилище) и EXTRACT (извлечение).
	// Безопасная обёртка internalTransfer проверяет, что результат операции корректен
	// (не отрицательный и не больше запрошенного), иначе логирует ошибку.
	private enum TransferOperation {
		INSERT {
			@Override
			long internalTransfer(Storage<FluidVariant> handler, FluidVariant fluid, int maxAmount, TransactionContext transaction) {
				return handler.insert(fluid, maxAmount, transaction);
			}
		},
		EXTRACT {
			@Override
			long internalTransfer(Storage<FluidVariant> handler, FluidVariant fluid, int maxAmount, TransactionContext transaction) {
				return handler.extract(fluid, maxAmount, transaction);
			}
		};

		abstract long internalTransfer(Storage<FluidVariant> handler, FluidVariant fluid, int maxAmount, TransactionContext transaction);

		// Выполняет операцию и валидирует результат: отрицательный или превышающий
		// запрошенный объём считается ошибкой хранилища и приводится к безопасному значению.
		long transfer(Storage<FluidVariant> handler, FluidVariant fluid, int maxAmount, TransactionContext transaction) {
			long ret = internalTransfer(handler, fluid, maxAmount, transaction);
			if (ret < 0) {
				LOGGER.error("Transfer operation {}({}, {}, {}) on fluid handler {} returned negative amount: {}", this, fluid, maxAmount, transaction,
						handler, ret);
				return 0;
			}
			if (ret > maxAmount) {
				LOGGER.error("Transfer operation {}({}, {}, {}) on fluid handler {} returned more than requested: {}", this, fluid, maxAmount,
						transaction, handler, ret);
				return maxAmount;
			}
			return ret;
		}
	}

	/**
	 * Слияние данных двух сетей при соединении труб. Правила:
	 * - если хотя бы одна из сетей пуста (нет жидкости) — побеждают данные
	 *   непустой сети (возвращается её копия);
	 * - если пусты обе или непусты обе — вернётся null, и сети НЕ соединяются
	 *   (разные жидкости не могут течь в одной сети).
	 * Проверка делается в два прохода: сначала только по жидкости, затем ещё
	 * и по фактическому запасу узлов (amount).
	 */
	@Override
	public PipeNetworkData merge(PipeNetwork other) {
		FluidNetworkData thisData = (FluidNetworkData) data;
		FluidNetworkData otherData = (FluidNetworkData) other.data;
		// If one is empty, it's easy to merge.
		// First check for empty fluid, then also check for empty network the second
		// time
		for (int i = 0; i < 2; ++i) {
			boolean onlyFluid = i == 0;
			if (this.isEmpty(onlyFluid))
				return otherData.clone();
			if (((FluidNetwork) other).isEmpty(onlyFluid))
				return thisData.clone();
		}
		return null;
	}

	// Проверка пустоты сети: жидкость не выбрана (blank), а при onlyFluid = false
	// дополнительно ни один узел не хранит жидкости (amount != 0).
	private boolean isEmpty(boolean onlyFluid) {
		if (((FluidNetworkData) data).fluid().isBlank())
			return true;
		if (onlyFluid)
			return false;
		for (PipeNetworkNode node : getRawNodeMap().values()) {
			if (node == null || ((FluidNetworkNode) node).amount != 0) {
				return false;
			}
		}
		return true;
	}
}

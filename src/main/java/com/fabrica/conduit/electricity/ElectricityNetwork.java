package com.fabrica.conduit.electricity;

import com.fabrica.api.energy.CableTier;
import com.fabrica.api.energy.EnergyContainer;
import com.fabrica.conduit.PipeStatsCollector;
import com.fabrica.conduit.api.PipeNetwork;
import com.fabrica.conduit.api.PipeNetworkData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.minecraft.server.level.ServerLevel;

/**
 * Электрическая сеть (объединение соседних кабелей одного типа). Отвечает за:
 * - хранение тира кабеля (tier) — определяет максимальную пропускную способность;
 * - каждый тик: сбор всех подключённых хранилищ энергии (EnergyContainer),
 *   извлечение энергии из генераторов в сеть, вставку в потребителей
 *   и равномерное распределение энергии по узлам;
 * - равномерный способ распределения: энергия делится поровну между всеми
 *   узлами сети (по 1 тику на каждый узел);
 * - сбор статистики передачи (stats) для отображения в GUI.
 * Тиры: чем выше тир кабеля, тем больше maxTransfer() — энергии за тик.
 */
public class ElectricityNetwork extends PipeNetwork {
	// Кэш-список хранилищ на время одного тика (статический, чтобы не создавать
	// новый список на каждый узел). Обязательно очищается в конце tick().
	private static final List<EnergyContainer> STORAGES_CACHE = new ArrayList<>();

	// Операции извлечения и вставки энергии: абстракция над insert/extract,
	// чтобы единый transferForTargets() работал в обе стороны.
	private static final TransferOperation EXTRACT = (storage, amount, simulate) -> storage.extractEnergy(amount, simulate);
	private static final TransferOperation INSERT = (storage, amount, simulate) -> storage.insertEnergy(amount, simulate);

	// Тир кабеля сети: ограничивает скорость передачи (EU/тик).
	final CableTier tier;
	// Статистика переданной энергии для GUI (EU/тик).
	final PipeStatsCollector stats = new PipeStatsCollector();

	// Создаёт сеть с заданным id, данными и тиром кабеля.
	public ElectricityNetwork(int id, ElectricityNetworkData data, CableTier tier) {
		super(id, data);
		this.tier = tier;
	}

	/**
	 * Главный тик сети (вызывается каждый игровой тик). Порядок работы:
	 * 1. Сбор: у каждого узла спрашиваются подключённые хранилища, суммируется
	 *    запас энергии (eu) по всем узлам.
	 * 2. Фильтр: удаляются хранилища, к которым кабель этого тира не может подключиться.
	 * 3. Извлечение: из хранилищ выкачивается столько, сколько позволяет тир,
	 *    при этом сеть не может запасти больше, чем суммарная ёмкость узлов.
	 * 4. Вставка: энергия из запаса сети раздаётся хранилищам-потребителям.
	 * 5. Распределение: оставшаяся энергия делится поровну между всеми узлами
	 *    (евклидово деление по очереди — остаток уходит первым узлам).
	 * 6. Очистка статического кэша хранилищ.
	 */
	@Override
	public void tick(ServerLevel world) {
		// Gather targets
		List<EnergyContainer> storages = STORAGES_CACHE;
		long networkAmount = 0;
		int loadedNodeCount = 0;
		for (var entry : iterateTickingNodes()) {
			ElectricityNetworkNode node = (ElectricityNetworkNode) entry.getNode();
			// Каждый узел добавляет свои подключённые хранилища в общий список.
			node.appendAttributes(world, entry.getPos(), tier, storages);
			networkAmount += node.eu;
			loadedNodeCount++;
		}

		// Filter targets
		storages.removeIf(s -> !canConnect(tier, s));

		// Do the transfer
		// Суммарная ёмкость сети: по одному "аккумулятору" (tier.maxTransfer) на каждый узел.
		long networkCapacity = loadedNodeCount * tier.maxTransfer();
		// Извлекаем только свободное место в сети (не больше тира за раз).
		long extractMaxAmount = Math.min(tier.maxTransfer(), networkCapacity - networkAmount);
		long extracted = transferForTargets(EXTRACT, storages, extractMaxAmount);
		networkAmount += extracted;

		// Вставляем потребителям всё, что есть в сети (не больше тира за раз).
		long insertMaxAmount = Math.min(tier.maxTransfer(), networkAmount);
		long inserted = transferForTargets(INSERT, storages, insertMaxAmount);
		networkAmount -= inserted;

		stats.addValue(Math.max(extracted, inserted));

		// Split energy evenly across the nodes
		// Равномерное распределение запаса по узлам: каждый узел получает
		// честную долю, остаток от деления раздаётся первым узлам.
		for (var entry : iterateTickingNodes()) {
			ElectricityNetworkNode electricityNode = (ElectricityNetworkNode) entry.getNode();
			electricityNode.eu = networkAmount / loadedNodeCount;
			networkAmount -= electricityNode.eu;
			--loadedNodeCount;
		}

		// Very important to clear the static caches
		// Обязательная очистка статического кэша, иначе хранилища накопятся за тики.
		storages.clear();
	}

	// Проверка, может ли кабель этого тира подключиться к хранилищу.
	// Как в Modern Industrialization: любой кабель подключается к любой машине,
	// тир ограничивает только скорость передачи, а не совместимость.
	static boolean canConnect(CableTier tier, EnergyContainer storage) {
		// Like MI, any cable can connect to any machine; the cable tier only
		// limits the transfer rate.
		return storage.getTier() != null;
	}

	/**
	 * Perform a transfer operation across a list of targets. Will not mutate the
	 * list. Does not check for the network's max transfer rate specifically.
	 */
	/**
	 * Выполняет операцию передачи (извлечение или вставку) по списку хранилищ.
	 * Алгоритм "честного деления":
	 * 1. Оборачивает каждое хранилище в EnergyTarget (для результата симуляции).
	 * 2. Перемешивает список — чтобы в среднем все хранилища получали поровну.
	 * 3. Симулирует операцию для каждого хранилища (simulate = true, без изменений).
	 * 4. Сортирует по результату симуляции от меньшего к большему — хранилища,
	 *    которые приняли/отдали больше, обрабатываются первыми (наименее жадные — в конец).
	 * 5. Реально выполняет операцию, деля оставшийся объём поровну между оставшимися
	 *    целями (remainingAmount / remainingTargets).
	 * Список не мутируется; суммарный лимит maxAmount сети при этом проверяется.
	 */
	private static long transferForTargets(TransferOperation operation, List<EnergyContainer> targets, long maxAmount) {
		// Build target list
		List<EnergyTarget> sortableTargets = new ArrayList<>(targets.size());
		for (var target : targets) {
			sortableTargets.add(new EnergyTarget(target));
		}
		// Shuffle for better transfer on average
		Collections.shuffle(sortableTargets);
		// Simulate the transfer for every target
		// Симуляция: сколько реально сможет принять/отдать каждое хранилище.
		for (EnergyTarget target : sortableTargets) {
			target.simulationResult = operation.transfer(target.target, maxAmount, true);
		}
		// Sort from low to high result
		sortableTargets.sort(Comparator.comparingLong(t -> t.simulationResult));
		// Actually perform the transfer
		// Реальное выполнение: поровну делим оставшийся объём между оставшимися целями.
		long transferredAmount = 0;
		for (int i = 0; i < sortableTargets.size(); ++i) {
			EnergyTarget target = sortableTargets.get(i);
			int remainingTargets = sortableTargets.size() - i;
			long remainingAmount = maxAmount - transferredAmount;
			long targetMaxAmount = remainingAmount / remainingTargets;

			transferredAmount += operation.transfer(target.target, targetMaxAmount, false);
		}
		return transferredAmount;
	}

	// Функциональный интерфейс операции передачи: извлечение или вставка энергии.
	@FunctionalInterface
	private interface TransferOperation {
		long transfer(EnergyContainer transferable, long maxAmount, boolean simulate);
	}

	// Обёртка хранилища для сортировки: хранит саму цель и результат симуляции,
	// по которому цели сортируются перед реальной передачей.
	private static class EnergyTarget {
		final EnergyContainer target;
		long simulationResult;

		EnergyTarget(EnergyContainer target) {
			this.target = target;
		}
	}
}

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

public class ElectricityNetwork extends PipeNetwork {
	private static final List<EnergyContainer> STORAGES_CACHE = new ArrayList<>();

	private static final TransferOperation EXTRACT = (storage, amount, simulate) -> storage.extractEnergy(amount, simulate);
	private static final TransferOperation INSERT = (storage, amount, simulate) -> storage.insertEnergy(amount, simulate);

	final CableTier tier;
	final PipeStatsCollector stats = new PipeStatsCollector();

	public ElectricityNetwork(int id, ElectricityNetworkData data, CableTier tier) {
		super(id, data);
		this.tier = tier;
	}

	@Override
	public void tick(ServerLevel world) {
		// Gather targets
		List<EnergyContainer> storages = STORAGES_CACHE;
		long networkAmount = 0;
		int loadedNodeCount = 0;
		for (var entry : iterateTickingNodes()) {
			ElectricityNetworkNode node = (ElectricityNetworkNode) entry.getNode();
			node.appendAttributes(world, entry.getPos(), tier, storages);
			networkAmount += node.eu;
			loadedNodeCount++;
		}

		// Filter targets
		storages.removeIf(s -> !canConnect(tier, s));

		// Do the transfer
		long networkCapacity = loadedNodeCount * tier.maxTransfer();
		long extractMaxAmount = Math.min(tier.maxTransfer(), networkCapacity - networkAmount);
		long extracted = transferForTargets(EXTRACT, storages, extractMaxAmount);
		networkAmount += extracted;

		long insertMaxAmount = Math.min(tier.maxTransfer(), networkAmount);
		long inserted = transferForTargets(INSERT, storages, insertMaxAmount);
		networkAmount -= inserted;

		stats.addValue(Math.max(extracted, inserted));

		// Split energy evenly across the nodes
		for (var entry : iterateTickingNodes()) {
			ElectricityNetworkNode electricityNode = (ElectricityNetworkNode) entry.getNode();
			electricityNode.eu = networkAmount / loadedNodeCount;
			networkAmount -= electricityNode.eu;
			--loadedNodeCount;
		}

		// Very important to clear the static caches
		storages.clear();
	}

	static boolean canConnect(CableTier tier, EnergyContainer storage) {
		// Like MI, any cable can connect to any machine; the cable tier only
		// limits the transfer rate.
		return storage.getTier() != null;
	}

	/**
	 * Perform a transfer operation across a list of targets. Will not mutate the
	 * list. Does not check for the network's max transfer rate specifically.
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
		for (EnergyTarget target : sortableTargets) {
			target.simulationResult = operation.transfer(target.target, maxAmount, true);
		}
		// Sort from low to high result
		sortableTargets.sort(Comparator.comparingLong(t -> t.simulationResult));
		// Actually perform the transfer
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

	@FunctionalInterface
	private interface TransferOperation {
		long transfer(EnergyContainer transferable, long maxAmount, boolean simulate);
	}

	private static class EnergyTarget {
		final EnergyContainer target;
		long simulationResult;

		EnergyTarget(EnergyContainer target) {
			this.target = target;
		}
	}
}

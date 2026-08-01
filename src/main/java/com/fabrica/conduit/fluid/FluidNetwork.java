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

public class FluidNetwork extends PipeNetwork {
	private static final Logger LOGGER = LoggerFactory.getLogger(FluidNetwork.class);

	final int nodeCapacity;
	final PipeStatsCollector stats = new PipeStatsCollector();
	final PipeStatsCollector capacityStats = new PipeStatsCollector();

	public FluidNetwork(int id, FluidNetworkData data, int nodeCapacity) {
		super(id, data);
		this.nodeCapacity = nodeCapacity;
	}

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
		long networkCapacity = (long) loadedNodeCount * nodeCapacity;
		FluidVariant fluid = ((FluidNetworkData) data).fluid();

		long extracted = 0, inserted = 0;

		if (!fluid.isBlank()) {
			// Extract from targets into the network
			try (var tx = Transaction.openOuter()) {
				extracted = transferByPriority(TransferOperation.EXTRACT, targets, fluid, networkCapacity - networkAmount, tx);
				networkAmount += extracted;
				// Insert into the targets from the network
				inserted = transferByPriority(TransferOperation.INSERT, targets, fluid, networkAmount, tx);

				networkAmount -= inserted;

				tx.commit();
			}

			for (var entry : iterateTickingNodes()) {
				FluidNetworkNode fluidNode = (FluidNetworkNode) entry.getNode();
				fluidNode.amount = networkAmount / loadedNodeCount;
				networkAmount -= fluidNode.amount;
				loadedNodeCount--;
			}
		}

		stats.addValue(Math.max(extracted, inserted));
		capacityStats.addValue(networkCapacity);

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
	private static long transferByPriority(TransferOperation operation, List<FluidTarget> targets, FluidVariant fluid, long maxAmount, TransactionContext transaction) {
		// Sort by decreasing priority
		targets.sort(Comparator.comparingInt(target -> -target.priority));
		// Transfer for each bucket
		long transferredAmount = 0;
		int bucketStart = 0;
		for (int i = 0; i < targets.size(); ++i) {
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
	private static long transferForBucket(TransferOperation operation, List<FluidTarget> bucket, FluidVariant fluid, long maxAmount, TransactionContext transaction) {
		// Shuffle the bucket for better average transfer when simulation returns the
		// same result every time
		Collections.shuffle(bucket);
		// Simulate the transfer for every target
		int maxAmountInt = (int) Math.min(Integer.MAX_VALUE, maxAmount);
		for (FluidTarget target : bucket) {
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

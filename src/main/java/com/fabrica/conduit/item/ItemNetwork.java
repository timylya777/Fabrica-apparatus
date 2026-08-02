package com.fabrica.conduit.item;

import com.fabrica.conduit.api.PipeNetwork;
import com.fabrica.conduit.api.PipeNetworkData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

public class ItemNetwork extends PipeNetwork {
	public static final int TICK_RATE = 20;
	static final int BASE_ITEM_PIPE_TRANSFER = 16;

	int inactiveTicks = 0;
	long lastMovedItems = 0;
	@Nullable
	ExtractionSource currentExtractionSource;

	public ItemNetwork(int id, ItemNetworkData data) {
		super(id, data);
	}

	@Override
	public void tick(ServerLevel world) {
		// Only tick once
		if (inactiveTicks == 0) {
			doNetworkTransfer(world);
			inactiveTicks = TICK_RATE;
		}
		--inactiveTicks;
	}

	private void doNetworkTransfer(ServerLevel world) {
		List<ExtractionSource> extractionSources = new ArrayList<>();
		for (var entry : iterateTickingNodes()) {
			BlockPos pos = entry.getPos();
			ItemNetworkNode itemNode = (ItemNetworkNode) entry.getNode();
			for (ItemNetworkNode.ItemConnection connection : itemNode.connections) {
				if (connection.canExtract()) {
					var queryPos = pos.relative(connection.direction);
					var querySide = connection.direction.getOpposite();

					var source = ItemStorage.SIDED.find(world, queryPos, querySide);

					if (source != null) {
						extractionSources.add(new ExtractionSource(connection, source, queryPos, querySide));
					}
				}
			}
		}
		// Lower priority extracts first.
		extractionSources.sort(Comparator.comparing(et -> et.connection().extractPriority));

		// Do the actual transfer.
		var insertTargets = getAggregatedInsertTargets(world);
		var insertionHandler = new CombinedInsertionHandler<>(insertTargets);
		lastMovedItems = 0;
		try (var tx = Transaction.openOuter()) {
			for (ExtractionSource source : extractionSources) {
				currentExtractionSource = source;
				// Lower priority extracts first, and pipes can only move items to things that have >= priorities.
				// So we can just pop insert targets at the end of the list if they have a priority smaller than the current extraction target.
				while (!insertTargets.isEmpty() && source.connection().extractPriority > insertTargets.getLast().getPriority()) {
					insertTargets.removeLast();
				}

				try {
					lastMovedItems += move(
							source.itemHandler(),
							insertionHandler,
							source.connection()::canMoveThrough,
							source.connection().getMoves(),
							tx);
				} catch (Exception exception) {
					var crashReport = CrashReport.forThrowable(exception, "Moving items in a pipe network");
					crashReport.addCategory("Block being extracted from:")
							.setDetail("Dimension", world.dimension())
							.setDetail("Position", source.queryPos())
							.setDetail("Accessed from side", source.querySide());
					throw new ReportedException(crashReport);
				}
			}

			tx.commit();
		} finally {
			currentExtractionSource = null;
		}
	}

	/**
	 * Move items from a storage into a combined insertion handler.
	 */
	private static long move(Storage<ItemVariant> from, CombinedInsertionHandler<ItemVariant> to, java.util.function.Predicate<ItemVariant> filter,
			int amount, @Nullable TransactionContext transaction) {
		if (from == null || amount == 0) return 0;
		try (Transaction subTx = Transaction.openNested(transaction)) {
			long totalMoved = 0;
			for (StorageView<ItemVariant> view : from.nonEmptyViews()) {
				ItemVariant resource = view.getResource();
				if (resource.isBlank() || !filter.test(resource)) continue;

				// check how much can be extracted
				long maxExtracted;
				try (Transaction simulated = Transaction.openNested(subTx)) {
					maxExtracted = from.extract(resource, amount - totalMoved, simulated);
				}
				if (maxExtracted == 0) continue;

				try (Transaction transferTx = Transaction.openNested(subTx)) {
					long inserted = to.insert(resource, maxExtracted, transferTx);

					// extract it, or rollback if we cannot actually extract the amount we inserted
					if (inserted != from.extract(resource, inserted, transferTx))
						continue;

					totalMoved += inserted;
					transferTx.commit();

					if (totalMoved >= amount) break;
				}
			}
			subTx.commit();
			return totalMoved;
		}
	}

	/**
	 * Find all connections in which to insert that are loaded.
	 */
	private List<Aggregate> getAggregatedInsertTargets(ServerLevel world) {
		Map<Integer, PriorityBucket> priorityBuckets = new java.util.HashMap<>();

		for (var entry : iterateTickingNodes()) {
			ItemNetworkNode node = (ItemNetworkNode) entry.getNode();
			for (ItemNetworkNode.ItemConnection connection : node.connections) {
				if (connection.canInsert()) {
					var target = ItemStorage.SIDED.find(world, entry.getPos().relative(connection.direction), connection.direction.getOpposite());
					if (target != null) {
						PriorityBucket bucket = priorityBuckets.computeIfAbsent(connection.insertPriority, PriorityBucket::new);
						InsertTarget it = new InsertTarget(connection, target);

						if (connection.whitelist) {
							bucket.whitelist.add(it);
						} else {
							bucket.blacklist.add(it);
						}
					}
				}
			}
		}

		PriorityBucket[] sortedBuckets = priorityBuckets.values().toArray(new PriorityBucket[0]);
		// Now we sort by priority, high to low
		Arrays.sort(sortedBuckets, Comparator.comparingInt(pb -> -pb.priority));

		List<Aggregate> targets = new ArrayList<>();
		Random random = ThreadLocalRandom.current();

		for (PriorityBucket pb : sortedBuckets) {
			int whitelistSize = pb.whitelist.size();
			int blacklistSize = pb.blacklist.size();
			if (whitelistSize > 0) {
				Collections.shuffle(pb.whitelist);
				targets.add(new WhitelistAggregate(pb.priority, pb.whitelist));
			}
			if (blacklistSize > 0) {
				Collections.shuffle(pb.blacklist);
				targets.add(new BlacklistAggregate(pb.priority, pb.blacklist));
			}

			// Ensure equal chance to receive items on average.
			if (whitelistSize > 0 && blacklistSize > 0) {
				if (random.nextDouble() >= (double) whitelistSize / (whitelistSize + blacklistSize)) {
					Collections.swap(targets, targets.size() - 2, targets.size() - 1);
				}
			}
		}

		return targets;
	}

	private static class PriorityBucket {
		private final int priority;
		private final List<InsertTarget> whitelist = new ArrayList<>();
		private final List<InsertTarget> blacklist = new ArrayList<>();

		private PriorityBucket(int priority) {
			this.priority = priority;
		}
	}

	private interface Aggregate extends InsertionHandler<ItemVariant> {
		int getPriority();
	}

	private class WhitelistAggregate implements Aggregate {
		private final int priority;
		// Used when the inserted item doesn't have NBT
		private final Map<Item, List<InsertTarget>> map = new IdentityHashMap<>();
		// Used when the inserted item has NBT.
		private final List<InsertTarget> targets;

		WhitelistAggregate(int priority, List<InsertTarget> targets) {
			this.priority = priority;
			this.targets = targets;
			for (InsertTarget target : targets) {
				ItemNetworkNode.ItemConnection conn = target.connection;
				for (ItemVariant stack : conn.stacks) {
					if (!stack.isBlank()) {
						map.computeIfAbsent(stack.getItem(), v -> new ArrayList<>()).add(target);
					}
				}
			}
		}

		@Override
		public long insert(ItemVariant resource, long amount, TransactionContext transaction) {
			var insertionTargets = resource.getItem() != null ? map.get(resource.getItem()) : targets;
			return insertionTargets == null ? 0 : insertTargets(insertionTargets, resource, amount, transaction);
		}

		@Override
		public int getPriority() {
			return priority;
		}
	}

	private class BlacklistAggregate implements Aggregate {
		private final int priority;
		private final List<InsertTarget> targets;

		private BlacklistAggregate(int priority, List<InsertTarget> targets) {
			this.priority = priority;
			this.targets = targets;
		}

		@Override
		public long insert(ItemVariant resource, long amount, TransactionContext transaction) {
			return insertTargets(targets, resource, amount, transaction);
		}

		@Override
		public int getPriority() {
			return priority;
		}
	}

	private long insertTargets(List<InsertTarget> targets, ItemVariant resource, long amount, TransactionContext transaction) {
		long inserted = 0;
		for (var target : targets) {
			if (currentExtractionSource != null && currentExtractionSource.connection() == target.connection()) {
				// Avoid self-insertion
				continue;
			}
			if (target.connection.canMoveThrough(resource)) {
				inserted += target.target.insert(resource, amount - inserted, transaction);
				if (inserted == amount) break;
			}
		}
		return inserted;
	}

	private record InsertTarget(ItemNetworkNode.ItemConnection connection, Storage<ItemVariant> target) {}
}

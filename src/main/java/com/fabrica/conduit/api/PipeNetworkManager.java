package com.fabrica.conduit.api;

import com.fabrica.FabricaMod;
import com.fabrica.conduit.FabricaPipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.status.ChunkStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Отвечает за все сети одного типа труб в измерении: ведёт реестр сетей и
 * соответствие «позиция блока -> сеть», хранит ссылки между узлами (links),
 * добавляет/удаляет узлы и связи, сливает сети при соединении (addLink) и
 * разбивает их при разрыве (removeLink, поиск в глубину), следит за
 * загруженными чанками (spannedChunks/tickingChunks) и тикает сети каждый тик.
 * Не сериализуется — строится заново из узлов.
 */
public class PipeNetworkManager {
	private final Map<BlockPos, PipeNetwork> networkByBlock = new HashMap<>();
	private final Map<BlockPos, Set<Direction>> links = new HashMap<>();
	private final Set<PipeNetwork> networks = new HashSet<>();
	private int nextNetworkId = 0;
	private final PipeNetworkType type;

	private final Map<Long, Set<BlockPos>> spannedChunks = new HashMap<>();
	protected final Set<Long> tickingChunks = new HashSet<>();

	public PipeNetworkManager(PipeNetworkType type) {
		this.type = type;
	}

	public boolean isChunkTicking(long chunkPos) {
		return tickingChunks.contains(chunkPos);
	}

	/**
	 * Tick networks
	 */
	// Тикает все сети: сначала обновляет список активных чанков, затем тикает
	// каждую сеть и помечает затронутые чанки как изменённые (для сохранения).
	public void tickNetworks(ServerLevel world) {
		updateTickingChunks(world);

		for (PipeNetwork network : networks) {
			network.tick(world);
		}

		for (long chunkPos : tickingChunks) {
			int chunkX = ChunkPos.getX(chunkPos);
			int chunkZ = ChunkPos.getZ(chunkPos);
			var chunk = world.getChunk(chunkX, chunkZ, ChunkStatus.FULL, false);
			if (chunk != null) {
				chunk.markUnsaved();
			} else {
				FabricaMod.LOGGER.error("Fabrica pipes issue: ticking spanned chunk was not loaded anymore.");
			}
		}
	}

	public boolean hasNode(BlockPos pos) {
		return networkByBlock.containsKey(pos);
	}

	// Пересчитывает, какие из затронутых чанков сейчас тикаются, и
	// инвалидирует кэш тикающихся узлов у сетей в этих чанках.
	private void updateTickingChunks(ServerLevel world) {
		tickingChunks.clear();
		for (long chunk : spannedChunks.keySet()) {
			if (isChunkTicking(world, chunk)) {
				tickingChunks.add(chunk);
			}
		}
		for (long chunk : tickingChunks) {
			notifyTickingChanged(spannedChunks.get(chunk));
		}
	}

	private static boolean isChunkTicking(ServerLevel world, long chunkPos) {
		int chunkX = ChunkPos.getX(chunkPos);
		int chunkZ = ChunkPos.getZ(chunkPos);
		return world.getChunkSource().isPositionTicking(chunkPos);
	}

	// Помечает кэш тикающихся узлов у сетей в заданном чанке как устаревший.
	private void notifyTickingChanged(Set<BlockPos> positionsInChunk) {
		if (positionsInChunk != null) {
			for (BlockPos pos : positionsInChunk) {
				PipeNetwork network = networkByBlock.get(pos);
				if (network != null) {
					network.tickingCacheValid = false;
				}
			}
		}
	}

	/**
	 * Add a network link and merge networks if necessary. Both the node at pos and
	 * the node at pos + direction must exist in the network.
	 */
	// Добавляет связь между узлом в pos и узлом в pos+direction; если они
	// находятся в разных сетях — сливает сети в одну (с предварительным
	// merge данных при разрешении). При force=true связь создаётся даже при
	// несовпадении данных, если сети разрешают слияние.
	public void addLink(BlockPos pos, Direction direction, boolean force) {
		if (hasLink(pos, direction))
			return;
		if (!canLink(pos, direction, force))
			return;

		BlockPos otherPos = pos.relative(direction);
		links.get(pos).add(direction);
		links.get(otherPos).add(direction.getOpposite());

		PipeNetwork network = networkByBlock.get(pos);
		PipeNetwork otherNetwork = networkByBlock.get(otherPos);
		if (network != otherNetwork) {
			if (!network.data.equals(otherNetwork.data)) {
				network.data = network.merge(otherNetwork);
			}
			for (Map.Entry<BlockPos, PipeNetworkNode> entry : otherNetwork.getRawNodeMap().entrySet()) {
				PipeNetworkNode node = entry.getValue();
				BlockPos nodePos = entry.getKey();
				if (node != null) {
					node.network = network;
				}
				networkByBlock.put(nodePos, network);
				network.setNode(nodePos, node);
			}
			var nodesCopy = new ArrayList<>(otherNetwork.getRawNodeMap().keySet());
			for (var nodePos : nodesCopy) {
				otherNetwork.removeNode(nodePos);
			}
			otherNetwork.onRemove();
			networks.remove(otherNetwork);
		}
		network.tickingCacheValid = false;
	}

	/**
	 * Remove a network link and split networks if necessary. Both the node at pos
	 * and the node at pos + direction must exist in the network.
	 */
	// Убирает связь между узлами; если после этого сеть распадается на две
	// компоненты связности — создаёт новую сеть для отрезанной части
	// (обход в глубину от pos по оставшимся связям).
	public void removeLink(BlockPos pos, Direction direction) {
		if (!hasLink(pos, direction))
			return;

		BlockPos otherPos = pos.relative(direction);
		links.get(pos).remove(direction);
		links.get(otherPos).remove(direction.getOpposite());

		PipeNetwork network = networkByBlock.get(pos);
		Map<BlockPos, PipeNetworkNode> unvisitedNodes = new HashMap<>(network.getRawNodeMap());
		network.tickingCacheValid = false;

		class Dfs {
			private void dfs(BlockPos currentPos) {
				if (!unvisitedNodes.containsKey(currentPos)) {
					return;
				}
				unvisitedNodes.remove(currentPos);
				for (Direction d : links.get(currentPos)) {
					dfs(currentPos.relative(d));
				}
			}
		}

		Dfs dfs = new Dfs();
		dfs.dfs(pos);

		if (unvisitedNodes.size() > 0) {
			PipeNetwork newNetwork = createNetwork(network.data.clone());
			for (Map.Entry<BlockPos, PipeNetworkNode> entry : unvisitedNodes.entrySet()) {
				PipeNetworkNode node = entry.getValue();
				BlockPos nodePos = entry.getKey();
				if (node != null) {
					node.network = newNetwork;
				}
				networkByBlock.put(nodePos, newNetwork);
				newNetwork.setNode(nodePos, node);
				network.removeNode(nodePos);
			}
		}
	}

	public boolean hasLink(BlockPos pos, Direction direction) {
		var nodeLinks = links.get(pos);
		return nodeLinks != null && nodeLinks.contains(direction);
	}

	// Можно ли провести связь: в соседней позиции есть сеть, и её данные
	// совпадают (или force и merge разрешает объединение).
	public boolean canLink(BlockPos pos, Direction direction, boolean forceLink) {
		BlockPos otherPos = pos.relative(direction);
		PipeNetwork network = networkByBlock.get(pos);
		PipeNetwork otherNetwork = networkByBlock.get(otherPos);
		return otherNetwork != null && (network.data.equals(otherNetwork.data) || forceLink && network.merge(otherNetwork) != null);
	}

	/**
	 * Add a node and create a new network for it.
	 */
	// Добавляет узел: создаёт для него новую сеть с клонированными данными,
	// регистрирует позицию в spannedChunks и готовит пустой набор ссылок.
	// Если на позиции остался «устаревший» узел от неправильного удаления —
	// сначала убирает его.
	public void addNode(PipeNetworkNode node, BlockPos pos, PipeNetworkData data) {
		if (networkByBlock.containsKey(pos)) {
			// A stale entry can remain if a previous pipe at this position was
			// removed through a path that only unloaded its node. Clean it up
			// before adding the new node, otherwise the position would be
			// permanently unplaceable.
			removeNode(pos);
		}

		PipeNetwork network = createNetwork(data.clone());
		if (node != null) {
			node.network = network;
		}
		networkByBlock.put(pos.immutable(), network);
		incrementSpanned(pos);
		network.setNode(pos, node);
		links.put(pos.immutable(), new HashSet<>());
	}

	/**
	 * Remove a node and its network. Will remove all remaining links.
	 */
	// Удаляет узел и его сеть: снимает все связи (при этом сеть может
	// распасться), вынимает сеть из реестра и вызывает onRemove().
	public void removeNode(BlockPos pos) {
		for (Direction direction : Direction.values()) {
			removeLink(pos, direction);
		}

		PipeNetwork network = networkByBlock.remove(pos);
		decrementSpanned(pos);
		network.onRemove();
		networks.remove(network);
		links.remove(pos);
	}

	/**
	 * Should be called when a node is loaded, it will link the node to its network.
	 */
	// Вызывается при загрузке чанка с узлом: если сеть на этой позиции ещё
	// существует (например, соседний узел не выгружался) — узел просто
	// подключается к ней; иначе создаётся новая сеть с данными по умолчанию
	// и все шесть связей пересоздаются.
	public void nodeLoaded(PipeNetworkNode node, BlockPos pos) {
		PipeNetwork network = networkByBlock.get(pos);
		if (network == null) {
			PipeNetworkData data = FabricaPipes.getDefaultData(getType());
			addNode(node, pos, data);
			for (Direction direction : Direction.values()) {
				addLink(pos, direction, false);
			}
		} else {
			node.network = network;
			network.setNode(pos, node);
			network.tickingCacheValid = false;
		}
		incrementSpanned(pos);
	}

	/**
	 * Should be called when a node is unloaded, it will unlink the node from its network.
	 */
	// Вызывается при выгрузке чанка: узел помечается null в карте сети
	// (позиция остаётся, чтобы сеть не разваливалась), чанк убирается из
	// spannedChunks, кэш тикающихся узлов инвалидируется.
	public void nodeUnloaded(PipeNetworkNode node, BlockPos pos) {
		node.network.setNode(pos, null);
		node.network.tickingCacheValid = false;
		decrementSpanned(pos);
	}

	// Создаёт сеть нужного типа через фабрику типа трубы, назначает менеджер,
	// выдаёт следующий id и регистрирует в наборе сетей.
	private PipeNetwork createNetwork(PipeNetworkData data) {
		PipeNetwork network = type.getNetworkCtor().apply(nextNetworkId, data);
		network.manager = this;
		nextNetworkId++;
		networks.add(network);
		return network;
	}

	// Учёт позиций по чанкам: прибавляет позицию к списку «затронутых» чанков
	// (нужно для определения тикающихся чанков).
	private void incrementSpanned(BlockPos pos) {
		spannedChunks.computeIfAbsent(ChunkPos.pack(pos), p -> new HashSet<>()).add(pos.immutable());
	}

	// Убирает позицию из списка затронутых чанков (пустой чанк удаляется).
	private void decrementSpanned(BlockPos pos) {
		long chunkPos = ChunkPos.pack(pos);
		Set<BlockPos> set = spannedChunks.get(chunkPos);
		if (set != null) {
			set.remove(pos);
			if (set.size() == 0) {
				spannedChunks.remove(chunkPos);
			}
		}
	}

	public PipeNetworkType getType() {
		return type;
	}

	public Set<Direction> getNodeLinks(BlockPos pos) {
		return new HashSet<>(links.get(pos));
	}
}

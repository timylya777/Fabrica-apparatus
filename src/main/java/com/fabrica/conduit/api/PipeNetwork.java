package com.fabrica.conduit.api;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Отвечает за абстрактную сеть труб одного типа: хранит узлы (PipeNetworkNode)
 * по позициям, разбивку по чанкам (nodesByChunk), кэш «тикающихся» узлов
 * (тики в активных чанках), тикает сеть, даёт возможность слияния сетей
 * (merge) и очистки при удалении (onRemove). Узел может быть null в карте —
 * так помечается выгруженная позиция. Подклассы (например, электрическая или
 * предметная сеть) реализуют тик и данные сети (data).
 */
/**
 * A pipe network. It is very important that you create a new empty data object
 * if your constructor was passed null.
 */
public abstract class PipeNetwork {
	protected int id;
	public PipeNetworkManager manager;
	public PipeNetworkData data;
	private final Map<BlockPos, @Nullable PipeNetworkNode> nodes = new HashMap<>();
	private final Map<Long, Map<BlockPos, @Nullable PipeNetworkNode>> nodesByChunk = new HashMap<>();
	private final List<PosNode> tickingNodesCache = new ArrayList<>();
	boolean tickingCacheValid = false;

	public PipeNetwork(int id, PipeNetworkData data) {
		this.id = id;
		this.data = data;
	}

	/**
	 * <b>Only access nodes that are ticking, for example with {@link #iterateTickingNodes}!</b>
	 */
	// Главный тик сети (каждый тик сервера): подклассы обрабатывают здесь
	// перенос предметов/жидкостей/энергии. Доступ к узлам — только через
	// iterateTickingNodes (тикаются лишь узлы в активных чанках).
	public void tick(ServerLevel world) {}

	/**
	 * Allow merging networks when the player explicitly requests to do so.
	 *
	 * @return null if there can be no merge, or the new pipe network data should there be a merge.
	 */
	// Слияние с другой сетью при явном запросе (например, объединение
	// электрических сетей через трансформатор): возвращает новые объединённые
	// данные или null, если слияние невозможно.
	@Nullable
	public PipeNetworkData merge(PipeNetwork other) {
		return null;
	}

	/**
	 * Called when the network is removed from the world. At that point, all the nodes are already gone.
	 */
	public void onRemove() {}

	@Nullable
	public PipeNetworkNode getNode(BlockPos pos) {
		return this.nodes.get(pos);
	}

	// Устанавливает/заменяет узел на позиции (и во внутренней разбивке по
	// чанкам); null означает выгруженный узел.
	public void setNode(BlockPos pos, @Nullable PipeNetworkNode node) {
		this.nodes.put(pos.immutable(), node);
		this.nodesByChunk.computeIfAbsent(ChunkPos.pack(pos), p -> new HashMap<>()).put(pos.immutable(), node);
	}

	// Удаляет узел с позиции, убирая и запись в разбивке по чанкам.
	public void removeNode(BlockPos pos) {
		this.nodes.remove(pos);
		long chunk = ChunkPos.pack(pos);
		Map<BlockPos, PipeNetworkNode> map = nodesByChunk.get(chunk);
		if (map != null) {
			map.remove(pos);
			if (map.size() == 0) {
				nodesByChunk.remove(chunk);
			}
		}
	}

	public Map<BlockPos, @Nullable PipeNetworkNode> getRawNodeMap() {
		return Collections.unmodifiableMap(this.nodes);
	}

	// Перечисляет узлы в активных (тикающихся) чанках; результат кэшируется,
	// кэш инвалидируется при изменении сети или чанков (tickingCacheValid).
	public Collection<PosNode> iterateTickingNodes() {
		if (!tickingCacheValid) {
			tickingNodesCache.clear();
			for (var chunkEntry : this.nodesByChunk.entrySet()) {
				if (manager.isChunkTicking(chunkEntry.getKey())) {
					for (var entry : chunkEntry.getValue().entrySet()) {
						var node = entry.getValue();
						if (node != null) {
							tickingNodesCache.add(new PosNode(entry.getKey(), node));
						}
					}
				}
			}
			tickingCacheValid = true;
		}
		return tickingNodesCache;
	}

	public static class PosNode {
		private final BlockPos pos;
		private final PipeNetworkNode node;

		public PosNode(BlockPos pos, PipeNetworkNode node) {
			this.pos = pos;
			this.node = node;
		}

		public BlockPos getPos() {
			return pos;
		}

		public PipeNetworkNode getNode() {
			return node;
		}
	}
}

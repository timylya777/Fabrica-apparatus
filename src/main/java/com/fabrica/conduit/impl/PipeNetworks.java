package com.fabrica.conduit.impl;

import com.fabrica.FabricaMod;
import com.fabrica.conduit.api.PipeNetworkManager;
import com.fabrica.conduit.api.PipeNetworkType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Отвечает за все сети труб одного измерения (SavedData, сохраняется вместе
 * с миром): хранит менеджеры сетей (по одному PipeNetworkManager на тип трубы),
 * откладывает загрузку узлов в выгруженных чанках (loadPipesByChunk) и каждый
 * тик сервера загружает узлы в активных чанках и тикает все сети.
 * Сам граф сетей не сериализуется — он перестраивается из узлов, сохранённых
 * в каждом PipeBlockEntity.
 */
public class PipeNetworks extends SavedData {
	private static final Codec<PipeNetworks> CODEC = MapCodec.unitCodec(() -> new PipeNetworks(new HashMap<>()));
	private static final SavedDataType<PipeNetworks> TYPE = new SavedDataType<>(
			FabricaMod.id("pipe_networks"),
			() -> new PipeNetworks(new HashMap<>()),
			CODEC,
			DataFixTypes.LEVEL);

	private final Map<PipeNetworkType, PipeNetworkManager> managers;
	private final Map<Long, List<Runnable>> loadPipesByChunk = new HashMap<>();

	public PipeNetworks(Map<PipeNetworkType, PipeNetworkManager> managers) {
		this.managers = managers;
		for (PipeNetworkType type : PipeNetworkType.getTypes().values()) {
			if (!managers.containsKey(type)) {
				managers.put(type, new PipeNetworkManager(type));
			}
		}
	}

	// Возвращает менеджер сетей для типа трубы, создавая его при первом запросе.
	public PipeNetworkManager getManager(PipeNetworkType type) {
		return managers.computeIfAbsent(type, PipeNetworkManager::new);
	}

	@Nullable
	public PipeNetworkManager getOptionalManager(PipeNetworkType type) {
		return managers.get(type);
	}

	// Достаёт (или создаёт) компонент сетей для серверного измерения.
	public static PipeNetworks get(ServerLevel world) {
		return world.getDataStorage().computeIfAbsent(TYPE);
	}

	// Откладывает загрузку узлов трубы до конца тика: узлы загружаются только
	// в тикающихся (активных) чанках, чтобы избежать проблем с генерацией чанков.
	// Вызывается из PipeBlockEntity.clearRemoved().
	public static void scheduleLoadPipe(Level world, PipeBlockEntity pipe) {
		if (world instanceof ServerLevel sw) {
			if (!sw.getServer().isSameThread()) {
				throw new IllegalStateException("Can only load pipe on server from the server thread.");
			}

			PipeNetworks.get(sw).loadPipesByChunk.computeIfAbsent(ChunkPos.pack(pipe.getBlockPos()), chunk -> new ArrayList<>())
					.add(pipe::loadPipes);
		}
	}

	// The network graph is not serialized; it is rebuilt from the pipes saved in
	// each PipeBlockEntity.

	// Регистрирует обработчик конца тика уровня: 1) загружает отложенные узлы
	// в активных чанках, 2) тикает все менеджеры сетей.
	public static void init() {
		ServerTickEvents.END_LEVEL_TICK.register(serverWorld -> {
			PipeNetworks networks = PipeNetworks.get(serverWorld);

			// Load pipes
			var it = networks.loadPipesByChunk.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<Long, List<Runnable>> chunkEntry = it.next();
				if (isChunkTicking(serverWorld, chunkEntry.getKey())) {
					chunkEntry.getValue().forEach(Runnable::run);
					it.remove();
				}
			}

			// Tick networks
			for (PipeNetworkManager manager : networks.managers.values()) {
				manager.tickNetworks(serverWorld);
			}
		});
	}

	private static boolean isChunkTicking(ServerLevel world, long chunkPos) {
		return world.getChunkSource().isPositionTicking(chunkPos);
	}
}

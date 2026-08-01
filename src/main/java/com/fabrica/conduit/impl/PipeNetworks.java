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

	public PipeNetworkManager getManager(PipeNetworkType type) {
		return managers.computeIfAbsent(type, PipeNetworkManager::new);
	}

	@Nullable
	public PipeNetworkManager getOptionalManager(PipeNetworkType type) {
		return managers.get(type);
	}

	public static PipeNetworks get(ServerLevel world) {
		return world.getDataStorage().computeIfAbsent(TYPE);
	}

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

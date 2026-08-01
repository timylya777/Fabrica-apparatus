package com.fabrica.conduit.item;

import com.fabrica.conduit.api.PipeEndpointType;
import com.fabrica.conduit.api.PipeNetworkNode;
import com.fabrica.conduit.api.PipeNetworkType;
import com.fabrica.conduit.impl.PipeBlockEntity;
import com.fabrica.conduit.impl.PipeNetworks;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import static com.fabrica.conduit.api.PipeEndpointType.BLOCK_IN;
import static com.fabrica.conduit.api.PipeEndpointType.BLOCK_IN_OUT;
import static com.fabrica.conduit.api.PipeEndpointType.BLOCK_OUT;
import static com.fabrica.conduit.api.PipeEndpointType.PIPE;

public class ItemNetworkNode extends PipeNetworkNode {
	public static final int SLOTS = 4;
	final List<ItemConnection> connections = new ArrayList<>();
	int inactiveTicks = 0;

	@Override
	public void updateConnections(Level world, BlockPos pos) {
		// Remove the connection to the outside world if a connection to another pipe is made.
		var levelNetworks = PipeNetworks.get((ServerLevel) world);
		connections.removeIf(connection -> {
			for (var type : PipeNetworkType.getTypes().values()) {
				var manager = levelNetworks.getOptionalManager(type);
				if (manager != null && manager.hasLink(pos, connection.direction)) {
					return true;
				}
			}
			return false;
		});
	}

	private boolean canConnect(Level world, BlockPos pos, Direction direction) {
		BlockPos adjPos = pos.relative(direction);
		return ItemStorage.SIDED.find(world, adjPos, direction.getOpposite()) != null;
	}

	@Override
	public @Nullable PipeEndpointType[] getConnections(BlockPos pos) {
		PipeEndpointType[] connections = new PipeEndpointType[6];
		for (Direction direction : network.manager.getNodeLinks(pos)) {
			connections[direction.get3DDataValue()] = PIPE;
		}
		for (ItemConnection connection : this.connections) {
			connections[connection.direction.get3DDataValue()] = connection.type;
		}
		return connections;
	}

	@Override
	public void removeConnection(Level world, BlockPos pos, Direction direction) {
		// Cycle if it exists
		for (int i = 0; i < connections.size(); i++) {
			ItemConnection conn = connections.get(i);
			if (conn.direction == direction) {
				if (conn.type == BLOCK_IN)
					conn.type = BLOCK_IN_OUT;
				else if (conn.type == BLOCK_IN_OUT)
					conn.type = BLOCK_OUT;
				else
					connections.remove(i);
				return;
			}
		}
	}

	@Override
	public void addConnection(PipeBlockEntity pipe, Player player, Level world, BlockPos pos, Direction direction) {
		// Refuse if it already exists
		for (ItemConnection connection : connections) {
			if (connection.direction == direction) {
				return;
			}
		}
		// Otherwise try to connect
		if (canConnect(world, pos, direction)) {
			connections.add(new ItemConnection(direction, BLOCK_IN, 0, -10));
		}
	}

	@Override
	public void save(ValueOutput output) {
		for (ItemConnection connection : connections) {
			net.minecraft.nbt.CompoundTag connectionTag = new net.minecraft.nbt.CompoundTag();
			connectionTag.putByte("connections", (byte) encodeConnectionType(connection.type));
			connectionTag.putBoolean("whitelist", connection.whitelist);
			connectionTag.putInt("insertPriority", connection.insertPriority);
			connectionTag.putInt("extractPriority", connection.extractPriority);
			for (int i = 0; i < SLOTS; i++) {
				output.store(connection.direction.toString() + "_" + i, ItemVariant.CODEC, connection.stacks[i]);
			}
			output.store(connection.direction.toString(), net.minecraft.nbt.CompoundTag.CODEC, connectionTag);
		}
		output.putInt("inactiveTicks", inactiveTicks);
	}

	@Override
	public void read(ValueInput input) {
		var keySet = input.keySet();
		for (Direction direction : Direction.values()) {
			if (keySet.contains(direction.toString())) {
				net.minecraft.nbt.CompoundTag connectionTag = input.read(direction.toString(), net.minecraft.nbt.CompoundTag.CODEC).orElseThrow();
				int insertPriority = connectionTag.getIntOr("insertPriority", 0);
				int extractPriority = connectionTag.getIntOr("extractPriority", 0);
				ItemConnection connection = new ItemConnection(direction, decodeConnectionType(connectionTag.getByteOr("connections", (byte) 0)),
						insertPriority, extractPriority);
				connection.whitelist = connectionTag.getBooleanOr("whitelist", false);
				for (int i = 0; i < SLOTS; i++) {
					connection.stacks[i] = input.read(direction.toString() + "_" + i, ItemVariant.CODEC).orElse(ItemVariant.blank());
				}
				connection.refreshStacksCache();
				connections.add(connection);
			}
		}
		inactiveTicks = input.getIntOr("inactiveTicks", 0);
	}

	public static PipeEndpointType decodeConnectionType(int i) {
		return i == 0 ? BLOCK_IN : i == 1 ? BLOCK_IN_OUT : BLOCK_OUT;
	}

	public static int encodeConnectionType(PipeEndpointType connection) {
		return connection == BLOCK_IN ? 0 : connection == BLOCK_IN_OUT ? 1 : 2;
	}

	class ItemConnection {
		final Direction direction;
		private PipeEndpointType type;
		boolean whitelist = false;
		int insertPriority, extractPriority;
		final ItemVariant[] stacks = new ItemVariant[SLOTS];
		final Map<Item, List<ItemVariant>> stacksCache = new IdentityHashMap<>();

		private ItemConnection(Direction direction, PipeEndpointType type, int insertPriority, int extractPriority) {
			this.direction = direction;
			this.type = type;
			this.insertPriority = insertPriority;
			this.extractPriority = extractPriority;
			for (int i = 0; i < SLOTS; i++) {
				stacks[i] = ItemVariant.blank();
			}
		}

		private void refreshStacksCache() {
			stacksCache.clear();
			for (ItemVariant stack : stacks) {
				if (!stack.isBlank()) {
					stacksCache.computeIfAbsent(stack.getItem(), k -> new ArrayList<>()).add(stack);
				}
			}
		}

		private boolean isInCache(ItemVariant resource) {
			var list = stacksCache.get(resource.getItem());
			if (list == null) {
				return false;
			}
			for (ItemVariant cachedStack : list) {
				if (resource.equals(cachedStack)) {
					return true;
				}
			}
			return false;
		}

		boolean canInsert() {
			return type == BLOCK_IN || type == BLOCK_IN_OUT;
		}

		boolean canExtract() {
			return type == BLOCK_OUT || type == BLOCK_IN_OUT;
		}

		boolean canMoveThrough(ItemVariant resource) {
			return isInCache(resource) == whitelist;
		}

		int getMoves() {
			return ItemNetwork.BASE_ITEM_PIPE_TRANSFER;
		}
	}
}

package com.fabrica.conduit.fluid;

import com.fabrica.conduit.api.PipeEndpointType;
import com.fabrica.conduit.api.PipeNetworkNode;
import com.fabrica.conduit.api.PipeNetworkType;
import com.fabrica.conduit.impl.PipeBlockEntity;
import com.fabrica.conduit.impl.PipeNetworks;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import static com.fabrica.conduit.api.PipeEndpointType.BLOCK_IN;
import static com.fabrica.conduit.api.PipeEndpointType.BLOCK_IN_OUT;
import static com.fabrica.conduit.api.PipeEndpointType.BLOCK_OUT;
import static com.fabrica.conduit.api.PipeEndpointType.PIPE;

public class FluidNetworkNode extends PipeNetworkNode {
	long amount = 0;
	private final List<FluidConnection> connections = new ArrayList<>();
	private FluidVariant cachedFluid = FluidVariant.blank();

	/**
	 * Add all valid targets to the target list, and pick the fluid for the network
	 * if no fluid is set.
	 */
	void gatherTargetsAndPickFluid(ServerLevel world, BlockPos pos, List<FluidTarget> targets) {
		FluidNetworkData data = (FluidNetworkData) network.data;
		FluidNetwork network = (FluidNetwork) this.network;

		if (amount > network.nodeCapacity) {
			amount = network.nodeCapacity;
		}
		if (amount > 0 && data.fluid().isBlank()) {
			amount = 0;
		}

		for (FluidConnection connection : connections) {
			var storage = getNeighborStorage(world, pos, connection);
			if (data.fluid().isBlank() && connection.canExtract()) {
				// Try to set fluid, will return null if none could be found.
				for (var view : storage.nonEmptyViews()) {
					if (view.getAmount() > 0) {
						network.data = data = new FluidNetworkData(view.getResource());
						break;
					}
				}
			}
			targets.add(new FluidTarget(connection.priority, storage, connection.canExtract(), connection.canInsert()));
		}
	}

	@SuppressWarnings("unchecked")
	private Storage<FluidVariant> getNeighborStorage(ServerLevel world, BlockPos pos, FluidConnection connection) {
		Storage<FluidVariant> storage = FluidStorage.SIDED.find(world, pos.relative(connection.direction), connection.direction.getOpposite());
		return storage != null ? storage : Storage.empty();
	}

	@Override
	public void buildInitialConnections(Level world, BlockPos pos) {
		for (Direction direction : Direction.values()) {
			if (canConnect(world, pos, direction)) {
				connections.add(new FluidConnection(direction, BLOCK_IN_OUT, 0));
			}
		}
	}

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
		// Auto-connect to newly placed tanks, like MI
		for (Direction direction : Direction.values()) {
			boolean connected = connections.stream().anyMatch(connection -> connection.direction == direction);
			if (!connected && canConnect(world, pos, direction)) {
				connections.add(new FluidConnection(direction, BLOCK_IN_OUT, 0));
			}
		}
	}

	@Override
	public @Nullable PipeEndpointType[] getConnections(BlockPos pos) {
		PipeEndpointType[] connections = new PipeEndpointType[6];
		for (Direction direction : network.manager.getNodeLinks(pos)) {
			connections[direction.get3DDataValue()] = PIPE;
		}
		for (FluidConnection connection : this.connections) {
			connections[connection.direction.get3DDataValue()] = connection.type;
		}
		return connections;
	}

	private boolean canConnect(Level world, BlockPos pos, Direction direction) {
		return FluidStorage.SIDED.find(world, pos.relative(direction), direction.getOpposite()) != null;
	}

	@Override
	public void removeConnection(Level world, BlockPos pos, Direction direction) {
		// Remove if it exists
		connections.removeIf(connection -> connection.direction == direction);
	}

	@Override
	public boolean cycleConnectionMode(Level world, BlockPos pos, Direction direction) {
		// Cycle import -> import/export -> export -> import
		for (FluidConnection connection : connections) {
			if (connection.direction == direction) {
				if (connection.type == BLOCK_IN) {
					connection.type = BLOCK_IN_OUT;
				} else if (connection.type == BLOCK_IN_OUT) {
					connection.type = BLOCK_OUT;
				} else {
					connection.type = BLOCK_IN;
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public void addConnection(PipeBlockEntity pipe, Player player, Level world, BlockPos pos, Direction direction) {
		// Refuse if it already exists
		for (FluidConnection connection : connections) {
			if (connection.direction == direction) {
				return;
			}
		}
		// Otherwise try to connect
		if (canConnect(world, pos, direction)) {
			connections.add(new FluidConnection(direction, BLOCK_IN_OUT, 0));
		}
	}

	@Override
	public void save(ValueOutput output) {
		output.putLong("amount", amount);
		for (FluidConnection connection : connections) {
			CompoundTag connectionTag = new CompoundTag();
			connectionTag.putByte("connections", (byte) encodeConnectionType(connection.type));
			connectionTag.putInt("priority", connection.priority);
			output.store(connection.direction.toString(), CompoundTag.CODEC, connectionTag);
		}
	}

	@Override
	public void read(ValueInput input) {
		amount = input.getLongOr("amount", 0);
		var keys = input.keySet();
		for (Direction direction : Direction.values()) {
			if (keys.contains(direction.toString())) {
				CompoundTag connectionTag = input.read(direction.toString(), CompoundTag.CODEC).orElseThrow();
				connections.add(new FluidConnection(direction, decodeConnectionType(connectionTag.getByteOr("connections", (byte) 0)),
						connectionTag.getIntOr("priority", 0)));
			}
		}
	}

	private static PipeEndpointType decodeConnectionType(int i) {
		return i == 0 ? BLOCK_IN : i == 1 ? BLOCK_IN_OUT : BLOCK_OUT;
	}

	private static int encodeConnectionType(PipeEndpointType connection) {
		return connection == BLOCK_IN ? 0 : connection == BLOCK_IN_OUT ? 1 : 2;
	}

	@Override
	public CompoundTag writeCustomData(HolderLookup.Provider registries) {
		CompoundTag tag = new CompoundTag();
		tag.store("fluid", FluidVariant.CODEC, ((FluidNetworkData) network.data).fluid());
		return tag;
	}

	public void afterTick(ServerLevel world, BlockPos pos) {
		FluidVariant networkFluid = ((FluidNetworkData) network.data).fluid();
		if (!networkFluid.equals(cachedFluid)) {
			cachedFluid = networkFluid;
			// Equivalent to calling sync()
			world.getChunkSource().blockChanged(pos);
		}
	}

	private class FluidConnection {
		private final Direction direction;
		private PipeEndpointType type;
		private int priority;

		private FluidConnection(Direction direction, PipeEndpointType type, int priority) {
			this.direction = direction;
			this.type = type;
			this.priority = priority;
		}

		private boolean canInsert() {
			return type == BLOCK_IN || type == BLOCK_IN_OUT;
		}

		private boolean canExtract() {
			return type == BLOCK_OUT || type == BLOCK_IN_OUT;
		}
	}
}

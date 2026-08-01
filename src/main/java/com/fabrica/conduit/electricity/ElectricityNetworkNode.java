package com.fabrica.conduit.electricity;

import com.fabrica.api.energy.CableTier;
import com.fabrica.api.energy.EnergyApiLookup;
import com.fabrica.api.energy.EnergyContainer;
import com.fabrica.api.energy.EnergyTier;
import com.fabrica.conduit.api.PipeEndpointType;
import com.fabrica.conduit.api.PipeNetworkNode;
import com.fabrica.conduit.impl.PipeBlockEntity;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import static com.fabrica.conduit.api.PipeEndpointType.BLOCK;
import static com.fabrica.conduit.api.PipeEndpointType.PIPE;

public class ElectricityNetworkNode extends PipeNetworkNode {
	private List<Direction> connections = new ArrayList<>();
	long eu = 0;

	public void appendAttributes(ServerLevel world, BlockPos pos, CableTier cableTier, List<EnergyContainer> storages) {
		for (Direction direction : connections) {
			EnergyContainer storage = EnergyApiLookup.CONTAINER.find(world, pos.relative(direction), direction.getOpposite());
			if (storage == null || !ElectricityNetwork.canConnect(cableTier, storage)) {
				continue;
			}
			storages.add(storage);
		}
	}

	@Override
	public void buildInitialConnections(Level world, BlockPos pos) {
		for (Direction direction : Direction.values()) {
			if (canConnect(world, pos, direction)) {
				connections.add(direction);
			}
		}
	}

	@Override
	public void updateConnections(Level world, BlockPos pos) {
		// We don't connect by default, so we just have to remove connections that have
		// become unavailable
		for (int i = 0; i < connections.size();) {
			if (canConnect(world, pos, connections.get(i))) {
				i++;
			} else {
				connections.remove(i);
			}
		}
	}

	@Override
	public @Nullable PipeEndpointType[] getConnections(BlockPos pos) {
		PipeEndpointType[] connections = new PipeEndpointType[6];
		for (Direction direction : network.manager.getNodeLinks(pos)) {
			connections[direction.get3DDataValue()] = PIPE;
		}
		for (Direction connection : this.connections) {
			connections[connection.get3DDataValue()] = BLOCK;
		}
		return connections;
	}

	@Override
	public void removeConnection(Level world, BlockPos pos, Direction direction) {
		// Remove if it exists
		for (int i = 0; i < connections.size(); i++) {
			if (connections.get(i) == direction) {
				connections.remove(i);
				return;
			}
		}
	}

	@Override
	public void addConnection(PipeBlockEntity pipe, Player player, Level world, BlockPos pos, Direction direction) {
		// Refuse if it already exists
		for (Direction connection : connections) {
			if (connection == direction) {
				return;
			}
		}
		// Otherwise try to connect
		if (canConnect(world, pos, direction)) {
			connections.add(direction);
		}
	}

	@Override
	public void save(ValueOutput output) {
		int mask = 0;
		for (Direction connection : connections) {
			mask |= 1 << connection.get3DDataValue();
		}
		output.putInt("connections", mask);
		output.putLong("eu", eu);
	}

	@Override
	public void read(ValueInput input) {
		connections = new ArrayList<>();
		int mask = input.getIntOr("connections", 0);
		for (Direction direction : Direction.values()) {
			if ((mask & (1 << direction.get3DDataValue())) != 0) {
				connections.add(direction);
			}
		}
		eu = input.getLongOr("eu", 0);
	}

	private boolean canConnect(Level world, BlockPos pos, Direction direction) {
		var storage = EnergyApiLookup.CONTAINER.find(world, pos.relative(direction), direction.getOpposite());
		return storage != null && ElectricityNetwork.canConnect(((ElectricityNetwork) network).tier, storage);
	}

	public long getMaxTransfer() {
		return ((ElectricityNetwork) network).tier.maxTransfer();
	}
}

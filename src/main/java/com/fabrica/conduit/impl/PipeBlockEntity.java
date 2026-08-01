package com.fabrica.conduit.impl;

import com.fabrica.conduit.FabricaPipes;
import com.fabrica.conduit.api.PipeEndpointType;
import com.fabrica.conduit.api.PipeNetworkData;
import com.fabrica.conduit.api.PipeNetworkManager;
import com.fabrica.conduit.api.PipeNetworkNode;
import com.fabrica.conduit.api.PipeNetworkType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import static net.minecraft.core.Direction.NORTH;

/**
 * The BlockEntity for a pipe.
 */
public class PipeBlockEntity extends BlockEntity {
	private static final Logger LOGGER = LoggerFactory.getLogger(PipeBlockEntity.class);
	private static final int MAX_PIPES = 3;
	private static final VoxelShape[][][] SHAPE_CACHE;
	static final VoxelShape DEFAULT_SHAPE;
	/**
	 * The current collision shape, i.e. the union of the shapes of the pipe parts.
	 */
	VoxelShape currentCollisionShape = Shapes.empty();
	/**
	 * The loaded nodes, server-side only.
	 */
	private final SortedSet<PipeNetworkNode> pipes = new TreeSet<>(Comparator.comparing(PipeNetworkNode::getType));
	/**
	 * The rendered connections, both client-side for rendering and server-side for
	 * bounds check.
	 */
	SortedMap<PipeNetworkType, @Nullable PipeEndpointType[]> connections = new TreeMap<>();
	/**
	 * Extra rendering data
	 */
	SortedMap<PipeNetworkType, CompoundTag> customData = new TreeMap<>();

	// Because we can't access the PipeNetworksComponent in fromTag because the
	// world is null, we defer the node loading.
	private final List<UnloadedPipe> unloadedPipes = new ArrayList<>();
	/**
	 * Set to true in PipeBlock to tell apart unloads and removals.
	 */
	boolean stateReplaced = false;

	public PipeBlockEntity(BlockPos pos, BlockState state) {
		super(FabricaPipes.BLOCK_ENTITY_TYPE, pos, state);
	}

	public void loadPipes() {
		if (level.isClientSide() || unloadedPipes.size() == 0)
			return;

		for (UnloadedPipe unloaded : unloadedPipes) {
			PipeNetworks.get((ServerLevel) level).getManager(unloaded.type()).nodeLoaded(unloaded.node(), worldPosition);
			pipes.add(unloaded.node());
		}
		unloadedPipes.clear();

		// Defer connection update to after the pipes are loaded, because updating the connections might trigger a neighbor update,
		// which would cause a nested loadPipes() call, leading to a concurrent modification exception.
		updateConnections();
	}

	void updateConnections() {
		loadPipes();
		for (PipeNetworkNode pipe : pipes) {
			pipe.updateConnections(level, worldPosition);
		}
		onConnectionsChanged();
	}

	public SortedSet<PipeNetworkNode> getNodes() {
		loadPipes();
		return Collections.unmodifiableSortedSet(pipes);
	}

	/**
	 * The rendered connections, ordered by pipe type. Used by the client-side
	 * renderer.
	 */
	public SortedMap<PipeNetworkType, @Nullable PipeEndpointType[]> getRenderedConnections() {
		return connections;
	}

	/**
	 * The extra rendering data (e.g. the contained fluid), ordered by pipe type.
	 * Used by the client-side renderer.
	 */
	public SortedMap<PipeNetworkType, CompoundTag> getCustomData() {
		return customData;
	}

	/**
	 * Check if it's possible to add a pipe.
	 *
	 * @param type The type to add.
	 * @return True if the pipe can be added, false otherwise.
	 */
	public boolean canAddPipe(PipeNetworkType type) {
		loadPipes();
		if (level.isClientSide()) {
			return connections.size() < MAX_PIPES && !connections.containsKey(type);
		} else {
			if (pipes.size() == MAX_PIPES)
				return false;
			for (PipeNetworkNode pipe : pipes) {
				if (pipe.getType() == type)
					return false;
			}
			return true;
		}
	}

	/**
	 * Add a pipe type. Will not do anything if the pipe couldn't be added.
	 *
	 * @param type The type to add.
	 */
	public void addPipe(PipeNetworkType type, PipeNetworkData data) {
		if (!canAddPipe(type))
			return;

		PipeNetworkNode node = type.getNodeCtor().get();
		PipeNetworkManager manager = PipeNetworks.get((ServerLevel) level).getManager(type);
		manager.addNode(node, worldPosition, data);
		for (Direction direction : Direction.values()) {
			manager.addLink(worldPosition, direction, false);
		}
		pipes.add(node);
		node.buildInitialConnections(level, worldPosition);
		updateConnections();
	}

	/**
	 * Remove a pipe type.
	 *
	 * @param type The type to remove.
	 */
	public void removePipeAndDropContainedItems(PipeNetworkType type) {
		loadPipes();
		PipeNetworkNode removedPipe = null;
		for (PipeNetworkNode pipe : pipes) {
			if (pipe.getType() == type) {
				removedPipe = pipe;
				break;
			}
		}
		if (removedPipe == null) {
			throw new IllegalArgumentException("Can't remove type " + type.getIdentifier() + " from BlockEntity at pos " + worldPosition);
		}
		pipes.remove(removedPipe);
		removedPipe.getManager().removeNode(worldPosition);
		onConnectionsChanged();

		// Drop items
		List<ItemStack> droppedStacks = new ArrayList<>();
		removedPipe.appendDroppedStacks(droppedStacks);
		for (ItemStack droppedStack : droppedStacks) {
			level.addFreshEntity(new ItemEntity(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), droppedStack));
		}
	}

	/**
	 * Remove a pipe connection.
	 */
	public void removeConnection(PipeNetworkType type, Direction direction) {
		for (PipeNetworkNode pipe : pipes) {
			if (pipe.getType() == type) {
				pipe.removeConnection(level, worldPosition, direction);
				pipe.getManager().removeLink(worldPosition, direction);
				onConnectionsChanged();
				return;
			}
		}
	}

	/**
	 * Add a pipe connection.
	 */
	public void addConnection(Player player, PipeNetworkType type, Direction direction) {
		for (PipeNetworkNode pipe : pipes) {
			if (pipe.getType() == type) {
				pipe.addConnection(this, player, level, worldPosition, direction);
				pipe.getManager().addLink(worldPosition, direction, true);
				pipe.updateConnections(level, worldPosition);

				onConnectionsChanged();
				return;
			}
		}
	}

	public boolean customUse(PipeVoxelShape shape, Player player, net.minecraft.world.InteractionHand hand) {
		for (var node : pipes) {
			if (node.getType() == shape.type) {
				return node.customUse(this, player, hand, shape.direction);
			}
		}
		return false;
	}

	@Override
	public void setRemoved() {
		if (stateReplaced) {
			loadPipes();
			for (PipeNetworkNode pipe : pipes) {
				pipe.getManager().removeNode(worldPosition);
			}
			// Don't clear pipes, otherwise they can't be dropped when broken by hand.
		} else {
			for (PipeNetworkNode pipe : pipes) {
				pipe.onUnload();
				pipe.getManager().nodeUnloaded(pipe, worldPosition);
			}
		}

		super.setRemoved();
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		int i = 0;
		for (PipeNetworkNode pipe : pipes) {
			output.putString("pipe_type_" + i, pipe.getType().getIdentifier().toString());
			pipe.save(output.child("pipe_data_" + i));
			i++;
		}
		for (UnloadedPipe entry : unloadedPipes) {
			output.putString("pipe_type_" + i, entry.type().getIdentifier().toString());
			entry.node().save(output.child("pipe_data_" + i));
			i++;
		}
	}

	@Override
	public void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		var keySet = input.keySet();

		if (!keySet.contains("pipes")) {
			pipes.clear();

			int i = 0;
			while (keySet.contains("pipe_type_" + i)) {
				Identifier typeId = Identifier.parse(input.getString("pipe_type_" + i).orElseThrow());
				PipeNetworkType type = PipeNetworkType.get(typeId);
				PipeNetworkNode node = type.getNodeCtor().get();
				node.read(input.child("pipe_data_" + i).orElseThrow());
				unloadedPipes.add(new UnloadedPipe(type, node));
				i++;
			}
		} else {
			connections.clear();
			customData.clear();
			var pipesTag = input.childOrEmpty("pipes");
			for (String key : pipesTag.keySet()) {
				var nodeTag = pipesTag.read(key, CompoundTag.CODEC).orElseThrow();
				PipeNetworkType type = PipeNetworkType.get(Identifier.parse(key));
				connections.put(type, decodeConnections(nodeTag.getByteArray("connections").orElseThrow()));
				customData.put(type, nodeTag.getCompoundOrEmpty("custom"));
			}
			rebuildCollisionShape();
		}
	}

	private static PipeEndpointType[] decodeConnections(byte[] bytes) {
		PipeEndpointType[] connections = new PipeEndpointType[6];
		for (int i = 0; i < 6; i++) {
			connections[i] = PipeEndpointType.byId(bytes[i]);
		}
		return connections;
	}

	private static byte[] encodeConnections(@Nullable PipeEndpointType[] connections) {
		byte[] bytes = new byte[6];
		if (connections != null) {
			for (int i = 0; i < 6; i++) {
				var conn = connections[i];
				bytes[i] = conn == null ? 127 : (byte) conn.getId();
			}
		}
		return bytes;
	}

	@Override
	public void clearRemoved() {
		super.clearRemoved();
		PipeNetworks.scheduleLoadPipe(level, this);
	}

	public void onConnectionsChanged() {
		// Update connections on the server side, we need them for the bounding box.
		Map<PipeNetworkType, @Nullable PipeEndpointType[]> oldRendererConnections = connections;
		connections = new TreeMap<>();
		for (PipeNetworkNode pipe : pipes) {
			connections.put(pipe.getType(), pipe.getConnections(worldPosition));
		}
		// Then send the update to the client if there was a change.
		if (!connections.equals(oldRendererConnections)) {
			rebuildCollisionShape();
			sync();
		}
		setChanged();
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(this.problemPath(), LOGGER)) {
			TagValueOutput output = TagValueOutput.createWithContext(reporter, registries);
			loadPipes();
			var pipesTag = output.child("pipes");
			for (PipeNetworkNode pipe : pipes) {
				CompoundTag nodeTag = new CompoundTag();
				nodeTag.put("custom", pipe.writeCustomData(registries));
				nodeTag.putByteArray("connections", encodeConnections(pipe.getConnections(worldPosition)));
				pipesTag.store(pipe.getType().getIdentifier().toString(), CompoundTag.CODEC, nodeTag);
			}
			return output.buildResult();
		}
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	public void sync() {
		if (level == null || level.isClientSide() || !(level instanceof ServerLevel serverLevel)) return;
		Packet<ClientGamePacketListener> packet = getUpdatePacket();
		if (packet != null) {
			for (Player player : serverLevel.getPlayers(p -> p.blockPosition().closerThan(worldPosition, 64))) {
				if (player instanceof ServerPlayer serverPlayer) {
					serverPlayer.connection.send(packet);
				}
			}
		}
	}

	/**
	 * Get the currently visible shapes.
	 */
	public Collection<PipeVoxelShape> getPartShapes() {
		Collection<PipeVoxelShape> shapes = new ArrayList<>();

		PipeEndpointType[][] renderedConnections = new PipeEndpointType[connections.size()][];
		PipeNetworkType[] types = new PipeNetworkType[connections.size()];
		int slot = 0;
		for (Map.Entry<PipeNetworkType, PipeEndpointType[]> connections : this.connections.entrySet()) {
			renderedConnections[slot] = connections.getValue();
			types[slot] = connections.getKey();
			slot++;
		}
		for (slot = 0; slot < renderedConnections.length; ++slot) {
			// Center connector
			shapes.add(new PipeVoxelShape(SHAPE_CACHE[slot][NORTH.get3DDataValue()][0], types[slot], null, false));

			// Side connectors
			for (Direction direction : Direction.values()) {
				int connectionType = PipePartBuilder.getRenderType(slot, direction, renderedConnections);
				if (connectionType != 0) {
					PipeEndpointType connType = renderedConnections[slot][direction.get3DDataValue()];
					boolean opensGui = connType != null && connType != PipeEndpointType.PIPE && types[slot].opensGui();
					shapes.add(new PipeVoxelShape(SHAPE_CACHE[slot][direction.get3DDataValue()][connectionType], types[slot], direction, opensGui));
				}
			}
		}

		return shapes;
	}

	private void rebuildCollisionShape() {
		currentCollisionShape = getPartShapes().stream().map(vs -> vs.shape).reduce(Shapes.empty(), Shapes::or);
		currentCollisionShape = currentCollisionShape.optimize();
	}

	static {
		// Note: the centor connector are at connectionType 0.
		SHAPE_CACHE = new VoxelShape[3][6][5];
		for (int slot = 0; slot < 3; slot++) {
			for (Direction direction : Direction.values()) {
				int connectionTypes = slot == 0 ? 2 : slot == 1 ? 4 : 5;
				for (int connectionType = 0; connectionType < connectionTypes; connectionType++) {
					PipeShapeBuilder psb = new PipeShapeBuilder(PipePartBuilder.getSlotPos(slot), direction);
					if (connectionType == 0)
						psb.centerConnector();
					else if (connectionType == 1)
						psb.straightLine(false, false);
					else if (connectionType == 2)
						psb.shortBend(false, false);
					else if (connectionType == 3)
						psb.farShortBend(false, false);
					else
						psb.longBend(false, false);
					SHAPE_CACHE[slot][direction.get3DDataValue()][connectionType] = psb.getShape();
				}
			}
		}

		DEFAULT_SHAPE = SHAPE_CACHE[0][0][0];
	}

	/**
	 * A pipe that couldn't be loaded in loadAdditional because the world was
	 * null; the node is loaded once the block entity is in a world.
	 */
	private record UnloadedPipe(PipeNetworkType type, PipeNetworkNode node) {
	}
}

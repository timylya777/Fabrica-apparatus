package com.fabrica.conduit.api;

import com.fabrica.conduit.impl.PipeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class PipeNetworkNode {
	@Nullable
	protected PipeNetwork network;

	public void updateConnections(Level world, BlockPos pos) {}

	public void buildInitialConnections(Level world, BlockPos pos) {}

	/**
	 * Get connections. Must return a size 6 array containing the 6 connections in
	 * the Direction order. Null can be used to render no connection.
	 */
	public abstract @Nullable PipeEndpointType[] getConnections(BlockPos pos);

	public abstract void removeConnection(Level world, BlockPos pos, Direction direction);

	public abstract void addConnection(PipeBlockEntity pipe, Player player, Level world, BlockPos pos, Direction direction);

	/**
	 * Cycle the import/export mode of the connection to a machine on the given
	 * side. Returns true if the mode was changed.
	 */
	public boolean cycleConnectionMode(Level world, BlockPos pos, Direction direction) {
		return false;
	}

	public abstract void save(ValueOutput output);

	public abstract void read(ValueInput input);

	public final PipeNetworkType getType() {
		return network.manager.getType();
	}

	public final PipeNetworkManager getManager() {
		return network.manager;
	}

	public CompoundTag writeCustomData(HolderLookup.Provider registries) {
		return new CompoundTag();
	}

	public void appendDroppedStacks(List<ItemStack> droppedStacks) {}

	/**
	 * Return true if something was done.
	 */
	public boolean customUse(PipeBlockEntity pipe, Player player, InteractionHand hand, @Nullable Direction hitDirection) {
		return false;
	}

	public void onUnload() {}
}

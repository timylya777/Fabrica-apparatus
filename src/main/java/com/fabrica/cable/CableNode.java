package com.fabrica.cable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class CableNode {
    @Nullable
    protected Network network;
    protected final List<Direction> connections;

    protected CableNode(List<Direction> connections) {
        this.connections = new ArrayList<>(connections);
    }

    public List<Direction> getConnections() {
        return connections;
    }

    public @Nullable Network getNetwork() {
        return network;
    }

    public void setNetwork(@Nullable Network network) {
        this.network = network;
    }

    public int getNetworkId() {
        return network != null ? network.getId() : -1;
    }

    public abstract ConnectionType getConnectionType(Direction dir);

    public abstract void updateConnections(Level level, BlockPos pos);

    public abstract CompoundTag save();

    public abstract void load(CompoundTag tag);
}

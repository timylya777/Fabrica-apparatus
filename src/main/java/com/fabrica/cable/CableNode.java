package com.fabrica.cable;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;

public abstract class CableNode {
    protected final List<Direction> connections;

    protected CableNode(List<Direction> connections) {
        this.connections = connections;
    }

    public List<Direction> getConnections() {
        return connections;
    }

    public abstract void updateConnections(Level level, BlockEntity be);

    public abstract CompoundTag save();

    public abstract void load(CompoundTag tag);
}

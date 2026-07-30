package com.fabrica.cable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class Network {
    protected final int id;
    protected final Map<BlockPos, CableNode> nodes = new HashMap<>();

    public Network(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void addNode(BlockPos pos, CableNode node) {
        nodes.put(pos, node);
        node.setNetwork(this);
    }

    public @Nullable CableNode removeNode(BlockPos pos) {
        CableNode node = nodes.remove(pos);
        if (node != null) {
            node.setNetwork(null);
        }
        return node;
    }

    public @Nullable CableNode getNode(BlockPos pos) {
        return nodes.get(pos);
    }

    public Collection<CableNode> getAllNodes() {
        return nodes.values();
    }

    public Set<BlockPos> getPositions() {
        return nodes.keySet();
    }

    public boolean containsPos(BlockPos pos) {
        return nodes.containsKey(pos);
    }

    public int nodeCount() {
        return nodes.size();
    }

    public boolean canMerge(Network other) {
        return other != null && other != this && other.getClass() == this.getClass();
    }

    public void absorb(Network other) {
        for (Map.Entry<BlockPos, CableNode> entry : other.nodes.entrySet()) {
            addNode(entry.getKey(), entry.getValue());
        }
        other.nodes.clear();
    }

    public abstract void tick(ServerLevel level);

    public abstract CompoundTag save();

    public abstract void load(CompoundTag tag);
}

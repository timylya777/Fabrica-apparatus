package com.fabrica.energy;

import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;

import team.reborn.energy.api.EnergyStorage;

import java.util.*;

public class APNetwork {
    final Set<BlockPos> nodes = new HashSet<>();
    final List<APProvider> providers = new ArrayList<>();
    final List<APConsumer> consumers = new ArrayList<>();
    private boolean dirty = false;

    public void addNode(BlockPos pos, APNode node) {
        nodes.add(pos);
        if (node instanceof APProvider p) providers.add(p);
        if (node instanceof APConsumer c) consumers.add(c);
    }

    public void removeNode(BlockPos pos) {
        nodes.remove(pos);
        dirty = true;
    }

    public void markDirty() { this.dirty = true; }
    public boolean isDirty() { return dirty; }
    public Set<BlockPos> getNodes() { return nodes; }
    public void clear() { nodes.clear(); providers.clear(); consumers.clear(); }

    public void tick() {
        if (providers.isEmpty() || consumers.isEmpty()) return;

        long totalAvailable = 0;
        for (APProvider provider : providers) {
            EnergyStorage storage = provider.getStorage();
            try (Transaction t = Transaction.openOuter()) {
                totalAvailable += storage.extract(Long.MAX_VALUE, t);
            }
        }
        if (totalAvailable <= 0) return;

        long remaining = totalAvailable;
        for (APConsumer consumer : consumers) {
            if (remaining <= 0) break;
            EnergyStorage storage = consumer.getStorage();
            try (Transaction t = Transaction.openOuter()) {
                long maxCanReceive = storage.insert(Long.MAX_VALUE, t);
                if (maxCanReceive > 0) {
                    long toGive = Math.min(remaining, maxCanReceive);
                    long actuallyGiven = storage.insert(toGive, t);
                    t.commit();
                    remaining -= actuallyGiven;
                }
            }
        }

        long toExtract = totalAvailable - remaining;
        for (APProvider provider : providers) {
            if (toExtract <= 0) break;
            EnergyStorage storage = provider.getStorage();
            try (Transaction t = Transaction.openOuter()) {
                long extracted = storage.extract(toExtract, t);
                t.commit();
                toExtract -= extracted;
            }
        }
    }
}

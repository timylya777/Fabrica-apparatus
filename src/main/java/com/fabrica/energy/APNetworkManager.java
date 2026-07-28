package com.fabrica.energy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.*;

public class APNetworkManager {
    private final Map<BlockPos, APNetwork> nodeToNetwork = new HashMap<>();
    private final List<APNetwork> networks = new ArrayList<>();
    private final Level level;

    public APNetworkManager(Level level) { this.level = level; }

    public void onBlockAdded(BlockPos pos, APNode node) {
        Set<APNetwork> adjacent = new HashSet<>();
        for (Direction dir : Direction.values()) {
            APNetwork net = nodeToNetwork.get(pos.relative(dir));
            if (net != null) adjacent.add(net);
        }

        APNetwork newNetwork;
        if (adjacent.isEmpty()) {
            newNetwork = new APNetwork();
            networks.add(newNetwork);
        } else {
            Iterator<APNetwork> it = adjacent.iterator();
            newNetwork = it.next();
            while (it.hasNext()) {
                APNetwork toMerge = it.next();
                newNetwork.getNodes().addAll(toMerge.getNodes());
                newNetwork.providers.addAll(toMerge.providers);
                newNetwork.consumers.addAll(toMerge.consumers);
                networks.remove(toMerge);
            }
        }

        newNetwork.addNode(pos, node);
        nodeToNetwork.put(pos, newNetwork);
    }

    public void onBlockRemoved(BlockPos pos) {
        APNetwork network = nodeToNetwork.remove(pos);
        if (network != null) {
            network.removeNode(pos);
        }
    }

    public void tick() {
        List<APNetwork> toRebuild = networks.stream().filter(APNetwork::isDirty).toList();
        for (APNetwork oldNetwork : toRebuild) {
            rebuildNetwork(oldNetwork);
        }

        for (APNetwork network : networks) {
            network.tick();
        }
    }

    private void rebuildNetwork(APNetwork oldNetwork) {
        Set<BlockPos> remaining = new HashSet<>(oldNetwork.getNodes());
        oldNetwork.clear();

        while (!remaining.isEmpty()) {
            BlockPos start = remaining.iterator().next();
            APNetwork newNetwork = new APNetwork();
            Queue<BlockPos> queue = new LinkedList<>();
            queue.add(start);
            remaining.remove(start);

            while (!queue.isEmpty()) {
                BlockPos current = queue.poll();
                BlockEntity be = level.getBlockEntity(current);

                if (be instanceof APNode node) {
                    newNetwork.addNode(current, node);
                    nodeToNetwork.put(current, newNetwork);

                    for (Direction dir : Direction.values()) {
                        BlockPos neighbor = current.relative(dir);
                        if (remaining.contains(neighbor)) {
                            remaining.remove(neighbor);
                            queue.add(neighbor);
                        }
                    }
                }
            }
            networks.add(newNetwork);
        }
        networks.remove(oldNetwork);
    }

    public APNetwork getNetwork(BlockPos pos) { return nodeToNetwork.get(pos); }
}

package com.fabrica.energy;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import java.util.*;

public class APNetworkManager {
    private final Map<BlockPos, APNetwork> nodeToNetwork = new HashMap<>();
    private final List<APNetwork> networks = new ArrayList<>();
    private final World world;

    public APNetworkManager(World world) { this.world = world; }

    // Вызывать при размещении блока (Block.onBlockAdded или BlockEntity initialization)
    public void onBlockAdded(BlockPos pos, APNode node) {
        Set<APNetwork> adjacent = new HashSet<>();
        for (Direction dir : Direction.values()) {
            APNetwork net = nodeToNetwork.get(pos.offset(dir));
            if (net != null) adjacent.add(net);
        }

        APNetwork newNetwork;
        if (adjacent.isEmpty()) {
            newNetwork = new APNetwork();
            networks.add(newNetwork);
        } else {
            // Сливаем все соседние сети в одну
            Iterator<APNetwork> it = adjacent.iterator();
            newNetwork = it.next();
            while (it.hasNext()) {
                APNetwork toMerge = it.next();
                newNetwork.nodes.addAll(toMerge.nodes);
                newNetwork.providers.addAll(toMerge.providers);
                newNetwork.consumers.addAll(toMerge.consumers);
                networks.remove(toMerge);
            }
        }

        newNetwork.addNode(pos, node);
        nodeToNetwork.put(pos, newNetwork);
    }

    // Вызывать при разрушении блока (Block.onStateReplaced)
    public void onBlockRemoved(BlockPos pos) {
        APNetwork network = nodeToNetwork.remove(pos);
        if (network != null) {
            network.removeNode(pos);
        }
    }

    // Вызывать один раз за тик для всего мира (через ServerTickEvents.END_TICK)
    public void tick() {
        // 1. Пересобираем разорванные сети (только если что-то сломали)
        List<APNetwork> toRebuild = networks.stream().filter(APNetwork::isDirty).toList();
        for (APNetwork oldNetwork : toRebuild) {
            rebuildNetwork(oldNetwork);
        }

        // 2. Тикаем все активные сети (мгновенное распределение без лагов)
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

            // BFS обход для поиска всех связанных блоков
            while (!queue.isEmpty()) {
                BlockPos current = queue.poll();
                BlockEntity be = world.getBlockEntity(current);
                
                if (be instanceof APNode node) {
                    newNetwork.addNode(current, node);
                    nodeToNetwork.put(current, newNetwork); // Обновляем карту

                    for (Direction dir : Direction.values()) {
                        BlockPos neighbor = current.offset(dir);
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
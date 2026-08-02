package com.fabrica.me;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class MeNetwork {

    public static MeStorage getStorage(Level level, BlockPos origin) {
        List<MeStorage> storages = new ArrayList<>();
        Set<BlockPos> visited = new HashSet<>();
        Deque<BlockPos> queue = new ArrayDeque<>();
        queue.add(origin);
        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            if (!visited.add(current)) {
                continue;
            }
            for (Direction direction : Direction.values()) {
                BlockPos neighbor = current.relative(direction);
                if (visited.contains(neighbor) || !level.isLoaded(neighbor)) {
                    continue;
                }
                BlockEntity blockEntity = level.getBlockEntity(neighbor);
                if (blockEntity instanceof MeNetworkNode node) {
                    storages.add(node.getMeStorage());
                    queue.add(neighbor);
                }
            }
        }
        return storages.isEmpty() ? MeNetworkStorage.empty() : new MeNetworkStorage(storages);
    }

    private MeNetwork() {
    }
}

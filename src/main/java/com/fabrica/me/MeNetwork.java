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

/**
 * ME-сеть: обход блоков сетки от точки входа по соседям (BFS)
 * и сбор всех MeStorage подключённых MeNetworkNode в единое MeNetworkStorage.
 * Только утилитарные статические методы; инстанцирование запрещено.
 */
public final class MeNetwork {

    /**
     * Найти объединённое хранилище сети, начиная обход с origin:
     * каждый соседний блок-узел (MeNetworkNode) добавляет своё хранилище в сеть.
     */
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
            // Проверяем всех шестерых соседей текущего блока.
            for (Direction direction : Direction.values()) {
                BlockPos neighbor = current.relative(direction);
                if (visited.contains(neighbor) || !level.isLoaded(neighbor)) {
                    continue;
                }
                BlockEntity blockEntity = level.getBlockEntity(neighbor);
                if (blockEntity instanceof MeNetworkNode node) {
                    // Сосед — узел сети: продолжаем обход через него.
                    queue.add(neighbor);
                    // Хранилище добавляем только от узлов-владельцев: у коннекторов
                    // (сеток) getMeStorage() пересчитывает всю сеть, и опрос их во
                    // время обхода приводит к бесконечной рекурсии.
                    if (!(node instanceof MeNetworkConnector)) {
                        storages.add(node.getMeStorage());
                    }
                }
            }
        }
        return storages.isEmpty() ? MeNetworkStorage.empty() : new MeNetworkStorage(storages);
    }

    private MeNetwork() {
    }
}

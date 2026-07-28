package com.fabrica.energy;

import net.minecraft.util.math.BlockPos;
import java.util.*;

public class APNetwork {
    private final Set<BlockPos> nodes = new HashSet<>();
    private final List<APProvider> providers = new ArrayList<>();
    private final List<APConsumer> consumers = new ArrayList<>();
    private boolean dirty = false;

    public void addNode(BlockPos pos, APNode node) {
        nodes.add(pos);
        if (node instanceof APProvider) providers.add((APProvider) node);
        if (node instanceof APConsumer) consumers.add((APConsumer) node);
    }

    public void removeNode(BlockPos pos) {
        nodes.remove(pos);
        dirty = true; // При удалении блока сеть нужно пересобрать
    }

    public void markDirty() { this.dirty = true; }
    public boolean isDirty() { return dirty; }
    public Set<BlockPos> getNodes() { return nodes; }
    public void clear() { nodes.clear(); providers.clear(); consumers.clear(); }

    // Вызывается менеджером при пересборке
    public void tick() {
        if (providers.isEmpty() || consumers.isEmpty()) return;

        // 1. Считаем, сколько всего энергии могут отдать генераторы
        long totalAvailable = 0;
        for (APProvider provider : providers) {
            totalAvailable += provider.getStorage().extractAP(Long.MAX_VALUE, true);
        }
        if (totalAvailable <= 0) return;

        // 2. Распределяем между потребителями
        long remaining = totalAvailable;
        for (APConsumer consumer : consumers) {
            if (remaining <= 0) break;
            long maxCanReceive = consumer.getStorage().insertAP(Long.MAX_VALUE, true);
            if (maxCanReceive > 0) {
                long toGive = Math.min(remaining, maxCanReceive);
                long actuallyGiven = consumer.getStorage().insertAP(toGive, false);
                remaining -= actuallyGiven;
            }
        }

        // 3. Реально забираем AP у провайдеров на сумму, которую удалось распределить
        long toExtract = totalAvailable - remaining;
        for (APProvider provider : providers) {
            if (toExtract <= 0) break;
            long extracted = provider.getStorage().extractAP(toExtract, false);
            toExtract -= extracted;
        }
    }
}
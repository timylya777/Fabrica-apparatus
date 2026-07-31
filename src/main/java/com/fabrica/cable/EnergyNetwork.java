package com.fabrica.cable;

import com.fabrica.api.energy.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class EnergyNetwork extends Network {

    private final CableTier tier;
    private long storedEu;

    public EnergyNetwork(int id, CableTier tier) {
        super(id);
        this.tier = tier;
        this.storedEu = 0;
    }

    public CableTier getTier() {
        return tier;
    }

    public long getStoredEu() {
        return storedEu;
    }

    @Override
    public void tick(ServerLevel level) {
        long available = storedEu;
        List<EnergyProducer> producers = new ArrayList<>();
        List<EnergyConsumer> consumers = new ArrayList<>();
        List<EnergyContainer> containers = new ArrayList<>();

        for (Map.Entry<BlockPos, CableNode> entry : nodes.entrySet()) {
            BlockPos pos = entry.getKey();
            CableNode node = entry.getValue();
            if (!(node instanceof EnergyCableNode energyNode)) continue;

            available += energyNode.getEu();
            energyNode.setEu(0);

            for (Direction dir : Direction.values()) {
                ConnectionType connType = node.getConnectionType(dir);
                if (connType == ConnectionType.PIPE || connType == null) continue;

                BlockPos neighborPos = pos.relative(dir);

                EnergyProducer producer = EnergyApiLookup.PRODUCER.find(level, neighborPos, dir.getOpposite());
                if (producer != null) producers.add(producer);

                EnergyConsumer consumer = EnergyApiLookup.CONSUMER.find(level, neighborPos, dir.getOpposite());
                if (consumer != null) consumers.add(consumer);

                EnergyContainer container = EnergyApiLookup.CONTAINER.find(level, neighborPos, dir.getOpposite());
                if (container != null) containers.add(container);
            }
        }

        // Extract from containers
        for (EnergyContainer container : containers) {
            long canExtract = Math.min(container.extractEnergy(Long.MAX_VALUE, true), tier.maxTransfer());
            if (canExtract > 0) {
                long extracted = container.extractEnergy(canExtract, false);
                available += extracted;
            }
        }

        // Produce from generators
        for (EnergyProducer producer : producers) {
            long produced = producer.produceEnergy();
            if (produced > 0) {
                available = Math.min(available + produced, (long) nodeCount() * tier.maxTransfer());
            }
        }

        // Distribute to consumers
        if (!consumers.isEmpty() && available > 0) {
            long perConsumer = Math.max(1, available / consumers.size());
            perConsumer = Math.min(perConsumer, tier.maxTransfer());
            long remaining = available;
            for (EnergyConsumer consumer : consumers) {
                if (remaining <= 0) break;
                long demand = Math.max(0, consumer.getEnergyDemand());
                if (demand == 0) continue;
                long toSend = Math.min(Math.min(perConsumer, demand), remaining);
                consumer.receiveEnergy(toSend);
                remaining -= toSend;
            }
            available = remaining;
        }

        // Fill containers
        for (EnergyContainer container : containers) {
            if (available <= 0) break;
            long canInsert = Math.min(container.insertEnergy(available, true), tier.maxTransfer());
            if (canInsert > 0) {
                long inserted = container.insertEnergy(canInsert, false);
                available -= inserted;
            }
        }

        // Store remainder in nodes (buffer)
        long maxBuffer = (long) nodeCount() * tier.maxTransfer();
        long toStore = Math.min(available, maxBuffer);
        if (nodeCount() > 0) {
            long perNode = toStore / nodeCount();
            for (CableNode node : nodes.values()) {
                if (node instanceof EnergyCableNode energyNode) {
                    energyNode.setEu(perNode);
                }
            }
        }
        storedEu = 0;
    }

    @Override
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("id", getId());
        tag.putString("tier", tier.name());
        tag.putLong("storedEu", storedEu);

        CompoundTag nodesTag = new CompoundTag();
        int i = 0;
        for (Map.Entry<BlockPos, CableNode> entry : nodes.entrySet()) {
            CompoundTag nodeTag = entry.getValue().save();
            nodeTag.putLong("pos_x", entry.getKey().getX());
            nodeTag.putLong("pos_y", entry.getKey().getY());
            nodeTag.putLong("pos_z", entry.getKey().getZ());
            nodesTag.put(String.valueOf(i++), nodeTag);
        }
        tag.put("nodes", nodesTag);

        return tag;
    }

    @Override
    public void load(CompoundTag tag) {
        this.storedEu = tag.getLongOr("storedEu", 0);

        CompoundTag nodesTag = tag.getCompoundOrEmpty("nodes");
        for (String key : nodesTag.keySet()) {
            CompoundTag nodeTag = nodesTag.getCompoundOrEmpty(key);
            long x = nodeTag.getLongOr("pos_x", 0);
            long y = nodeTag.getLongOr("pos_y", 0);
            long z = nodeTag.getLongOr("pos_z", 0);
            BlockPos pos = new BlockPos((int) x, (int) y, (int) z);

            EnergyCableNode node = new EnergyCableNode(new ArrayList<>(), tier);
            node.load(nodeTag);
            addNode(pos, node);
        }
    }

    public static @Nullable EnergyNetwork tryLoad(CompoundTag tag) {
        int id = tag.getIntOr("id", -1);
        String tierName = tag.getStringOr("tier", "");
        CableTier cableTier = CableTier.byName(tierName);
        if (id < 0 || cableTier == null) return null;

        EnergyNetwork network = new EnergyNetwork(id, cableTier);
        network.load(tag);
        return network;
    }
}

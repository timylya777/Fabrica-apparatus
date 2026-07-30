package com.fabrica.cable;

import com.fabrica.api.energy.CableTier;
import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.List;

public class EnergyNetwork extends Network {

    private final CableTier tier;
    private final List<EnergyCableNode> nodes = new ArrayList<>();
    private long storedEu = 0;

    public EnergyNetwork(int id, CableTier tier) {
        super(id);
        this.tier = tier;
    }

    public void addNode(EnergyCableNode node) {
        nodes.add(node);
    }

    public void removeNode(EnergyCableNode node) {
        nodes.remove(node);
    }

    public CableTier getTier() {
        return tier;
    }

    @Override
    public void tick() {
        long available = storedEu;
        for (EnergyCableNode node : nodes) {
            available += node.getEu();
        }

        long used = 0;
        for (EnergyCableNode node : nodes) {
            long nodeEu = node.getEu();
            long contribution = Math.min(nodeEu, available);
            node.setEu(nodeEu - contribution);
        }

        storedEu = Math.max(0, available - used);
    }

    @Override
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("id", getId());
        tag.putString("tier", tier.name());
        tag.putLong("storedEu", storedEu);
        return tag;
    }

    @Override
    public void load(CompoundTag tag) {
        this.storedEu = tag.getLongOr("storedEu", 0);
    }
}

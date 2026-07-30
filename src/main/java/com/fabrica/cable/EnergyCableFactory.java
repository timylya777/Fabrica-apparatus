package com.fabrica.cable;

import com.fabrica.api.energy.CableTier;
import com.fabrica.api.energy.EnergyContainer;
import com.fabrica.api.energy.EnergyConsumer;
import com.fabrica.api.energy.EnergyProducer;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;

public class EnergyCableFactory implements CableNodeFactory {

    private final CableTier tier;

    public EnergyCableFactory(CableTier tier) {
        this.tier = tier;
    }

    @Override
    public CableNode createNode(Level level, BlockEntity be, List<Direction> connections) {
        return new EnergyCableNode(connections, tier);
    }

    @Override
    public CableNode createNodeFromNbt(CompoundTag tag) {
        EnergyCableNode node = new EnergyCableNode(new ArrayList<>(), tier);
        node.load(tag);
        return node;
    }
}

package com.fabrica.cable;

import com.fabrica.api.energy.CableTier;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;

public class EnergyCableFactory implements CableNodeFactory {

    private final String typeId;
    private final CableTier tier;

    public EnergyCableFactory(String typeId, CableTier tier) {
        this.typeId = typeId;
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

    @Override
    public Network createNetwork(int id) {
        return new EnergyNetwork(id, tier);
    }

    @Override
    public String getTypeId() {
        return typeId;
    }

    public CableTier getTier() {
        return tier;
    }
}

package com.fabrica.cable;

import com.fabrica.api.energy.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class EnergyCableNode extends CableNode {

    private final CableTier tier;
    private long eu;

    public EnergyCableNode(List<Direction> connections, CableTier tier) {
        super(connections);
        this.tier = tier;
        this.eu = 0;
    }

    public EnergyCableNode(CableTier tier) {
        super(new ArrayList<>());
        this.tier = tier;
        this.eu = 0;
    }

    public CableTier getTier() {
        return tier;
    }

    public long getEu() {
        return eu;
    }

    public void setEu(long eu) {
        this.eu = Math.max(0, Math.min(eu, tier.maxTransfer()));
    }

    public long getMaxEu() {
        return tier.maxTransfer();
    }

    @Override
    public void updateConnections(Level level, BlockEntity be) {
        connections.clear();
        BlockPos pos = be.getBlockPos();

        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = pos.relative(dir);
            BlockEntity neighborBe = level.getBlockEntity(neighborPos);
            if (neighborBe instanceof CableBlockEntity cableBE) {
                for (CableNodeSlot slot : cableBE.getNodes()) {
                    if (slot != null && slot.node() instanceof EnergyCableNode) {
                        connections.add(dir);
                        break;
                    }
                }
            } else if (neighborBe != null) {
                connections.add(dir);
            }
        }
    }

    public @Nullable EnergyContainer findContainer(Level level, BlockPos pos, Direction dir) {
        return EnergyApiLookup.CONTAINER.find(level, pos.relative(dir), dir.getOpposite());
    }

    public @Nullable EnergyProducer findProducer(Level level, BlockPos pos, Direction dir) {
        return EnergyApiLookup.PRODUCER.find(level, pos.relative(dir), dir.getOpposite());
    }

    public @Nullable EnergyConsumer findConsumer(Level level, BlockPos pos, Direction dir) {
        return EnergyApiLookup.CONSUMER.find(level, pos.relative(dir), dir.getOpposite());
    }

    @Override
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("tier", tier.name());
        tag.putLong("eu", eu);
        return tag;
    }

    @Override
    public void load(CompoundTag tag) {
        this.eu = tag.getLongOr("eu", 0);
    }

    public List<Direction> getConnections() {
        return connections;
    }
}

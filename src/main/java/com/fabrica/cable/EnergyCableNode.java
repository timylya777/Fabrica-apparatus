package com.fabrica.cable;

import com.fabrica.api.energy.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class EnergyCableNode extends CableNode {

    private final CableTier tier;
    private long eu;
    private final Map<Direction, ConnectionType> connectionTypes = new EnumMap<>(Direction.class);

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
    public ConnectionType getConnectionType(Direction dir) {
        return connectionTypes.get(dir);
    }

    public void setConnectionType(Direction dir, ConnectionType type) {
        if (type == null) {
            connectionTypes.remove(dir);
            connections.remove(dir);
        } else {
            connectionTypes.put(dir, type);
            if (!connections.contains(dir)) {
                connections.add(dir);
            }
        }
    }

    @Override
    public void updateConnections(Level level, BlockPos pos) {
        connectionTypes.clear();
        connections.clear();

        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = pos.relative(dir);
            BlockEntity neighborBe = level.getBlockEntity(neighborPos);

            if (neighborBe instanceof CableBlockEntity cableBE) {
                boolean hasEnergyPipe = false;
                for (CableNodeSlot slot : cableBE.getNodes()) {
                    if (slot != null && slot.node() instanceof EnergyCableNode) {
                        hasEnergyPipe = true;
                        break;
                    }
                }
                if (hasEnergyPipe) {
                    setConnectionType(dir, ConnectionType.PIPE);
                    continue;
                }
            }

            if (neighborBe != null && !neighborBe.isRemoved()) {
                BlockState neighborState = level.getBlockState(neighborPos);
                if (neighborState.getBlock() instanceof IEnergyConnectable connectable
                    && connectable.canConnectEnergy(dir.getOpposite())) {
                    setConnectionType(dir, ConnectionType.BLOCK);
                    continue;
                }

                EnergyProducer producer = EnergyApiLookup.PRODUCER.find(level, neighborPos, dir.getOpposite());
                EnergyConsumer consumer = EnergyApiLookup.CONSUMER.find(level, neighborPos, dir.getOpposite());
                EnergyContainer container = EnergyApiLookup.CONTAINER.find(level, neighborPos, dir.getOpposite());

                if (producer != null || consumer != null || container != null) {
                    setConnectionType(dir, ConnectionType.BLOCK);
                }
            }
        }
    }

    @Override
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("tier", tier.name());
        tag.putLong("eu", eu);

        byte connMask = 0;
        for (Direction dir : Direction.values()) {
            ConnectionType ct = connectionTypes.get(dir);
            if (ct == ConnectionType.PIPE || ct == ConnectionType.BLOCK) {
                connMask |= (byte) (1 << dir.ordinal());
            }
        }
        tag.putByte("connections", connMask);

        return tag;
    }

    @Override
    public void load(CompoundTag tag) {
        this.eu = tag.getLongOr("eu", 0);

        byte connMask = tag.getByteOr("connections", (byte) 0);
        connectionTypes.clear();
        connections.clear();
        for (Direction dir : Direction.values()) {
            if ((connMask & (1 << dir.ordinal())) != 0) {
                setConnectionType(dir, ConnectionType.PIPE);
            }
        }
    }
}

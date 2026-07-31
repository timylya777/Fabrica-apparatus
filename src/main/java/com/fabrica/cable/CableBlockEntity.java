package com.fabrica.cable;

import com.fabrica.api.energy.CableTier;
import com.fabrica.api.energy.EnergyApiLookup;
import com.fabrica.api.energy.EnergyConsumer;
import com.fabrica.api.energy.EnergyContainer;
import com.fabrica.api.energy.EnergyProducer;
import com.fabrica.api.energy.EnergyStorageComponent;
import com.fabrica.api.energy.IEnergyConnectable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class CableBlockEntity extends BlockEntity {

    private static final CableTier TIER = CableTier.COPPER_LV;

    private CableNetwork network;

    public CableBlockEntity(BlockPos pos, BlockState state) {
        super(FabricaCables.CABLE_BE, pos, state);
        this.network = new CableNetwork();
        this.network.addMember(this);
    }

    public void setNetwork(CableNetwork network) {
        this.network = network;
    }

    public CableNetwork getNetwork() {
        return network;
    }

    public void serverTick() {
        if (level == null || level.isClientSide()) return;

        mergeWithConnectedNeighbors();

        EnergyStorageComponent buffer = network.getBuffer();
        long before = buffer.getEnergy();
        long available = before;

        long tick = level.getServer().getTickCount();
        if (network.lastTick != tick) {
            network.lastTick = tick;
            network.lastDemand = network.pendingDemand;
            network.pullRemaining = network.pendingDemand;
            network.pendingDemand = 0;
        }

        long localDemand = 0;
        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = worldPosition.relative(dir);
            BlockEntity neighborBe = level.getBlockEntity(neighborPos);
            if (neighborBe == null || neighborBe.isRemoved() || neighborBe instanceof CableBlockEntity) {
                continue;
            }
            if (!isMachineConnected(dir, neighborPos)) continue;
            EnergyConsumer consumer = EnergyApiLookup.CONSUMER.find(level, neighborPos, dir.getOpposite());
            if (consumer != null) {
                localDemand += consumer.getEnergyDemand();
            }
        }
        network.pendingDemand += localDemand;

        long budget = Math.min(network.pullRemaining, buffer.getCapacity() - available);
        if (budget > 0) {
            for (Direction dir : Direction.values()) {
                if (budget <= 0) break;
                BlockPos neighborPos = worldPosition.relative(dir);
                BlockEntity neighborBe = level.getBlockEntity(neighborPos);
                if (neighborBe == null || neighborBe.isRemoved() || neighborBe instanceof CableBlockEntity) {
                    continue;
                }
                if (!isMachineConnected(dir, neighborPos)) continue;

                EnergyProducer producer = EnergyApiLookup.PRODUCER.find(level, neighborPos, dir.getOpposite());
                if (producer == null) continue;
                EnergyContainer container = EnergyApiLookup.CONTAINER.find(level, neighborPos, dir.getOpposite());
                if (container == null) continue;
                long want = Math.min(budget, TIER.maxTransfer());
                long canExtract = Math.min(container.extractEnergy(want, true), budget);
                if (canExtract > 0) {
                    long extracted = container.extractEnergy(canExtract, false);
                    available += extracted;
                    budget -= extracted;
                    network.pullRemaining -= extracted;
                }
            }
        }

        for (Direction dir : Direction.values()) {
            if (available <= 0) break;
            BlockPos neighborPos = worldPosition.relative(dir);
            BlockEntity neighborBe = level.getBlockEntity(neighborPos);
            if (neighborBe == null || neighborBe.isRemoved() || neighborBe instanceof CableBlockEntity) {
                continue;
            }
            if (!isMachineConnected(dir, neighborPos)) continue;

            EnergyConsumer consumer = EnergyApiLookup.CONSUMER.find(level, neighborPos, dir.getOpposite());
            if (consumer != null) {
                long demand = consumer.getEnergyDemand();
                if (demand > 0) {
                    long toSend = Math.min(available, Math.min(TIER.maxTransfer(), demand));
                    if (toSend > 0) {
                        consumer.receiveEnergy(toSend);
                        available -= toSend;
                    }
                }
            }
        }

        buffer.setEnergy(available);
        if (buffer.getEnergy() != before) {
            setChanged();
        }
    }

    private void mergeWithConnectedNeighbors() {
        for (Direction dir : Direction.values()) {
            BlockEntity neighborBe = level.getBlockEntity(worldPosition.relative(dir));
            if (neighborBe instanceof CableBlockEntity other && network != other.network) {
                network.absorb(other.network);
            }
        }
    }

    private boolean isMachineConnected(Direction dir, BlockPos neighborPos) {
        BlockState neighborState = level.getBlockState(neighborPos);
        if (neighborState.getBlock() instanceof IEnergyConnectable connectable) {
            return connectable.canConnectEnergy(neighborPos, neighborState, dir.getOpposite());
        }
        return false;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putLong("energy", network.getShare());
        output.putLong("capacity", CableNetwork.PER_CABLE_CAPACITY);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        long energy = input.getLongOr("energy", 0);
        network.getBuffer().setCapacity(CableNetwork.PER_CABLE_CAPACITY);
        network.getBuffer().setEnergy(energy);
    }
}

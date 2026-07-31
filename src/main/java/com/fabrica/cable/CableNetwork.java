package com.fabrica.cable;

import com.fabrica.api.energy.EnergyStorageComponent;
import com.fabrica.api.energy.EnergyTier;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CableNetwork {

    public static final long PER_CABLE_CAPACITY = 1024;

    private final Set<CableBlockEntity> members = new HashSet<>();
    private final EnergyStorageComponent buffer = new EnergyStorageComponent(0, EnergyTier.LV);

    long lastTick;
    long lastDemand;
    long pendingDemand;
    long pullRemaining;

    public void addMember(CableBlockEntity cable) {
        if (members.add(cable)) {
            buffer.setCapacity(buffer.getCapacity() + PER_CABLE_CAPACITY);
            cable.setNetwork(this);
        }
    }

    public void removeMember(CableBlockEntity cable) {
        if (members.remove(cable)) {
            buffer.setCapacity(Math.max(0, buffer.getCapacity() - PER_CABLE_CAPACITY));
            cable.setNetwork(null);
        }
    }

    public void absorb(CableNetwork other) {
        if (other == this) return;
        long energy = other.buffer.getEnergy();
        for (CableBlockEntity member : List.copyOf(other.members)) {
            other.removeMember(member);
            addMember(member);
        }
        if (energy > 0) {
            buffer.addEnergy(energy);
        }
    }

    public int getMemberCount() {
        return members.size();
    }

    public Set<CableBlockEntity> getMembers() {
        return members;
    }

    public EnergyStorageComponent getBuffer() {
        return buffer;
    }

    public long getShare() {
        return members.isEmpty() ? 0 : buffer.getEnergy() / members.size();
    }

    public static void rebuildComponents(CableNetwork network) {
        Set<CableBlockEntity> remaining = new HashSet<>();
        for (CableBlockEntity member : network.members) {
            if (!member.isRemoved() && member.getLevel() != null) {
                remaining.add(member);
            }
        }
        if (remaining.isEmpty()) return;
        long perCable = network.buffer.getEnergy() / remaining.size();
        for (Set<CableBlockEntity> component : findComponents(remaining)) {
            CableNetwork net = new CableNetwork();
            for (CableBlockEntity member : component) {
                net.addMember(member);
            }
            net.buffer.setEnergy(perCable * component.size());
        }
    }

    private static List<Set<CableBlockEntity>> findComponents(Set<CableBlockEntity> remaining) {
        List<Set<CableBlockEntity>> components = new ArrayList<>();
        Set<CableBlockEntity> visited = new HashSet<>();
        for (CableBlockEntity root : remaining) {
            if (!visited.add(root)) continue;
            Set<CableBlockEntity> component = new HashSet<>();
            List<CableBlockEntity> queue = new ArrayList<>();
            queue.add(root);
            while (!queue.isEmpty()) {
                CableBlockEntity current = queue.remove(queue.size() - 1);
                component.add(current);
                if (current.getLevel() == null) continue;
                for (Direction dir : Direction.values()) {
                    BlockEntity neighbor = current.getLevel().getBlockEntity(current.getBlockPos().relative(dir));
                    if (neighbor instanceof CableBlockEntity other && remaining.contains(other) && !visited.contains(other)) {
                        visited.add(other);
                        queue.add(other);
                    }
                }
            }
            components.add(component);
        }
        return components;
    }
}

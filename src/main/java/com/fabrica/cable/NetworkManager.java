package com.fabrica.cable;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.*;

public class NetworkManager {

    private final ServerLevel level;

    public NetworkManager(ServerLevel level) {
        this.level = level;
    }

    public void onNodeAdded(BlockPos pos, CableType type) {
        CableNetworks networks = CableNetworks.get(level);
        Set<Integer> connectedNetworks = findConnectedNetworks(pos, type);

        if (connectedNetworks.isEmpty()) {
            int id = networks.getNextNetworkId();
            Network network = createNetwork(id, type, pos);
            networks.addNetwork(network);
        } else if (connectedNetworks.size() == 1) {
            int id = connectedNetworks.iterator().next();
            Network network = networks.getNetwork(id);
            addNodeToNetwork(network, pos);
        } else {
            mergeNetworks(networks, connectedNetworks, pos, type);
        }
    }

    public void onNodeRemoved(BlockPos pos, CableType type) {
        CableNetworks networks = CableNetworks.get(level);
        List<Integer> toCheck = new ArrayList<>();

        for (Network network : networks.getNetworks().values()) {
            if (removeNodeFromNetwork(network, pos)) {
                toCheck.add(network.getId());
                break;
            }
        }

        for (int id : toCheck) {
            Network network = networks.getNetwork(id);
            if (network != null && shouldSplit(network, type)) {
                splitNetwork(networks, network, type);
            }
        }
    }

    private Set<Integer> findConnectedNetworks(BlockPos pos, CableType type) {
        Set<Integer> found = new HashSet<>();
        CableNetworks networks = CableNetworks.get(level);

        for (net.minecraft.core.Direction dir : net.minecraft.core.Direction.values()) {
            BlockPos neighborPos = pos.relative(dir);
            BlockEntity be = level.getBlockEntity(neighborPos);
            if (be instanceof CableBlockEntity cableBE) {
                for (CableNodeSlot slot : cableBE.getNodes()) {
                    if (slot != null && slot.type().equals(type)) {
                        int networkId = getNodeNetworkId(slot.node());
                        if (networkId >= 0) {
                            found.add(networkId);
                        }
                    }
                }
            }
        }
        return found;
    }

    private int getNodeNetworkId(CableNode node) {
        return -1;
    }

    private Network createNetwork(int id, CableType type, BlockPos pos) {
        return null;
    }

    private void addNodeToNetwork(Network network, BlockPos pos) {
    }

    private void mergeNetworks(CableNetworks networks, Set<Integer> ids, BlockPos pos, CableType type) {
    }

    private boolean removeNodeFromNetwork(Network network, BlockPos pos) {
        return false;
    }

    private boolean shouldSplit(Network network, CableType type) {
        return false;
    }

    private void splitNetwork(CableNetworks networks, Network network, CableType type) {
    }

    public void tick() {
        CableNetworks.get(level).tickAll();
    }
}

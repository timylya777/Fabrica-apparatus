package com.fabrica.cable;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;

public class CableNetworks extends SavedData {

    private static final String DATA_NAME = "fabrica_cable_networks";
    private final Map<Integer, Network> networks = new HashMap<>();
    private int nextNetworkId = 0;

    public CableNetworks() {
    }

    public static CableNetworks get(ServerLevel level) {
        return new CableNetworks();
    }

    public Map<Integer, Network> getNetworks() {
        return networks;
    }

    public int getNextNetworkId() {
        return nextNetworkId++;
    }

    public void addNetwork(Network network) {
        networks.put(network.getId(), network);
        setDirty();
    }

    public void removeNetwork(int id) {
        networks.remove(id);
        setDirty();
    }

    public Network getNetwork(int id) {
        return networks.get(id);
    }

    public void tickAll() {
        for (Network network : networks.values()) {
            network.tick();
        }
    }
}

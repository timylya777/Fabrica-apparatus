package com.fabrica.cable;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CableNetworks extends SavedData {

    private final Map<String, NetworkManager> managers = new HashMap<>();

    public CableNetworks() {
    }

    private static final Codec<CableNetworks> CODEC = CompoundTag.CODEC
        .xmap(
            tag -> {
                CableNetworks networks = new CableNetworks();
                CompoundTag managersTag = tag.getCompoundOrEmpty("managers");
                for (String key : managersTag.keySet()) {
                    CableNodeFactory factory = FabricaCables.getFactory(key);
                    if (factory == null) continue;
                    NetworkManager manager = new NetworkManager(factory, networks);
                    manager.load(managersTag.getCompoundOrEmpty(key));
                    networks.managers.put(key, manager);
                }
                return networks;
            },
            networks -> {
                CompoundTag tag = new CompoundTag();
                CompoundTag managersTag = new CompoundTag();
                for (Map.Entry<String, NetworkManager> entry : networks.managers.entrySet()) {
                    managersTag.put(entry.getKey(), entry.getValue().save());
                }
                tag.put("managers", managersTag);
                return tag;
            }
        );

    private static final SavedDataType<CableNetworks> TYPE = new SavedDataType<>(
        Identifier.fromNamespaceAndPath("fabrica_apparatus", "cable_networks"),
        CableNetworks::new,
        CODEC,
        DataFixTypes.LEVEL
    );

    public static CableNetworks get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public NetworkManager getOrCreateManager(CableType type) {
        return managers.computeIfAbsent(
            type.getId().toString(),
            k -> new NetworkManager(type.getFactory(), this)
        );
    }

    public @Nullable NetworkManager getManager(CableType type) {
        return managers.get(type.getId().toString());
    }

    public Collection<NetworkManager> getAllManagers() {
        return managers.values();
    }

    public void tickAll(ServerLevel level) {
        for (NetworkManager manager : managers.values()) {
            manager.tick(level);
        }
    }
}

package com.fabrica.cable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class NetworkManager {

    private final String typeId;
    private final CableNetworks parent;
    private final CableNodeFactory factory;
    private final Map<Integer, Network> networksById = new HashMap<>();
    private final Map<BlockPos, Integer> networkByBlock = new HashMap<>();
    private int nextNetworkId = 0;

    public NetworkManager(CableNodeFactory factory, CableNetworks parent) {
        this.typeId = factory.getTypeId();
        this.parent = parent;
        this.factory = factory;
    }

    public NetworkManager(String typeId, CableNetworks parent) {
        this.typeId = typeId;
        this.parent = parent;
        this.factory = null;
    }

    public String getTypeId() {
        return typeId;
    }

    // ====== PUBLIC API ======

    public void onNodeAdded(BlockPos pos, CableNode node, ServerLevel level) {
        Set<Integer> connected = findConnectedNetworks(pos, level);

        if (connected.isEmpty()) {
            Network network = factory.createNetwork(nextNetworkId++);
            network.addNode(pos, node);
            addNetwork(network);
        } else if (connected.size() == 1) {
            int id = connected.iterator().next();
            Network network = networksById.get(id);
            if (network != null) {
                network.addNode(pos, node);
                networkByBlock.put(pos, id);
                parent.setDirty();
            }
        } else {
            mergeNetworks(connected, pos, node, level);
        }
    }

    public void onNodeRemoved(BlockPos pos, ServerLevel level) {
        Integer netId = networkByBlock.remove(pos);
        if (netId == null) return;

        Network network = networksById.get(netId);
        if (network == null) return;

        network.removeNode(pos);
        parent.setDirty();

        if (network.nodeCount() == 0) {
            networksById.remove(netId);
            return;
        }

        if (shouldSplit(network, pos, level)) {
            splitNetwork(network, pos, level);
        }
    }

    public @Nullable Network getNetwork(BlockPos pos) {
        Integer id = networkByBlock.get(pos);
        if (id == null) return null;
        return networksById.get(id);
    }

    public Collection<Network> getAllNetworks() {
        return networksById.values();
    }

    public void tick(ServerLevel level) {
        for (Network network : networksById.values()) {
            network.tick(level);
        }
    }

    // ====== INTERNAL ======

    private Set<Integer> findConnectedNetworks(BlockPos pos, ServerLevel level) {
        Set<Integer> found = new HashSet<>();
        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = pos.relative(dir);
            BlockEntity be = level.getBlockEntity(neighborPos);
            if (be instanceof CableBlockEntity cableBE) {
                for (CableNodeSlot slot : cableBE.getNodes()) {
                    if (slot != null && slot.node().getNetwork() != null) {
                        int nid = slot.node().getNetworkId();
                        if (nid >= 0) {
                            found.add(nid);
                        }
                    }
                }
            }
        }
        return found;
    }

    private void addNetwork(Network network) {
        networksById.put(network.getId(), network);
        for (BlockPos pos : network.getPositions()) {
            networkByBlock.put(pos, network.getId());
        }
        parent.setDirty();
    }

    private void mergeNetworks(Set<Integer> ids, BlockPos pos, CableNode node, ServerLevel level) {
        if (ids.isEmpty()) return;

        List<Integer> sorted = new ArrayList<>(ids);
        int targetId = sorted.get(0);
        Network target = networksById.get(targetId);
        if (target == null) return;

        target.addNode(pos, node);
        networkByBlock.put(pos, targetId);

        for (int i = 1; i < sorted.size(); i++) {
            Network other = networksById.remove(sorted.get(i));
            if (other != null) {
                target.absorb(other);
            }
        }

        for (Map.Entry<BlockPos, Integer> entry : new HashMap<>(networkByBlock).entrySet()) {
            if (sorted.subList(1, sorted.size()).contains(entry.getValue())) {
                networkByBlock.put(entry.getKey(), targetId);
            }
        }

        parent.setDirty();
    }

    private boolean shouldSplit(Network network, BlockPos removedPos, ServerLevel level) {
        if (network.nodeCount() <= 1) return false;

        Set<BlockPos> reachable = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();

        for (BlockPos pos : network.getPositions()) {
            if (!pos.equals(removedPos)) {
                queue.add(pos);
                break;
            }
        }

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            if (!reachable.add(current)) continue;

            for (Direction dir : Direction.values()) {
                BlockPos neighbor = current.relative(dir);
                if (network.containsPos(neighbor) && !reachable.contains(neighbor) && !neighbor.equals(removedPos)) {
                    queue.add(neighbor);
                }
            }
        }

        return reachable.size() < network.nodeCount();
    }

    private void splitNetwork(Network network, BlockPos removedPos, ServerLevel level) {
        Set<BlockPos> allPositions = new HashSet<>(network.getPositions());
        allPositions.remove(removedPos);

        while (!allPositions.isEmpty()) {
            Set<BlockPos> component = new HashSet<>();
            Queue<BlockPos> queue = new ArrayDeque<>();
            BlockPos start = allPositions.iterator().next();
            queue.add(start);

            while (!queue.isEmpty()) {
                BlockPos current = queue.poll();
                if (!component.add(current)) continue;
                allPositions.remove(current);

                for (Direction dir : Direction.values()) {
                    BlockPos neighbor = current.relative(dir);
                    if (allPositions.contains(neighbor) && !component.contains(neighbor)) {
                        queue.add(neighbor);
                    }
                }
            }

            if (component.size() == network.nodeCount() + 1) {
                component.remove(removedPos);
                if (component.size() == network.nodeCount() || component.isEmpty()) {
                    return;
                }
            }

            Network newNet = factory.createNetwork(nextNetworkId++);
            for (BlockPos pos : component) {
                CableNode node = network.removeNode(pos);
                if (node != null) {
                    newNet.addNode(pos, node);
                }
            }
            addNetwork(newNet);

            allPositions.removeAll(component);
        }
    }

    // ====== NBT ======

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("nextNetworkId", nextNetworkId);

        ListTag networksList = new ListTag();
        for (Network network : networksById.values()) {
            CompoundTag netTag = network.save();
            netTag.putString("class", network.getClass().getName());

            ListTag posList = new ListTag();
            for (BlockPos pos : network.getPositions()) {
                CompoundTag posTag = new CompoundTag();
                posTag.putLong("x", pos.getX());
                posTag.putLong("y", pos.getY());
                posTag.putLong("z", pos.getZ());
                posList.add(posTag);
            }
            netTag.put("positions", posList);

            networksList.add(netTag);
        }
        tag.put("networks", networksList);

        return tag;
    }

    public void load(CompoundTag tag) {
        networksById.clear();
        networkByBlock.clear();
        nextNetworkId = tag.getIntOr("nextNetworkId", 0);

        ListTag networksList = tag.getListOrEmpty("networks");
        for (Tag element : networksList) {
            CompoundTag netTag = (CompoundTag) element;
            String className = netTag.getStringOr("class", "");

            Network network = EnergyNetwork.tryLoad(netTag);
            if (network == null) continue;

            networksById.put(network.getId(), network);

            ListTag posList = netTag.getListOrEmpty("positions");
            for (Tag posTag : posList) {
                CompoundTag pt = (CompoundTag) posTag;
                BlockPos pos = new BlockPos(
                    (int) pt.getLongOr("x", 0),
                    (int) pt.getLongOr("y", 0),
                    (int) pt.getLongOr("z", 0)
                );
                networkByBlock.put(pos, network.getId());
            }
        }
    }
}

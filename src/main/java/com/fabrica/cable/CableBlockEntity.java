package com.fabrica.cable;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.Optional;

public class CableBlockEntity extends BlockEntity {

    private final CableNodeSlot[] nodes = new CableNodeSlot[MAX_NODES];
    public static final int MAX_NODES = 3;

    public CableBlockEntity(BlockPos pos, BlockState state) {
        super(FabricaCables.CABLE_BE, pos, state);
    }

    public CableNodeSlot[] getNodes() {
        return nodes;
    }

    public boolean canAddNode(CableType type) {
        for (CableNodeSlot slot : nodes) {
            if (slot != null && slot.type().getId().equals(type.getId())) {
                return false;
            }
        }
        for (CableNodeSlot slot : nodes) {
            if (slot == null) return true;
        }
        return false;
    }

    public boolean addNode(CableType type, CableNode node) {
        for (int i = 0; i < MAX_NODES; i++) {
            if (nodes[i] == null) {
                nodes[i] = new CableNodeSlot(type, node);
                setChanged();
                return true;
            }
        }
        return false;
    }

    public void removeNode(CableType type) {
        for (int i = 0; i < MAX_NODES; i++) {
            if (nodes[i] != null && nodes[i].type().getId().equals(type.getId())) {
                nodes[i] = null;
                setChanged();
                return;
            }
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        var nodesTag = new net.minecraft.nbt.CompoundTag();
        for (int i = 0; i < MAX_NODES; i++) {
            CableNodeSlot slot = nodes[i];
            if (slot == null) continue;

            var slotTag = new net.minecraft.nbt.CompoundTag();
            slotTag.putString("type", slot.type().getId().toString());
            slotTag.put("node_data", slot.node().save());
            nodesTag.put(String.valueOf(i), slotTag);
        }
        output.store("nodes", net.minecraft.nbt.CompoundTag.CODEC, nodesTag);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        for (int i = 0; i < MAX_NODES; i++) {
            nodes[i] = null;
        }

        Optional<net.minecraft.nbt.CompoundTag> opt = input.read("nodes", net.minecraft.nbt.CompoundTag.CODEC);
        if (opt.isEmpty()) return;

        var nodesTag = opt.get();
        for (String key : nodesTag.keySet()) {
            var slotTag = nodesTag.getCompoundOrEmpty(key);
            int slotIndex = Integer.parseInt(key);
            if (slotIndex < 0 || slotIndex >= MAX_NODES) continue;

            String typeId = slotTag.getStringOr("type", "");
            var nodeData = slotTag.getCompoundOrEmpty("node_data");

            CableNodeFactory factory = FabricaCables.getFactory(typeId);
            if (factory == null) continue;

            CableNode node = factory.createNodeFromNbt(nodeData);
            nodes[slotIndex] = new CableNodeSlot(
                new CableType(net.minecraft.resources.Identifier.parse(typeId), "", 0, factory),
                node
            );
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level instanceof ServerLevel serverLevel) {
            CableNetworks networks = CableNetworks.get(serverLevel);
            for (CableNodeSlot slot : nodes) {
                if (slot != null) {
                    NetworkManager manager = networks.getManager(slot.type());
                    if (manager != null) {
                        manager.onNodeRemoved(pos, serverLevel);
                    }
                }
            }
        }
    }

    public Object getRenderAttachmentData() {
        return nodes;
    }
}

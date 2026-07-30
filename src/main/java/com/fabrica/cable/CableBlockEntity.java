package com.fabrica.cable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

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

    public Object getRenderAttachmentData() {
        return nodes;
    }
}

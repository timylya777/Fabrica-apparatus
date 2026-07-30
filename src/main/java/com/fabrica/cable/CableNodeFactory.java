package com.fabrica.cable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;

public interface CableNodeFactory {
    CableNode createNode(Level level, BlockEntity be, List<net.minecraft.core.Direction> connections);
    CableNode createNodeFromNbt(CompoundTag tag);
}

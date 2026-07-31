package com.fabrica.cable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;

public interface CableNodeFactory {
    CableNode createNode(Level level, BlockEntity be, List<Direction> connections);
    CableNode createNodeFromNbt(CompoundTag tag);
    Network createNetwork(int id);
    String getTypeId();
}

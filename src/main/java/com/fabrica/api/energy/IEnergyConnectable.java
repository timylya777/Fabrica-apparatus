package com.fabrica.api.energy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public interface IEnergyConnectable {
    boolean canConnectEnergy(BlockPos pos, BlockState state, Direction fromNeighborToUs);
}

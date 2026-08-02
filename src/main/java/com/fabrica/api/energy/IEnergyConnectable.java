package com.fabrica.api.energy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Интерфейс блоков, способных подключаться к энергосети: определяет,
 * можно ли провести энергию от соседнего блока в этот блок с указанной стороны.
 */
public interface IEnergyConnectable {
    /** Может ли соседний блок (fromNeighborToUs) соединиться с нами в точке pos. */
    boolean canConnectEnergy(BlockPos pos, BlockState state, Direction fromNeighborToUs);
}

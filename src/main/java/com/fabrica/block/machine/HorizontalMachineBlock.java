package com.fabrica.block.machine;

import com.fabrica.api.energy.IEnergyConnectable;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public abstract class HorizontalMachineBlock extends HorizontalDirectionalBlock implements IEnergyConnectable {
    public HorizontalMachineBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public boolean canConnectEnergy(Direction fromNeighborToUs) {
        return true;
    }
}

package com.fabrica.api.energy;

import net.minecraft.core.Direction;

public interface IEnergyConnectable {
    boolean canConnectEnergy(Direction fromNeighborToUs);
}

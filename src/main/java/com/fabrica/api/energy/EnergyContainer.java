package com.fabrica.api.energy;

public interface EnergyContainer {
    long getEnergy();
    long getCapacity();
    long insertEnergy(long amount, boolean simulate);
    long extractEnergy(long amount, boolean simulate);
    EnergyTier getTier();
}

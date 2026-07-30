package com.fabrica.api.energy;

public interface EnergyConsumer {
    long getEnergyDemand();
    void receiveEnergy(long amount);
    EnergyTier getConsumeTier();
}

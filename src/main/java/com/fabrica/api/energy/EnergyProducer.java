package com.fabrica.api.energy;

public interface EnergyProducer {
    long produceEnergy();
    EnergyTier getProduceTier();
}

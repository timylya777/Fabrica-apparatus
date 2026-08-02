package com.fabrica.api.energy;

/**
 * Интерфейс потребителя энергии: блок, способный принимать энергию.
 * Кабели спрашивают getEnergyDemand и передают энергию через receiveEnergy.
 */
public interface EnergyConsumer {
    /** Сколько энергии сейчас нужно (потребность за тик). */
    long getEnergyDemand();
    /** Принять указанное количество энергии. */
    void receiveEnergy(long amount);
    /** Тип напряжения, с которым работает потребитель. */
    EnergyTier getConsumeTier();
}

package com.fabrica.api.energy;

/**
 * Интерфейс производителя энергии: блок, вырабатывающий энергию (генератор).
 * Кабели вызывают produceEnergy, чтобы забрать выработанную энергию за тик.
 */
public interface EnergyProducer {
    /** Выработать и выдать энергию (возвращает количество за тик). */
    long produceEnergy();
    /** Тип напряжения, который выдаёт производитель. */
    EnergyTier getProduceTier();
}

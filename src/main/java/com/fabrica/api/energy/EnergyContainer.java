package com.fabrica.api.energy;

/**
 * Универсальный контейнер энергии: хранилище с ёмкостью, у которого можно
 * запросить состояние и вставить/извлечь энергию (с поддержкой симуляции).
 */
public interface EnergyContainer {
    /** Текущее количество энергии. */
    long getEnergy();
    /** Максимальная ёмкость. */
    long getCapacity();
    /** Вставить энергию; simulate=true — проверить без изменения. */
    long insertEnergy(long amount, boolean simulate);
    /** Извлечь энергию; simulate=true — проверить без изменения. */
    long extractEnergy(long amount, boolean simulate);
    /** Тип напряжения контейнера. */
    EnergyTier getTier();
}

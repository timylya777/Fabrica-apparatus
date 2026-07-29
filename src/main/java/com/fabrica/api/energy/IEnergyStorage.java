package com.fabrica.api.energy;

/**
 * Базовый интерфейс любой энергетической системы.
 */
public interface IEnergyStorage {

    long receiveEnergy(long maxReceive, boolean simulate);

    long extractEnergy(long maxExtract, boolean simulate);

    long getEnergyStored();

    long getMaxEnergyStored();

    boolean canExtract();

    boolean canReceive();
}
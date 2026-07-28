package com.fabrica.energy;

import net.minecraft.nbt.NbtCompound;

public class SimpleEnergyStorage {
    private long energy;
    private final long capacity;
    private final long maxInsert;  // Максимум, который можно принять за тик
    private final long maxExtract; // Максимум, который можно отдать за тик

    public SimpleEnergyStorage(long capacity, long maxInsert, long maxExtract) {
        this.capacity = capacity;
        this.maxInsert = maxInsert;
        this.maxExtract = maxExtract;
        this.energy = 0;
    }

    // Конструктор с тиром (удобно для машин)
    public SimpleEnergyStorage(long capacity, EnergyTier tier, boolean isGenerator) {
        this.capacity = capacity;
        if (isGenerator) {
            this.maxInsert = 0;  // Генератор не принимает энергию
            this.maxExtract = tier.getMaxTransfer();
        } else {
            this.maxInsert = tier.getMaxTransfer();
            this.maxExtract = 0; // Машина не отдаёт энергию (только потребляет)
        }
        this.energy = 0;
    }

    /**
     * Попытаться принять энергию
     * @param amount Сколько энергии пытаемся принять
     * @param simulate Если true, просто считаем, не меняем состояние
     * @return Сколько энергии реально принято
     */
    public long insertEnergy(long amount, boolean simulate) {
        if (maxInsert <= 0) return 0; // Нельзя принимать
        
        long canAccept = Math.min(amount, maxInsert); // Ограничение по скорости приёма
        canAccept = Math.min(canAccept, capacity - energy); // Ограничение по ёмкости
        
        if (canAccept <= 0) return 0;
        
        if (!simulate) {
            energy += canAccept;
        }
        return canAccept;
    }

    /**
     * Попытаться извлечь энергию
     * @param amount Сколько энергии пытаемся извлечь
     * @param simulate Если true, просто считаем, не меняем состояние
     * @return Сколько энергии реально извлечено
     */
    public long extractEnergy(long amount, boolean simulate) {
        if (maxExtract <= 0) return 0; // Нельзя отдавать
        
        long canExtract = Math.min(amount, maxExtract); // Ограничение по скорости отдачи
        canExtract = Math.min(canExtract, energy); // Ограничение по текущему запасу
        
        if (canExtract <= 0) return 0;
        
        if (!simulate) {
            energy -= canExtract;
        }
        return canExtract;
    }

    // Старые методы для обратной совместимости
    public long receiveEnergy(long amount, boolean simulate) {
        return insertEnergy(amount, simulate);
    }

    public long getEnergy() { return energy; }
    public long getCapacity() { return capacity; }
    public long getMaxInsert() { return maxInsert; }
    public long getMaxExtract() { return maxExtract; }
    public boolean isFull() { return energy >= capacity; }
    public boolean isEmpty() { return energy <= 0; }

    public void writeNbt(NbtCompound nbt) {
        nbt.putLong("Energy", energy);
    }

    public void readNbt(NbtCompound nbt) {
        this.energy = nbt.getLong("Energy");
    }
}
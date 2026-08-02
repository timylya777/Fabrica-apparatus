package com.fabrica.api.energy;

/**
 * Базовая реализация EnergyContainer — хранилище энергии с ёмкостью и тиром.
 * Используется машинами мода как внутренний буфер энергии.
 * Методы insertEnergy/extractEnergy поддерживают режим simulate (без изменений),
 * а setEnergy/setCapacity можно расширить в подклассах (onEnergyChanged).
 */
public class EnergyStorageComponent implements EnergyContainer {

    /** Текущее количество энергии в буфере. */
    protected long energy;
    /** Максимальная ёмкость буфера. */
    protected long capacity;
    /** Тип напряжения (LV/MV/...), которому соответствует хранилище. */
    protected EnergyTier tier;

    public EnergyStorageComponent(long capacity, EnergyTier tier) {
        this.capacity = Math.max(0, capacity);
        this.tier = tier;
        this.energy = 0;
    }

    /** Текущее количество энергии. */
    @Override
    public long getEnergy() {
        return energy;
    }

    /** Ёмкость хранилища. */
    @Override
    public long getCapacity() {
        return capacity;
    }

    /** Вставить энергию (simulate=true — только проверить, сколько влезет). */
    @Override
    public long insertEnergy(long amount, boolean simulate) {
        long accepted = Math.min(amount, capacity - energy);
        if (!simulate) {
            energy += accepted;
            onEnergyChanged();
        }
        return accepted;
    }

    /** Извлечь энергию (simulate=true — только проверить, сколько доступно). */
    @Override
    public long extractEnergy(long amount, boolean simulate) {
        long extracted = Math.min(amount, energy);
        if (!simulate) {
            energy -= extracted;
            onEnergyChanged();
        }
        return extracted;
    }

    /** Тип напряжения хранилища. */
    @Override
    public EnergyTier getTier() {
        return tier;
    }

    /** Установить энергию напрямую (с обрезкой до ёмкости). */
    public void setEnergy(long energy) {
        this.energy = Math.max(0, Math.min(capacity, energy));
        onEnergyChanged();
    }

    /** Изменить ёмкость (текущая энергия не превысит новую ёмкость). */
    public void setCapacity(long capacity) {
        this.capacity = Math.max(0, capacity);
        this.energy = Math.min(this.energy, this.capacity);
        onEnergyChanged();
    }

    /** Изменить тир напряжения хранилища. */
    public void setTier(EnergyTier tier) {
        this.tier = tier;
    }

    /** Добавить энергию (без симуляции). */
    public void addEnergy(long amount) {
        insertEnergy(amount, false);
    }

    /** Убрать энергию (без симуляции). */
    public void removeEnergy(long amount) {
        extractEnergy(amount, false);
    }

    /** Пусто ли хранилище. */
    public boolean isEmpty() {
        return energy == 0;
    }

    /** Заполнено ли хранилище. */
    public boolean isFull() {
        return energy >= capacity;
    }

    /** Хук для подклассов: вызывается при любом изменении энергии/ёмкости. */
    protected void onEnergyChanged() {
    }
}

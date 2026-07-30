package com.fabrica.api.energy;

public class EnergyStorageComponent implements EnergyContainer {

    protected long energy;
    protected long capacity;
    protected EnergyTier tier;

    public EnergyStorageComponent(long capacity, EnergyTier tier) {
        this.capacity = Math.max(0, capacity);
        this.tier = tier;
        this.energy = 0;
    }

    @Override
    public long getEnergy() {
        return energy;
    }

    @Override
    public long getCapacity() {
        return capacity;
    }

    @Override
    public long insertEnergy(long amount, boolean simulate) {
        long accepted = Math.min(amount, capacity - energy);
        if (!simulate) {
            energy += accepted;
            onEnergyChanged();
        }
        return accepted;
    }

    @Override
    public long extractEnergy(long amount, boolean simulate) {
        long extracted = Math.min(amount, energy);
        if (!simulate) {
            energy -= extracted;
            onEnergyChanged();
        }
        return extracted;
    }

    @Override
    public EnergyTier getTier() {
        return tier;
    }

    public void setEnergy(long energy) {
        this.energy = Math.max(0, Math.min(capacity, energy));
        onEnergyChanged();
    }

    public void setCapacity(long capacity) {
        this.capacity = Math.max(0, capacity);
        this.energy = Math.min(this.energy, this.capacity);
        onEnergyChanged();
    }

    public void setTier(EnergyTier tier) {
        this.tier = tier;
    }

    public void addEnergy(long amount) {
        insertEnergy(amount, false);
    }

    public void removeEnergy(long amount) {
        extractEnergy(amount, false);
    }

    public boolean isEmpty() {
        return energy == 0;
    }

    public boolean isFull() {
        return energy >= capacity;
    }

    protected void onEnergyChanged() {
    }
}

package com.fabrica.apparatus.api.energy;

/**
 * Стандартная реализация энергетического хранилища.
 */
public class EnergyStorage implements IEnergyStorage {

    protected long energy;

    protected final long capacity;

    protected final long maxReceive;

    protected final long maxExtract;

    public EnergyStorage(long capacity) {
        this(capacity, capacity, capacity);
    }

    public EnergyStorage(long capacity, long maxTransfer) {
        this(capacity, maxTransfer, maxTransfer);
    }

    public EnergyStorage(long capacity, long maxReceive, long maxExtract) {

        this.capacity = Math.max(0, capacity);
        this.maxReceive = Math.max(0, maxReceive);
        this.maxExtract = Math.max(0, maxExtract);
        this.energy = 0;
    }

    @Override
    public long receiveEnergy(long maxReceive, boolean simulate) {

        if (!canReceive())
            return 0;

        long received = Math.min(
                capacity - energy,
                Math.min(this.maxReceive, maxReceive)
        );

        if (!simulate) {
            energy += received;
            onEnergyChanged();
        }

        return received;
    }

    @Override
    public long extractEnergy(long maxExtract, boolean simulate) {

        if (!canExtract())
            return 0;

        long extracted = Math.min(
                energy,
                Math.min(this.maxExtract, maxExtract)
        );

        if (!simulate) {
            energy -= extracted;
            onEnergyChanged();
        }

        return extracted;
    }

    @Override
    public long getEnergyStored() {
        return energy;
    }

    @Override
    public long getMaxEnergyStored() {
        return capacity;
    }

    @Override
    public boolean canExtract() {
        return maxExtract > 0;
    }

    @Override
    public boolean canReceive() {
        return maxReceive > 0;
    }

    public void setEnergy(long energy) {

        this.energy = Math.max(0, Math.min(capacity, energy));
        onEnergyChanged();
    }

    public void addEnergy(long amount) {
        receiveEnergy(amount, false);
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

    /**
     * Вызывается после изменения количества энергии.
     * BlockEntity сможет переопределить этот метод и вызвать markDirty().
     */
    protected void onEnergyChanged() {
    }
}
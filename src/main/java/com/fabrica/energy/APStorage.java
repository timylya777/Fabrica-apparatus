package com.fabrica.energy;

import net.minecraft.nbt.NbtCompound;

public class APStorage {
    private long ap;
    private final long capacity;
    private final long maxInsert;  // Макс. прием (AP/t)
    private final long maxExtract; // Макс. отдача (AP/t)

    public APStorage(long capacity, long maxInsert, long maxExtract) {
        this.capacity = capacity;
        this.maxInsert = maxInsert;
        this.maxExtract = maxExtract;
        this.ap = 0;
    }

    public long insertAP(long amount, boolean simulate) {
        if (maxInsert <= 0) return 0;
        long canAccept = Math.min(amount, Math.min(maxInsert, capacity - ap));
        if (canAccept <= 0) return 0;
        if (!simulate) ap += canAccept;
        return canAccept;
    }

    public long extractAP(long amount, boolean simulate) {
        if (maxExtract <= 0) return 0;
        long canExtract = Math.min(amount, Math.min(maxExtract, ap));
        if (canExtract <= 0) return 0;
        if (!simulate) ap -= canExtract;
        return canExtract;
    }

    public long getAP() { return ap; }
    public long getCapacity() { return capacity; }
    public boolean isFull() { return ap >= capacity; }
    public boolean isEmpty() { return ap <= 0; }
    public long generateAP(long amount) {
        long canAdd = Math.min(amount, capacity - ap);
        if (canAdd <= 0) return 0;
        ap += canAdd;
        return canAdd;
    }

    public void writeNbt(NbtCompound nbt) { nbt.putLong("AP", ap); }
    public void readNbt(NbtCompound nbt) { this.ap = nbt.getLong("AP"); }
}
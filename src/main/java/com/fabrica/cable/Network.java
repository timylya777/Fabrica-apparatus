package com.fabrica.cable;

import net.minecraft.nbt.CompoundTag;

public abstract class Network {
    protected int id;

    public Network(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public abstract void tick();

    public abstract CompoundTag save();

    public abstract void load(CompoundTag tag);
}

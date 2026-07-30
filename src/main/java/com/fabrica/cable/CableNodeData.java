package com.fabrica.cable;

import net.minecraft.nbt.CompoundTag;

public class CableNodeData {
    private final CableType type;
    private final CompoundTag data;

    public CableNodeData(CableType type, CompoundTag data) {
        this.type = type;
        this.data = data;
    }

    public CableType getType() { return type; }
    public CompoundTag getData() { return data; }

    public CableNodeData copy() {
        return new CableNodeData(type, data.copy());
    }

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putString("type", type.getId().toString());
        tag.put("data", data);
        return tag;
    }
}

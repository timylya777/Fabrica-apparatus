package com.fabrica.cable;

import net.minecraft.resources.Identifier;

import java.util.Objects;

public class CableType {
    private final Identifier id;
    private final String name;
    private final int color;

    public CableType(Identifier id, String name, int color) {
        this.id = id;
        this.name = name;
        this.color = color;
    }

    public Identifier getId() { return id; }
    public String getName() { return name; }
    public int getColor() { return color; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CableType cableType)) return false;
        return id.equals(cableType.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

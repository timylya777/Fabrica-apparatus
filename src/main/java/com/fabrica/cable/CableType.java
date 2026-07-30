package com.fabrica.cable;

import net.minecraft.resources.Identifier;

import java.util.Objects;

public class CableType {
    private final Identifier id;
    private final String name;
    private final int color;
    private final CableNodeFactory factory;

    public CableType(Identifier id, String name, int color, CableNodeFactory factory) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.factory = factory;
    }

    public Identifier getId() { return id; }
    public String getName() { return name; }
    public int getColor() { return color; }
    public CableNodeFactory getFactory() { return factory; }

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

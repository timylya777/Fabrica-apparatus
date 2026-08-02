package com.fabrica.me;

public enum MeStorageDiskTier {
    BASIC(64),
    ADVANCED(256),
    ELITE(1024);

    private final long unit;

    MeStorageDiskTier(long unit) {
        this.unit = unit;
    }

    public long getUnit() {
        return unit;
    }

    public long getCapacity() {
        return unit * 1024L;
    }
}

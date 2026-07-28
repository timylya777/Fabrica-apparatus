package com.fabrica.energy;

public enum EnergyTier {
    LV(32, "Low Voltage"),      // 32 EU/t
    MV(128, "Medium Voltage"),  // 128 EU/t
    HV(512, "High Voltage"),    // 512 EU/t
    EV(2048, "Extreme Voltage"),// 2048 EU/t
    IV(8192, "Insane Voltage"), // 8192 EU/t
    UV(32768, "Ultimate Voltage"); // 32768 EU/t

    private final long maxTransfer;
    private final String name;

    EnergyTier(long maxTransfer, String name) {
        this.maxTransfer = maxTransfer;
        this.name = name;
    }

    public long getMaxTransfer() {
        return maxTransfer;
    }

    public String getName() {
        return name;
    }
}
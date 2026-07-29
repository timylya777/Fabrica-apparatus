package com.fabrica.energy;

public enum EnergyTier {
    LV(32, "Low Voltage"),      // 32 AP/t
    MV(128, "Medium Voltage"),  // 128 AP/t
    HV(512, "High Voltage"),    // 512 AP/t
    EV(2048, "Extreme Voltage"),// 2048 AP/t
    IV(8192, "Insane Voltage"), // 8192 AP/t
    UV(32768, "Ultimate Voltage"); // 32768 AP/t

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

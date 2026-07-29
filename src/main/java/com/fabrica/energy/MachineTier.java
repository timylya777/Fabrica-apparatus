
package com.fabrica.energy;

public enum MachineTier {
    BASIC(EnergyTier.LV, 1.0f, 1.0f, 1),
    ADVANCED(EnergyTier.MV, 1.75f, 1.4f, 4),
    ELITE(EnergyTier.HV, 3.0f, 1.9f, 16),
    ULTIMATE(EnergyTier.EV, 5.0f, 2.5f, 64);

    public final EnergyTier energyTier;
    public final float speedMultiplier;
    public final float energyUsageMultiplier;
    public final long internalBufferMultiplier;

    MachineTier(EnergyTier energyTier, float speedMultiplier, float energyUsageMultiplier, long internalBufferMultiplier) {
        this.energyTier = energyTier;
        this.speedMultiplier = speedMultiplier;
        this.energyUsageMultiplier = energyUsageMultiplier;
        this.internalBufferMultiplier = internalBufferMultiplier;
    }

    public long getCapacity(long baseCapacity) {
        return (long) (baseCapacity * internalBufferMultiplier);
    }

    public long getMaxInput() {
        return energyTier.getMaxTransfer();
    }

    public int getProcessingTicks(int baseTicks) {
        return Math.max(1, (int) (baseTicks / speedMultiplier));
    }

    public long getEnergyPerOperation(long baseCost) {
        return (long) (baseCost * energyUsageMultiplier / speedMultiplier);
    }
}
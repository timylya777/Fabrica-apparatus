package com.fabrica.api.energy;

public record EnergyTier(String name, long baseVoltage) implements Comparable<EnergyTier> {

    public static final EnergyTier LV = new EnergyTier("lv", 32);
    public static final EnergyTier MV = new EnergyTier("mv", 128);
    public static final EnergyTier HV = new EnergyTier("hv", 512);
    public static final EnergyTier EV = new EnergyTier("ev", 2048);
    public static final EnergyTier IV = new EnergyTier("iv", 8192);
    public static final EnergyTier LuV = new EnergyTier("luv", 32768);
    public static final EnergyTier ZPM = new EnergyTier("zpm", 131072);
    public static final EnergyTier UV = new EnergyTier("uv", 524288);

    @Override
    public int compareTo(EnergyTier o) {
        return Long.compare(this.baseVoltage, o.baseVoltage);
    }
}

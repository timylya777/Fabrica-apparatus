package com.fabrica.api.energy;

/**
 * Тиры напряжения энергии мода: имя + базовое напряжение (FE/тик).
 * Сравнение тиров идёт по напряжению (LV < MV < ... < UV),
 * что позволяет кабелям и машинам проверять совместимость уровней.
 */
public record EnergyTier(String name, long baseVoltage) implements Comparable<EnergyTier> {

    /** Низкое напряжение. */
    public static final EnergyTier LV = new EnergyTier("lv", 32);
    /** Среднее напряжение. */
    public static final EnergyTier MV = new EnergyTier("mv", 128);
    /** Высокое напряжение. */
    public static final EnergyTier HV = new EnergyTier("hv", 512);
    /** Сверхвысокое напряжение. */
    public static final EnergyTier EV = new EnergyTier("ev", 2048);
    /** Сверхвысокий уровень. */
    public static final EnergyTier IV = new EnergyTier("iv", 8192);
    /** Сверхпроводниковый уровень LuV. */
    public static final EnergyTier LuV = new EnergyTier("luv", 32768);
    /** Зетапотенциальный уровень ZPM. */
    public static final EnergyTier ZPM = new EnergyTier("zpm", 131072);
    /** Ультравысокое напряжение. */
    public static final EnergyTier UV = new EnergyTier("uv", 524288);

    /** Сравнение тиров по базовому напряжению. */
    @Override
    public int compareTo(EnergyTier o) {
        return Long.compare(this.baseVoltage, o.baseVoltage);
    }
}

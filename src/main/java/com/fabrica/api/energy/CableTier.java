package com.fabrica.api.energy;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Тиры кабелей: связывают название кабеля, его напряжение (EnergyTier)
 * и максимальную скорость передачи энергии (FE/тик).
 * Статический реестр позволяет находить тир по строковому имени (для NBT/конфигов).
 */
public record CableTier(String name, EnergyTier voltageTier, long maxTransfer) {

    /** Медный кабель, низкое напряжение. */
    public static final CableTier COPPER_LV = new CableTier("copper_lv", EnergyTier.LV, 256);
    /** Алюминиевый кабель, среднее напряжение. */
    public static final CableTier ALUMINUM_MV = new CableTier("aluminum_mv", EnergyTier.MV, 1024);
    /** Золотой кабель, высокое напряжение. */
    public static final CableTier GOLD_HV = new CableTier("gold_hv", EnergyTier.HV, 4096);
    /** Алюминиевый кабель, сверхвысокое напряжение. */
    public static final CableTier ALUMINUM_EV = new CableTier("aluminum_ev", EnergyTier.EV, 16384);
    /** Платиновый кабель, сверхвысокий уровень IV. */
    public static final CableTier PLATINUM_IV = new CableTier("platinum_iv", EnergyTier.IV, 65536);
    /** Вольфрамовый кабель, уровень LuV. */
    public static final CableTier TUNGSTEN_LUV = new CableTier("tungsten_luv", EnergyTier.LuV, 262144);
    /** Сверхпроводник — неограниченная передача (UV). */
    public static final CableTier SUPERCONDUCTOR = new CableTier("superconductor", EnergyTier.UV, Long.MAX_VALUE);

    /** Реестр тиров кабелей по строковому имени. */
    private static final Map<String, CableTier> BY_NAME = new HashMap<>();

    static {
        BY_NAME.put(COPPER_LV.name(), COPPER_LV);
        BY_NAME.put(ALUMINUM_MV.name(), ALUMINUM_MV);
        BY_NAME.put(GOLD_HV.name(), GOLD_HV);
        BY_NAME.put(ALUMINUM_EV.name(), ALUMINUM_EV);
        BY_NAME.put(PLATINUM_IV.name(), PLATINUM_IV);
        BY_NAME.put(TUNGSTEN_LUV.name(), TUNGSTEN_LUV);
        BY_NAME.put(SUPERCONDUCTOR.name(), SUPERCONDUCTOR);
    }

    /** Найти тир кабеля по имени (null, если такого тира нет). */
    public static @Nullable CableTier byName(String name) {
        return BY_NAME.get(name);
    }
}

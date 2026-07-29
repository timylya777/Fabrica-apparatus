
package com.fabrica.recipe;

import com.mojang.serialization.Codec;

public enum MachineType {
    CRUSHER,
    ORE_WASHER,
    THERMAL_CENTRIFUGE,
    INDUCTION_SMELTER,
    ALLOY_SMELTER,
    COMPRESSOR,
    ELECTROLYZER;

    public static final Codec<MachineType> CODEC = Codec.STRING.xmap(MachineType::valueOf, Enum::name);
}
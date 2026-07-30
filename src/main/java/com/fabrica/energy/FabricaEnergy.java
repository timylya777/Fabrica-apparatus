package com.fabrica.energy;

import com.fabrica.api.energy.EnergyApiLookup;
import com.fabrica.block.ModBlockEntities;

public final class FabricaEnergy {

    public static void register() {
        EnergyApiLookup.PRODUCER.registerForBlockEntity(
            (be, dir) -> be.getEnergyProducer(),
            ModBlockEntities.GENERATOR
        );

        EnergyApiLookup.CONSUMER.registerForBlockEntity(
            (be, dir) -> be.getEnergyConsumer(),
            ModBlockEntities.CONSUMER
        );
    }

    private FabricaEnergy() {
    }
}

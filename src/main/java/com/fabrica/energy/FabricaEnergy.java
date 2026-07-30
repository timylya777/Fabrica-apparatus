package com.fabrica.energy;

import com.fabrica.api.energy.EnergyApiLookup;
import com.fabrica.block.ModBlockEntities;
import com.fabrica.block.machine.fuel.FabricaGeneratorMachines;

public final class FabricaEnergy {

    public static void register() {
        EnergyApiLookup.PRODUCER.registerForBlockEntity(
            (be, dir) -> be.getEnergyProducer(),
            ModBlockEntities.GENERATOR
        );
        EnergyApiLookup.PRODUCER.registerForBlockEntity(
            (be, dir) -> be.getEnergyProducer(),
            FabricaGeneratorMachines.COAL_GENERATOR_BE
        );

        EnergyApiLookup.CONSUMER.registerForBlockEntity(
            (be, dir) -> be.getEnergyConsumer(),
            ModBlockEntities.CONSUMER
        );

        EnergyApiLookup.CONTAINER.registerForBlockEntity(
            (be, dir) -> be.getEnergyContainer(),
            ModBlockEntities.GENERATOR
        );
        EnergyApiLookup.CONTAINER.registerForBlockEntity(
            (be, dir) -> be.getEnergyContainer(),
            ModBlockEntities.CONSUMER
        );
        EnergyApiLookup.CONTAINER.registerForBlockEntity(
            (be, dir) -> be.getEnergyContainer(),
            FabricaGeneratorMachines.COAL_GENERATOR_BE
        );
    }

    private FabricaEnergy() {
    }
}

package com.fabrica.energy;

import com.fabrica.api.energy.EnergyApiLookup;
import com.fabrica.block.ModBlockEntities;
import com.fabrica.block.machine.furnace.ElectricFurnaceBlockEntity;
import com.fabrica.block.machine.generator.GeneratorBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;

import java.util.List;

public final class FabricaEnergy {

    public static void register() {
        EnergyApiLookup.PRODUCER.registerForBlockEntity(
            (be, dir) -> be.getEnergyProducer(),
            ModBlockEntities.GENERATOR
        );

        EnergyApiLookup.CONSUMER.registerForBlockEntity(
            (be, dir) -> be.getEnergyConsumer(),
            ModBlockEntities.ELECTRIC_FURNACE
        );

        EnergyApiLookup.CONTAINER.registerForBlockEntity(
            (be, dir) -> be.getEnergyContainer(),
            ModBlockEntities.GENERATOR
        );
        EnergyApiLookup.CONTAINER.registerForBlockEntity(
            (be, dir) -> be.getEnergyContainer(),
            ModBlockEntities.ELECTRIC_FURNACE
        );

        ItemStorage.SIDED.registerForBlockEntity(
            (GeneratorBlockEntity be, net.minecraft.core.Direction dir) -> ContainerStorage.of(be.getFuelInventory(), dir),
            ModBlockEntities.GENERATOR
        );

        ItemStorage.SIDED.registerForBlockEntity(
            (ElectricFurnaceBlockEntity be, net.minecraft.core.Direction dir) -> {
                Storage<ItemVariant> input = ContainerStorage.of(be.getInputInventory(), dir);
                Storage<ItemVariant> output = ContainerStorage.of(be.getOutputInventory(), dir);
                return new CombinedStorage<>(List.of(input, output));
            },
            ModBlockEntities.ELECTRIC_FURNACE
        );
    }

    private FabricaEnergy() {
    }
}

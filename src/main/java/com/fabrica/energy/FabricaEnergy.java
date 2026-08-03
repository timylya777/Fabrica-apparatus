package com.fabrica.energy;

import com.fabrica.api.energy.EnergyApiLookup;
import com.fabrica.block.ModBlockEntities;
import com.fabrica.block.machine.furnace.ElectricFurnaceBlockEntity;
import com.fabrica.block.machine.generator.GeneratorBlockEntity;
import com.fabrica.block.machine.macerator.MaceratorBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;

import java.util.List;

/**
 * Регистрация энергетического API (EnergyApiLookup: PRODUCER/CONSUMER/CONTAINER)
 * и ItemStorage для машин мода: генератора и электропечи.
 * Определяет, какие блоки выдают/потребляют энергию и какие у них инвентари.
 */
public final class FabricaEnergy {

    /** Зарегистрировать energy- и item-lookup-и для машин (вызывается при инициализации мода). */
    public static void register() {
        // Генератор — производитель энергии.
        EnergyApiLookup.PRODUCER.registerForBlockEntity(
            (be, dir) -> be.getEnergyProducer(),
            ModBlockEntities.GENERATOR
        );

        // Электропечь — потребитель энергии.
        EnergyApiLookup.CONSUMER.registerForBlockEntity(
            (be, dir) -> be.getEnergyConsumer(),
            ModBlockEntities.ELECTRIC_FURNACE
        );

        // Обе машины являются контейнерами энергии (для чтения/записи через кабели).
        EnergyApiLookup.CONTAINER.registerForBlockEntity(
            (be, dir) -> be.getEnergyContainer(),
            ModBlockEntities.GENERATOR
        );
        EnergyApiLookup.CONTAINER.registerForBlockEntity(
            (be, dir) -> be.getEnergyContainer(),
            ModBlockEntities.ELECTRIC_FURNACE
        );

        // Мацератор — потребитель энергии.
        EnergyApiLookup.CONSUMER.registerForBlockEntity(
            (be, dir) -> be.getEnergyConsumer(),
            ModBlockEntities.MACERATOR
        );
        EnergyApiLookup.CONTAINER.registerForBlockEntity(
            (be, dir) -> be.getEnergyContainer(),
            ModBlockEntities.MACERATOR
        );

        // Генератор: инвентарь топлива как ItemStorage.
        ItemStorage.SIDED.registerForBlockEntity(
            (GeneratorBlockEntity be, net.minecraft.core.Direction dir) -> ContainerStorage.of(be.getFuelInventory(), dir),
            ModBlockEntities.GENERATOR
        );

        // Электропечь: вход и выход (комбинированное хранилище) как ItemStorage.
        ItemStorage.SIDED.registerForBlockEntity(
            (ElectricFurnaceBlockEntity be, net.minecraft.core.Direction dir) -> {
                Storage<ItemVariant> input = ContainerStorage.of(be.getInputInventory(), dir);
                Storage<ItemVariant> output = ContainerStorage.of(be.getOutputInventory(), dir);
                return new CombinedStorage<>(List.of(input, output));
            },
            ModBlockEntities.ELECTRIC_FURNACE
        );

        // Мацератор: вход и выход как ItemStorage (для подачи руды трубами).
        ItemStorage.SIDED.registerForBlockEntity(
            (MaceratorBlockEntity be, net.minecraft.core.Direction dir) -> {
                Storage<ItemVariant> input = ContainerStorage.of(be.getInputInventory(), dir);
                Storage<ItemVariant> output = ContainerStorage.of(be.getOutputInventory(), dir);
                return new CombinedStorage<>(List.of(input, output));
            },
            ModBlockEntities.MACERATOR
        );
    }

    private FabricaEnergy() {
    }
}

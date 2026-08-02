package com.fabrica.block.machine;

import com.fabrica.api.energy.EnergyContainer;
import com.fabrica.api.energy.EnergyProducer;
import com.fabrica.api.energy.EnergyConsumer;
import com.fabrica.api.energy.EnergyStorageComponent;
import com.fabrica.api.energy.EnergyTier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

// Машина с внутренним хранилищем энергии: сохраняет в NBT заряд, ёмкость и тир.
public abstract class EnergyMachineBlockEntity extends MachineBlockEntity {
    // Хранилище энергии: ёмкость, тир и текущий заряд.
    protected EnergyStorageComponent energyStorage;

    public EnergyMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, long capacity, EnergyTier tier) {
        super(type, pos, state);
        this.energyStorage = new EnergyStorageComponent(capacity, tier) {
            @Override
            // Изменение заряда помечает сущность как требующую сохранения.
            protected void onEnergyChanged() {
                setChanged();
            }
        };
    }

    public EnergyContainer getEnergyContainer() {
        return energyStorage;
    }

    public EnergyProducer getEnergyProducer() {
        return null;
    }

    public EnergyConsumer getEnergyConsumer() {
        return null;
    }

    // Сохраняем энергию в NBT: заряд, ёмкость и тир (позволяет редактировать их в файле мира).
    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putLong("energy", energyStorage.getEnergy());
        output.putLong("capacity", energyStorage.getCapacity());
        output.putString("tier", energyStorage.getTier().name());
    }

    // Восстанавливаем энергию из NBT.
    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        long energy = input.getLongOr("energy", 0);
        long capacity = input.getLongOr("capacity", energyStorage.getCapacity());
        String tierName = input.getStringOr("tier", energyStorage.getTier().name());

        // Разбор тира из строки; неизвестное имя оставляет текущий тир.
        EnergyTier tier = switch (tierName) {
            case "lv" -> EnergyTier.LV;
            case "mv" -> EnergyTier.MV;
            case "hv" -> EnergyTier.HV;
            case "ev" -> EnergyTier.EV;
            case "iv" -> EnergyTier.IV;
            case "luv" -> EnergyTier.LuV;
            case "zpm" -> EnergyTier.ZPM;
            case "uv" -> EnergyTier.UV;
            default -> energyStorage.getTier();
        };

        // Применяем восстановленные значения к хранилищу.
        energyStorage.setCapacity(capacity);
        energyStorage.setTier(tier);
        energyStorage.setEnergy(energy);
    }
}

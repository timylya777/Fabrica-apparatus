package com.fabrica.block.entity.machine;

import com.fabrica.energy.EnergyTier;
import com.fabrica.energy.SimpleEnergyStorage;
import com.fabrica.registry.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

public class EnergyCableBlockEntity extends BlockEntity {
    private final SimpleEnergyStorage energyStorage;
    private final EnergyTier tier;

    public EnergyCableBlockEntity(BlockPos pos, BlockState state, EnergyTier tier) {
        super(ModBlockEntities.ENERGY_CABLE, pos, state);
        this.tier = tier;
        // Кабель имеет маленькую ёмкость (буфер), но лимит передачи = тир
        this.energyStorage = new SimpleEnergyStorage(
            tier.getMaxTransfer(), // Ёмкость = макс передача за тик
            tier.getMaxTransfer(), // maxInsert
            tier.getMaxTransfer()  // maxExtract
        );
    }

    public SimpleEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    public EnergyTier getTier() {
        return tier;
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.energyStorage.readNbt(nbt);
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        this.energyStorage.writeNbt(nbt);
    }
}
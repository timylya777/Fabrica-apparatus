package com.fabrica.block.entity;

import com.fabrica.energy.EnergyTier;
import com.fabrica.registry.ModBlockEntities;

import team.reborn.energy.api.base.SimpleEnergyStorage;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class EnergyCableBlockEntity extends BlockEntity {
    private final SimpleEnergyStorage energyStorage;
    private final EnergyTier tier;

    public EnergyCableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ENERGY_CABLE, pos, state);
        this.tier = EnergyTier.LV;
        this.energyStorage = new SimpleEnergyStorage(
            this.tier.getMaxTransfer(),
            this.tier.getMaxTransfer(),
            this.tier.getMaxTransfer()
        );
    }

    public SimpleEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    public EnergyTier getTier() {
        return tier;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.energyStorage.amount = input.getLongOr("Energy", 0L);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putLong("Energy", energyStorage.amount);
    }
}

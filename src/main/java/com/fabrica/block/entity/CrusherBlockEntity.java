
package com.fabrica.block.entity;

import com.fabrica.energy.MachineTier;
import com.fabrica.recipe.MachineType;
import com.fabrica.registry.ModBlockEntities;
import team.reborn.energy.api.base.SimpleEnergyStorage;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class CrusherBlockEntity extends ProcessingMachineBlockEntity {
    public CrusherBlockEntity(BlockPos pos, BlockState state, MachineTier tier) {
        super(ModBlockEntities.CRUSHER, pos, state, MachineType.CRUSHER, tier, 1, 2);
        this.energyStorage = new SimpleEnergyStorage(tier.getCapacity(1000), tier.getMaxInput(), 0) {
            @Override
            protected void onFinalCommit() {
                setChanged();
            }
        };
    }
}
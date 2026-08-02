package com.fabrica.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

// Базовый BlockEntity Фабрики: от него наследуются все машины и устройства мода.
public class FabricaBlockEntity extends BlockEntity {
    public FabricaBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }
}

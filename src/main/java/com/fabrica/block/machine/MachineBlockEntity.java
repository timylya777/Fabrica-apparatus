package com.fabrica.block.machine;

import com.fabrica.block.FabricaBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

// Абстрактная сущность машины: наследники реализуют логику каждого тика.
public abstract class MachineBlockEntity extends FabricaBlockEntity {
    public MachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // Серверный тик: производство/потребление энергии, обработка рецептов.
    public abstract void serverTick();
}

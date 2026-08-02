package com.fabrica.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

// Базовый блок Фабрики: общая обёртка над Block для всех блоков мода.
public class FabricaBlock extends Block {
    public FabricaBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }
}

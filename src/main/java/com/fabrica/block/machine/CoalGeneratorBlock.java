package com.fabrica.block.machine;

import com.fabrica.block.entity.CoalGeneratorBlockEntity;
import com.fabrica.registry.ModBlockEntities;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class CoalGeneratorBlock extends BaseEntityBlock {

    public static final MapCodec<CoalGeneratorBlock> CODEC = simpleCodec(CoalGeneratorBlock::new);

    public CoalGeneratorBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<CoalGeneratorBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CoalGeneratorBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return createTickerHelper(type, ModBlockEntities.COAL_GENERATOR,
                (lvl, pos, st, be) -> ((CoalGeneratorBlockEntity) be).serverTick(lvl, pos, st));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            var be = level.getBlockEntity(pos);
            if (be instanceof CoalGeneratorBlockEntity) {
                player.openMenu((CoalGeneratorBlockEntity) be);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        var be = level.getBlockEntity(pos);
        return be instanceof MenuProvider ? (MenuProvider) be : null;
    }
}

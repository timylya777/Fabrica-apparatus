package com.fabrica.cable;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;

public class CableTypeItem extends BlockItem {

    private final CableType cableType;

    public CableTypeItem(Block block, Properties properties, CableType cableType) {
        super(block, properties);
        this.cableType = cableType;
    }

    public CableType getCableType() {
        return cableType;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        BlockState clickedState = level.getBlockState(clickedPos);

        if (clickedState.getBlock() instanceof CableBlock && !context.isSecondaryUseActive()) {
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            return addToCable(level, clickedPos) ? InteractionResult.CONSUME : InteractionResult.FAIL;
        }

        InteractionResult result = super.useOn(context);
        if (result.consumesAction() && !level.isClientSide()) {
            BlockPos placedPos = clickedPos;
            if (!(level.getBlockState(placedPos).getBlock() instanceof CableBlock)) {
                placedPos = clickedPos.relative(context.getClickedFace());
            }
            if (level.getBlockState(placedPos).getBlock() instanceof CableBlock) {
                addToCable(level, placedPos);
            }
        }
        return result;
    }

    private boolean addToCable(Level level, BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof CableBlockEntity be)) return false;
        if (!be.canAddNode(cableType)) return false;

        CableNode node = cableType.getFactory().createNode(level, be, new ArrayList<>());
        node.updateConnections(level, pos);
        be.addNode(cableType, node);

        if (level instanceof ServerLevel serverLevel) {
            CableNetworks networks = CableNetworks.get(serverLevel);
            NetworkManager manager = networks.getOrCreateManager(cableType);
            manager.onNodeAdded(pos, node, serverLevel);
        }

        return true;
    }
}

package com.fabrica.cable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class WrenchItem extends Item {

    public WrenchItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Direction face = context.getClickedFace();

        if (!(state.getBlock() instanceof CableBlock)) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        if (!(level.getBlockEntity(pos) instanceof CableBlockEntity be)) return InteractionResult.FAIL;

        if (context.isSecondaryUseActive()) {
            return removeLastCable(be, level, pos);
        }

        return cycleConnection(be, level, pos, state, face);
    }

    private InteractionResult removeLastCable(CableBlockEntity be, Level level, BlockPos pos) {
        for (int i = CableBlockEntity.MAX_NODES - 1; i >= 0; i--) {
            CableNodeSlot slot = be.getNodes()[i];
            if (slot == null) continue;

            if (level instanceof ServerLevel serverLevel) {
                CableNetworks networks = CableNetworks.get(serverLevel);
                NetworkManager manager = networks.getManager(slot.type());
                if (manager != null) {
                    manager.onNodeRemoved(pos, serverLevel);
                }
            }
            be.removeNode(slot.type());
            return InteractionResult.CONSUME;
        }
        return InteractionResult.FAIL;
    }

    private InteractionResult cycleConnection(CableBlockEntity be, Level level, BlockPos pos,
                                              BlockState state, Direction face) {
        for (int i = 0; i < CableBlockEntity.MAX_NODES; i++) {
            CableNodeSlot slot = be.getNodes()[i];
            if (slot == null) continue;

            if (!(slot.node() instanceof EnergyCableNode energyNode)) continue;

            ConnectionType current = energyNode.getConnectionType(face);
            ConnectionType next = switch (current) {
                case null -> ConnectionType.PIPE;
                case PIPE -> ConnectionType.BLOCK;
                default -> null;
            };
            energyNode.setConnectionType(face, next);

            updateBlockState(be, level, pos, state, face);

            if (level instanceof ServerLevel serverLevel) {
                CableNetworks networks = CableNetworks.get(serverLevel);
                NetworkManager manager = networks.getManager(slot.type());
                if (manager != null) {
                    manager.onNodeRemoved(pos, serverLevel);
                    manager.onNodeAdded(pos, slot.node(), serverLevel);
                }
            }

            return InteractionResult.CONSUME;
        }
        return InteractionResult.FAIL;
    }

    private static void updateBlockState(CableBlockEntity be, Level level, BlockPos pos,
                                         BlockState state, Direction face) {
        boolean anyConnected = false;
        for (CableNodeSlot s : be.getNodes()) {
            if (s != null && s.node().getConnectionType(face) != null) {
                anyConnected = true;
                break;
            }
        }
        BooleanProperty prop = getPropertyForDirection(face);
        level.setBlock(pos, state.setValue(prop, anyConnected), 3);
    }

    private static BooleanProperty getPropertyForDirection(Direction dir) {
        return switch (dir) {
            case DOWN  -> CableBlock.DOWN;
            case UP    -> CableBlock.UP;
            case NORTH -> CableBlock.NORTH;
            case SOUTH -> CableBlock.SOUTH;
            case WEST  -> CableBlock.WEST;
            case EAST  -> CableBlock.EAST;
        };
    }
}

package com.fabrica.cable;

import com.fabrica.api.energy.IEnergyConnectable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

public class CableBlock extends Block implements SimpleWaterloggedBlock, EntityBlock, IEnergyConnectable {

    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty EAST  = BooleanProperty.create("east");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty WEST  = BooleanProperty.create("west");
    public static final BooleanProperty UP    = BooleanProperty.create("up");
    public static final BooleanProperty DOWN  = BooleanProperty.create("down");
    public static final BooleanProperty WATERLOGGED = BooleanProperty.create("waterlogged");

    private static final double CORE_MIN = 5.0 / 16.0;
    private static final double CORE_MAX = 11.0 / 16.0;
    private static final double ARM_MIN = 6.0 / 16.0;
    private static final double ARM_MAX = 10.0 / 16.0;

    private static final VoxelShape CORE = Shapes.box(CORE_MIN, CORE_MIN, CORE_MIN, CORE_MAX, CORE_MAX, CORE_MAX);

    private static final VoxelShape ARM_DOWN  = Shapes.box(ARM_MIN, 0.0,       ARM_MIN, ARM_MAX, CORE_MIN, ARM_MAX);
    private static final VoxelShape ARM_UP    = Shapes.box(ARM_MIN, CORE_MAX,  ARM_MIN, ARM_MAX, 1.0,      ARM_MAX);
    private static final VoxelShape ARM_NORTH = Shapes.box(ARM_MIN, ARM_MIN,  0.0,      ARM_MAX, ARM_MAX,  CORE_MIN);
    private static final VoxelShape ARM_SOUTH = Shapes.box(ARM_MIN, ARM_MIN,  CORE_MAX, ARM_MAX, ARM_MAX,  1.0);
    private static final VoxelShape ARM_WEST  = Shapes.box(0.0,      ARM_MIN,  ARM_MIN, CORE_MIN, ARM_MAX, ARM_MAX);
    private static final VoxelShape ARM_EAST  = Shapes.box(CORE_MAX, ARM_MIN,  ARM_MIN, 1.0,      ARM_MAX, ARM_MAX);

    private static final VoxelShape[] SHAPE_CACHE = new VoxelShape[64];

    static {
        for (int mask = 0; mask < 64; mask++) {
            VoxelShape shape = CORE;
            if ((mask & 1)  != 0) shape = Shapes.or(shape, ARM_DOWN);
            if ((mask & 2)  != 0) shape = Shapes.or(shape, ARM_UP);
            if ((mask & 4)  != 0) shape = Shapes.or(shape, ARM_NORTH);
            if ((mask & 8)  != 0) shape = Shapes.or(shape, ARM_SOUTH);
            if ((mask & 16) != 0) shape = Shapes.or(shape, ARM_WEST);
            if ((mask & 32) != 0) shape = Shapes.or(shape, ARM_EAST);
            SHAPE_CACHE[mask] = shape;
        }
    }

    public CableBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(NORTH, false)
                .setValue(EAST, false)
                .setValue(SOUTH, false)
                .setValue(WEST, false)
                .setValue(UP, false)
                .setValue(DOWN, false)
                .setValue(WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN, WATERLOGGED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Level world = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        return this.defaultBlockState()
                .setValue(DOWN,  canConnect(world, pos, Direction.DOWN))
                .setValue(UP,    canConnect(world, pos, Direction.UP))
                .setValue(NORTH, canConnect(world, pos, Direction.NORTH))
                .setValue(SOUTH, canConnect(world, pos, Direction.SOUTH))
                .setValue(WEST,  canConnect(world, pos, Direction.WEST))
                .setValue(EAST,  canConnect(world, pos, Direction.EAST))
                .setValue(WATERLOGGED, world.getFluidState(pos).getType() == Fluids.WATER);
    }

    @Override
    public BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess tickAccess,
                                  BlockPos pos, Direction direction, BlockPos neighborPos,
                                  BlockState neighborState, RandomSource random) {
        return state
                .setValue(getPropertyForDirection(direction), canConnect(neighborState, direction))
                .setValue(WATERLOGGED, level.getFluidState(pos).getType() == Fluids.WATER);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            CableBlockEntity be = getBlockEntity(level, pos);
            if (be != null) {
                for (CableNodeSlot slot : be.getNodes()) {
                    if (slot != null) {
                        slot.node().updateConnections(level, pos);
                        CableNetworks networks = CableNetworks.get(serverLevel);
                        NetworkManager manager = networks.getOrCreateManager(slot.type());
                        manager.onNodeAdded(pos, slot.node(), serverLevel);
                    }
                }
            }
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
                                   Orientation orientation, boolean movedByPiston) {
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            CableBlockEntity be = getBlockEntity(level, pos);
            if (be != null) {
                for (CableNodeSlot slot : be.getNodes()) {
                    if (slot != null) {
                        slot.node().updateConnections(level, pos);
                        CableNetworks networks = CableNetworks.get(serverLevel);
                        NetworkManager manager = networks.getManager(slot.type());
                        if (manager != null) {
                            manager.onNodeRemoved(pos, serverLevel);
                            manager.onNodeAdded(pos, slot.node(), serverLevel);
                        }
                    }
                }
            }
        }
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CableBlockEntity(pos, state);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter view, BlockPos pos, CollisionContext context) {
        int mask = 0;
        if (state.getValue(DOWN))  mask |= 1;
        if (state.getValue(UP))    mask |= 2;
        if (state.getValue(NORTH)) mask |= 4;
        if (state.getValue(SOUTH)) mask |= 8;
        if (state.getValue(WEST))  mask |= 16;
        if (state.getValue(EAST))  mask |= 32;
        return SHAPE_CACHE[mask];
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter view, BlockPos pos, CollisionContext context) {
        return getShape(state, view, pos, context);
    }

    @Override
    public boolean hasDynamicShape() {
        return true;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        return List.of(new ItemStack(this));
    }

    @Override
    public boolean canConnectEnergy(Direction fromNeighborToUs) {
        return true;
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    private boolean canConnect(BlockState neighborState, Direction fromCableToNeighbor) {
        if (neighborState.getBlock() instanceof IEnergyConnectable connectable) {
            return connectable.canConnectEnergy(fromCableToNeighbor.getOpposite());
        }
        return neighborState.getBlock() instanceof CableBlock;
    }

    private boolean canConnect(BlockGetter world, BlockPos pos, Direction dir) {
        BlockPos neighborPos = pos.relative(dir);
        BlockState neighborState = world.getBlockState(neighborPos);

        if (canConnect(neighborState, dir)) {
            return true;
        }

        BlockEntity neighborEntity = world.getBlockEntity(neighborPos);
        return neighborEntity != null && !neighborEntity.isRemoved();
    }

    private static BooleanProperty getPropertyForDirection(Direction dir) {
        return switch (dir) {
            case DOWN  -> DOWN;
            case UP    -> UP;
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case WEST  -> WEST;
            case EAST  -> EAST;
        };
    }

    private static CableBlockEntity getBlockEntity(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        return be instanceof CableBlockEntity cableBE ? cableBE : null;
    }
}

package com.fabrica.conduit.impl;

import com.fabrica.conduit.FabricaPipes;
import com.fabrica.conduit.api.PipeNetworkNode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * The pipe block. Hosts up to three pipe types in a single block,
 * which are stored in the PipeBlockEntity.
 */
public class PipeBlock extends Block implements EntityBlock, SimpleWaterloggedBlock {
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

	public PipeBlock(Properties settings) {
		super(settings
				.isValidSpawn((state, level, pos, entityType) -> false)
				.noOcclusion());
		this.registerDefaultState(this.defaultBlockState().setValue(WATERLOGGED, false));
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new PipeBlockEntity(pos, state);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(WATERLOGGED));
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockPos pos = context.getClickedPos();
		FluidState fluidState = context.getLevel().getFluidState(pos);
		return this.defaultBlockState().setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
	}

	@Override
	protected FluidState getFluidState(BlockState state) {
		return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
	}

	@Override
	protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
			Direction directionToNeighbour, BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
		if (state.getValue(WATERLOGGED)) {
			ticks.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
		}
		return super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random);
	}

	@Override
	public boolean canPlaceLiquid(@Nullable LivingEntity user, BlockGetter level, BlockPos pos, BlockState state, Fluid type) {
		return SimpleWaterloggedBlock.super.canPlaceLiquid(user, level, pos, state, type);
	}

	@Override
	public boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState state, FluidState fluidState) {
		return SimpleWaterloggedBlock.super.placeLiquid(level, pos, state, fluidState);
	}

	private static boolean isPartHit(VoxelShape shape, BlockHitResult hit) {
		var pos = hit.getBlockPos();
		Vec3 posInBlock = hit.getLocation().subtract(pos.getX(), pos.getY(), pos.getZ());
		for (AABB box : shape.toAabbs()) {
			// move slightly towards box center
			Vec3 dir = box.getCenter().subtract(posInBlock).normalize().scale(1e-4);
			if (box.contains(posInBlock.add(dir))) {
				return true;
			}
		}
		return false;
	}

	@Nullable
	public static PipeVoxelShape getHitPart(Level level, BlockPos pos, BlockHitResult hit) {
		return level.getBlockEntity(pos) instanceof PipeBlockEntity pipe ? getHitPart(pipe, hit) : null;
	}

	@Nullable
	private static PipeVoxelShape getHitPart(PipeBlockEntity pipe, BlockHitResult hit) {
		for (PipeVoxelShape partShape : pipe.getPartShapes()) {
			if (isPartHit(partShape.shape, hit)) {
				return partShape;
			}
		}
		return null;
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos blockPos, Player player,
			InteractionHand hand, BlockHitResult hit) {
		// Pipe placement and connection additions are handled by PipeItem.useOn.
		return InteractionResult.TRY_WITH_EMPTY_HAND;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos blockPos, Player player, BlockHitResult hit) {
		if (!(world.getBlockEntity(blockPos) instanceof PipeBlockEntity pipeEntity) || pipeEntity.getNodes().isEmpty()) {
			return InteractionResult.PASS;
		}

		PipeVoxelShape partShape = getHitPart(pipeEntity, hit);
		if (partShape == null) {
			return InteractionResult.PASS;
		}

		SoundType group = state.getSoundType();
		Vec3 hitPos = hit.getLocation();

		if (player.isShiftKeyDown()) {
			// Remove the pipe part and drop it.
			boolean removeBlock = pipeEntity.connections.size() == 1;
			if (!world.isClientSide()) {
				pipeEntity.removePipeAndDropContainedItems(partShape.type);
			}
			if (removeBlock) {
				world.setBlockAndUpdate(blockPos, Blocks.AIR.defaultBlockState());
			}
			world.updateNeighborsAt(blockPos, Blocks.AIR);
			if (!world.isClientSide()) {
				world.addFreshEntity(new ItemEntity(world, hitPos.x, hitPos.y, hitPos.z,
						new ItemStack(FabricaPipes.getPipeItem(partShape.type))));
			}
			world.playSound(player, blockPos, group.getBreakSound(), SoundSource.BLOCKS, (group.getVolume() + 1.0F) / 2.0F,
					group.getPitch() * 0.8F);
		} else {
			// Toggle a connection: click the center to add a connection towards the clicked side,
			// click an existing side connector to remove it.
			if (partShape.direction == null) {
				if (!world.isClientSide()) {
					pipeEntity.addConnection(player, partShape.type, hit.getDirection());
				} else {
					world.playSound(player, blockPos, group.getPlaceSound(), SoundSource.BLOCKS,
							(group.getVolume() + 1.0F) / 4.0F, group.getPitch() * 0.8F);
				}
			} else {
				if (!world.isClientSide()) {
					pipeEntity.removeConnection(partShape.type, partShape.direction);
				} else {
					world.playSound(player, blockPos, group.getBreakSound(), SoundSource.BLOCKS,
							(group.getVolume() + 1.0F) / 4.0F, group.getPitch() * 0.8F);
				}
			}
			world.updateNeighborsAt(blockPos, Blocks.AIR);
		}

		return InteractionResult.SUCCESS;
	}

	@Override
	protected List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
		PipeBlockEntity pipeEntity = (PipeBlockEntity) builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
		if (pipeEntity == null) {
			return List.of();
		}
		List<ItemStack> droppedStacks = new ArrayList<>();
		for (PipeNetworkNode node : pipeEntity.getNodes()) {
			droppedStacks.add(new ItemStack(FabricaPipes.getPipeItem(node.getType())));
			node.appendDroppedStacks(droppedStacks);
		}
		return droppedStacks;
	}

	@Override
	protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation,
			boolean movedByPiston) {
		if (!level.isClientSide() && level.getBlockEntity(pos) instanceof PipeBlockEntity pipe) {
			pipe.updateConnections();
		}
		super.neighborChanged(state, level, pos, block, orientation, movedByPiston);
	}

	@Override
	protected int getLightDampening(BlockState state) {
		return 0;
	}

	@Override
	public boolean hasDynamicShape() {
		return true;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		BlockEntity be = world.getBlockEntity(pos);
		if (!(be instanceof PipeBlockEntity entity)) {
			return PipeBlockEntity.DEFAULT_SHAPE;
		}
		return entity.currentCollisionShape;
	}

	@Override
	protected VoxelShape getOcclusionShape(BlockState state) {
		return Shapes.empty();
	}

	@Override
	protected boolean isPathfindable(BlockState state, PathComputationType type) {
		return false;
	}

	@Override
	protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
		if (level.getBlockEntity(pos) instanceof PipeBlockEntity pipe) {
			pipe.stateReplaced = true;
		}
	}
}

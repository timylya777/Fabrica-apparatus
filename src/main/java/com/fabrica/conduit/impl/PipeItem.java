package com.fabrica.conduit.impl;

import com.fabrica.conduit.FabricaPipes;
import com.fabrica.conduit.api.PipeNetworkData;
import com.fabrica.conduit.api.PipeNetworkType;
import com.fabrica.conduit.electricity.ElectricityNetworkData;
import com.fabrica.conduit.fluid.FluidNetworkData;
import com.fabrica.conduit.item.ItemNetworkData;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jetbrains.annotations.Nullable;

/**
 * The item for a pipe type. Right-clicking a pipe block adds the pipe to it,
 * right-clicking a pipe block that already contains the pipe type adds a
 * connection towards the clicked side, otherwise the pipe block is placed.
 */
public class PipeItem extends Item {
	public final PipeNetworkType type;
	public final PipeNetworkData defaultData;

	public PipeItem(Properties settings, PipeNetworkType type, PipeNetworkData defaultData) {
		super(settings);
		this.type = type;
		this.defaultData = defaultData;
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		BlockPos placingPos = tryPlace(context);
		if (placingPos != null) {
			Level world = context.getLevel();
			Player player = context.getPlayer();

			// update adjacent pipes
			world.updateNeighborsAt(placingPos, Blocks.AIR);
			// remove one from stack
			ItemStack placementStack = context.getItemInHand();
			if (player != null && !player.getAbilities().instabuild) {
				placementStack.shrink(1);
			}
			// play placing sound
			BlockState newState = world.getBlockState(placingPos);
			SoundType group = newState.getSoundType();
			world.playSound(player, placingPos, group.getPlaceSound(), SoundSource.BLOCKS, (group.getVolume() + 1.0F) / 2.0F,
					group.getPitch() * 0.8F);

			return InteractionResult.SUCCESS;
		} else {
			// if we couldn't place a pipe, we try to add a connection instead
			placingPos = context.getClickedPos().relative(context.getClickedFace());
			Level world = context.getLevel();
			BlockEntity entity = world.getBlockEntity(placingPos);
			if (entity instanceof PipeBlockEntity pipeEntity) {
				if (pipeEntity.connections.containsKey(type)) {
					if (!world.isClientSide()) {
						pipeEntity.addConnection(context.getPlayer(), type, context.getClickedFace().getOpposite());
					}
					// update adjacent pipes
					world.updateNeighborsAt(placingPos, Blocks.AIR);
					// play placing sound
					BlockState newState = world.getBlockState(placingPos);
					SoundType group = newState.getSoundType();
					world.playSound(context.getPlayer(), placingPos, group.getPlaceSound(), SoundSource.BLOCKS,
							(group.getVolume() + 1.0F) / 2.0F, group.getPitch() * 0.8F);
					return InteractionResult.SUCCESS;
				}
			}
		}
		return super.useOn(context);
	}

	// Try placing the pipe and registering the new pipe to the entity, returns null
	// if it failed
	@Nullable
	private BlockPos tryPlace(UseOnContext context) {
		BlockPos hitPos = context.getClickedPos();
		BlockPos adjacentPos = hitPos.relative(context.getClickedFace());
		if (tryPlaceAt(context, hitPos)) {
			return hitPos;
		} else if (tryPlaceAt(context, adjacentPos)) {
			return adjacentPos;
		} else {
			return null;
		}
	}

	/**
	 * Try adding the pipe to an existing block entity, or replacing the current
	 * state if that was not possible.
	 *
	 * @return True if succeeded, false otherwise.
	 */
	private boolean tryPlaceAt(UseOnContext context, BlockPos pos) {
		Level world = context.getLevel();
		// If there is a block entity we try to add the pipe.
		BlockEntity be = world.getBlockEntity(pos);
		if (be instanceof PipeBlockEntity pipeBe) {
			if (pipeBe.canAddPipe(type)) {
				if (!world.isClientSide()) {
					pipeBe.addPipe(type, defaultData.clone());
				}
				return true;
			}
		}
		// Otherwise we try replacing the target block.
		if (canPlace(context, pos)) {
			boolean waterLog = context.getLevel().getFluidState(pos).getType() == Fluids.WATER;

			// neighbor update is handled later
			world.setBlock(pos, FabricaPipes.PIPE_BLOCK.defaultBlockState().setValue(PipeBlock.WATERLOGGED, waterLog), 3);
			if (!world.isClientSide()) {
				PipeBlockEntity pipeBe = (PipeBlockEntity) world.getBlockEntity(pos);
				pipeBe.addPipe(type, defaultData.clone());
			}
			return true;
		}
		return false;
	}

	private static boolean canPlace(UseOnContext ctx, BlockPos pos) {
		BlockState state = FabricaPipes.PIPE_BLOCK.defaultBlockState();
		CollisionContext shapeContext = ctx.getPlayer() == null ? CollisionContext.empty() : CollisionContext.of(ctx.getPlayer());
		return ctx.getLevel().getBlockState(pos).canBeReplaced(new BlockPlaceContext(ctx)) && state.canSurvive(ctx.getLevel(), pos)
				&& ctx.getLevel().isUnobstructed(state, pos, shapeContext);
	}

	public boolean isItemPipe() {
		return this.defaultData instanceof ItemNetworkData;
	}

	public boolean isFluidPipe() {
		return this.defaultData instanceof FluidNetworkData;
	}

	public boolean isCable() {
		return this.defaultData instanceof ElectricityNetworkData;
	}
}

package com.fabrica.conduit.impl;

import com.fabrica.conduit.FabricaPipes;
import com.fabrica.conduit.api.PipeEndpointType;
import com.fabrica.conduit.api.PipeNetworkData;
import com.fabrica.conduit.api.PipeNetworkType;
import com.fabrica.conduit.electricity.ElectricityNetworkData;
import com.fabrica.conduit.fluid.FluidNetworkData;
import com.fabrica.conduit.item.ItemNetworkData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
 * Отвечает за предмет конкретного типа трубы: размещение новых труб
 * (добавление в существующий PipeBlockEntity или установка нового блока),
 * добавление соединений между трубами, разрыв связей (shift+клик) и
 * переключение режима ввода/вывода соединения с машиной. Поля type и
 * defaultData описывают, какой тип сети создаёт этот предмет.
 */
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

	// Главная точка входа: клик предметом по миру. Логика по приоритету:
	// shift+клик — только разрыв соединений труб; иначе попытка сменить режим
	// ввода/вывода, затем размещение трубы или добавление соединения.
	@Override
	public InteractionResult useOn(UseOnContext context) {
		Player player = context.getPlayer();
		if (player != null && player.isShiftKeyDown()) {
			// Shift+click only breaks connections between pipes, it never places
			// pipes.
			if (tryBreakConnection(context)) {
				Level world = context.getLevel();
				SoundType group = world.getBlockState(context.getClickedPos()).getSoundType();
				world.playSound(player, context.getClickedPos(), group.getBreakSound(), SoundSource.BLOCKS,
						(group.getVolume() + 1.0F) / 2.0F, group.getPitch() * 0.8F);
				return InteractionResult.SUCCESS;
			}
			return InteractionResult.PASS;
		}

		// Like MI, clicking a connected part of an item/fluid pipe with the pipe
		// item cycles the mode: arrow into the block (insert), out of the block
		// (extract), or both.
		if (tryCycleMode(context)) {
			Level world = context.getLevel();
			SoundType group = world.getBlockState(context.getClickedPos()).getSoundType();
			world.playSound(context.getPlayer(), context.getClickedPos(), group.getPlaceSound(), SoundSource.BLOCKS,
					(group.getVolume() + 1.0F) / 4.0F, group.getPitch());
			return InteractionResult.SUCCESS;
		}

		BlockPos placingPos = tryPlace(context);
		if (placingPos != null) {
			Level world = context.getLevel();
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

	// Try breaking a connection between two pipes on the clicked side, returns
	// true if a connection was found and removed
	// Ищет соединение труба-труба на кликнутой стороне: сначала в кликнутом
	// блоке, затем «сквозь» клик — в блоке за ним. Возвращает true, если
	// соединение найдено и разорвано.
	private boolean tryBreakConnection(UseOnContext context) {
		Level world = context.getLevel();
		BlockPos clickedPos = context.getClickedPos();
		Direction clickedFace = context.getClickedFace();
		// Direct click on a pipe, or click through to the pipe behind
		if (breakConnectionAt(world, clickedPos, clickedFace)) {
			return true;
		}
		return breakConnectionAt(world, clickedPos.relative(clickedFace), clickedFace.getOpposite());
	}

	// Разрывает соединение PIPE на стороне direction у конкретного блока
	// (если оно есть), через PipeBlockEntity.removeConnection.
	private boolean breakConnectionAt(Level world, BlockPos pos, Direction direction) {
		if (!(world.getBlockEntity(pos) instanceof PipeBlockEntity pipeEntity)) {
			return false;
		}
		PipeEndpointType[] connections = pipeEntity.connections.get(type);
		if (connections == null || connections[direction.get3DDataValue()] != PipeEndpointType.PIPE) {
			return false;
		}
		if (!world.isClientSide()) {
			pipeEntity.removeConnection(type, direction);
		}
		return true;
	}

	// Try cycling the import/export mode of a machine connection on the clicked
	// side, returns true if a connection mode was cycled.
	// Ищет соединение с машиной на кликнутой стороне для смены режима
	// ввода/вывода; кабели (электричество) этот режим не имеют.
	private boolean tryCycleMode(UseOnContext context) {
		if (isCable()) {
			return false;
		}
		Level world = context.getLevel();
		BlockPos clickedPos = context.getClickedPos();
		Direction clickedFace = context.getClickedFace();
		// Direct click on a pipe, or click through to the pipe behind
		if (cycleModeAt(world, clickedPos, clickedFace)) {
			return true;
		}
		return cycleModeAt(world, clickedPos.relative(clickedFace), clickedFace.getOpposite());
	}

	// Переключает режим соединения с машиной на стороне direction (только
	// не-PIPE соединения), через PipeBlockEntity.cycleConnectionMode.
	private boolean cycleModeAt(Level world, BlockPos pos, Direction direction) {
		if (!(world.getBlockEntity(pos) instanceof PipeBlockEntity pipeEntity)) {
			return false;
		}
		// Only machine connections can be cycled, pipe links cannot.
		PipeEndpointType[] connections = pipeEntity.connections.get(type);
		if (connections == null || connections[direction.get3DDataValue()] == null
				|| connections[direction.get3DDataValue()] == PipeEndpointType.PIPE) {
			return false;
		}
		if (!world.isClientSide()) {
			pipeEntity.cycleConnectionMode(type, direction);
		}
		return true;
	}

	// Try placing the pipe and registering the new pipe to the entity, returns null
	// if it failed
	// Пытается разместить трубу: сначала в кликнутом блоке, затем в соседнем
	// за гранью клика. Возвращает позицию удачного размещения или null.
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
	// Добавляет тип трубы в существующий PipeBlockEntity (если можно), иначе
	// заменяет целевой блок новым блоком трубы с учётом waterlogging и создаёт
	// в нём первый узел (addPipe).
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

	// Проверка, можно ли разместить блок трубы в позиции: блок заменяемый,
	// может стоять в этом месте и не пересекается с объектами.
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

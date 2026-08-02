package com.fabrica.conduit.impl;

import com.fabrica.conduit.api.PipeNetworkType;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Отвечает за связку «форма части трубы + её смысл»: хранит VoxelShape,
 * тип трубы (PipeNetworkType), направление коннектора (null — центр) и
 * признак того, что клик по этой части открывает GUI. Используется для
 * обработки кликов (PipeBlock.getHitPart) и для пересборки коллизионной формы.
 */
/**
 * A voxel shape and the part of the pipe it represents.
 */
public class PipeVoxelShape {
	/**
	 * The shape.
	 */
	public final VoxelShape shape;
	/**
	 * The network type.
	 */
	public final PipeNetworkType type;
	/**
	 * If null, the center of the pipe. Otherwise, the connector in the given
	 * direction.
	 */
	@Nullable
	public final Direction direction;

	/**
	 * Whether this pipe being right-clicked opens a gui.
	 */
	final boolean opensGui;

	PipeVoxelShape(VoxelShape shape, PipeNetworkType type, @Nullable Direction direction, boolean opensGui) {
		this.shape = shape;
		this.type = type;
		this.direction = direction;
		this.opensGui = opensGui;
	}
}

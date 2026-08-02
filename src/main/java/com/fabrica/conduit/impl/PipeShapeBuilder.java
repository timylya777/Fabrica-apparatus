package com.fabrica.conduit.impl;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Отвечает за превращение «маршрутов» PipePartBuilder в готовые VoxelShape:
 * переопределяет drawPipe, собирая из каждого сегмента прямоугольную коробку
 * (через две угловые точки) и объединяя её с общей формой. Используется в
 * статическом кэше форм PipeBlockEntity.
 */
/**
 * Reusing the PipePartBuilder to generate shapes for the pipe parts.
 */
public class PipeShapeBuilder extends PipePartBuilder {
	private VoxelShape shape;

	PipeShapeBuilder(int slotPos, Direction direction) {
		super(slotPos, direction);
		shape = Shapes.empty();
	}

	// Рисует один сегмент трубы: прямоугольник от нижнего ближнего угла
	// (pos минус половина ширины по up и right) до верхнего дальнего
	// (плюс длина по facing).
	@Override
	protected void drawPipe(float length, Intent intent, boolean end) {
		Vec3 up = up();
		addShape(pos.add(up.scale(SIDE / 2)).add(right.scale(SIDE / 2)),
				pos.subtract(up.scale(SIDE / 2)).subtract(right.scale(SIDE / 2)).add(facing.scale(length)));
	}

	/**
	 * Add a shape to the current shape using two corners.
	 */
	// Объединяет коробку, заданную двумя углами, с накопленной формой.
	private void addShape(Vec3 c1, Vec3 c2) {
		double x = Math.min(c1.x, c2.x), y = Math.min(c1.y, c2.y), z = Math.min(c1.z, c2.z);
		double X = Math.max(c1.x, c2.x), Y = Math.max(c1.y, c2.y), Z = Math.max(c1.z, c2.z);
		shape = Shapes.or(shape, Shapes.box(x, y, z, X, Y, Z));
	}

	/**
	 * Retrieve the built shape.
	 */
	VoxelShape getShape() {
		return shape;
	}

	/**
	 * Draw the center connector (starting from whatever direction).
	 */
	// Центральный коннектор: короткий сегмент в середине блока, который
	// соединяет все трубы этого типа.
	public void centerConnector() {
		moveForward(-SIDE);
		drawPipe(SIDE, null);
	}
}

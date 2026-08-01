package com.fabrica.conduit.impl;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Reusing the PipePartBuilder to generate shapes for the pipe parts.
 */
public class PipeShapeBuilder extends PipePartBuilder {
	private VoxelShape shape;

	PipeShapeBuilder(int slotPos, Direction direction) {
		super(slotPos, direction);
		shape = Shapes.empty();
	}

	@Override
	protected void drawPipe(float length, Intent intent, boolean end) {
		Vec3 up = up();
		addShape(pos.add(up.scale(SIDE / 2)).add(right.scale(SIDE / 2)),
				pos.subtract(up.scale(SIDE / 2)).subtract(right.scale(SIDE / 2)).add(facing.scale(length)));
	}

	/**
	 * Add a shape to the current shape using two corners.
	 */
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
	public void centerConnector() {
		moveForward(-SIDE);
		drawPipe(SIDE, null);
	}
}

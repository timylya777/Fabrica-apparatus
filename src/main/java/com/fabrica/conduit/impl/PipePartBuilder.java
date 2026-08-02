package com.fabrica.conduit.impl;

import com.fabrica.conduit.api.PipeEndpointType;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import static net.minecraft.core.Direction.AxisDirection;

/**
 * Отвечает за построение геометрии частей трубы: абстрактный «маршрутизатор»
 * сегментов трубы внутри блока. Держит текущую позицию (pos), направление
 * (facing) и орты (right/up) и умеет рисовать прямые линии, короткие и длинные
 * повороты, двигаться вперёд и поворачиваться. Конкретные подклассы решают,
 * что делать с сегментами (например, PipeShapeBuilder строит VoxelShape).
 * Статические методы getSlotPos/getRenderType/getInitialDirection определяют
 * раскладку пяти слотов труб и тип сегмента по конфигурации соединений.
 */
/**
 * A class that can build pipe model parts using a simple interface.
 */
public abstract class PipePartBuilder {
	/**
	 * The width of a pipe.
	 */
	protected static final float SIDE = 2.0f / 16;
	/**
	 * The spacing between two pipes.
	 */
	protected static final float SPACING = 1.0f / 16;
	/**
	 * The distance between the side of the block and the first of the five pipe
	 * slots.
	 */
	protected static final float FIRST_POS = (1.0f - 5 * SIDE - 4 * SPACING) / 2;
	protected Vec3 pos;
	protected Vec3 facing;
	protected Vec3 right;

	// Инициализация: задаёт направление сегмента, стартовую позицию внутри
	// блока по номеру слота, подбирает орт right так, чтобы right и up смотрели
	// внутрь блока, и отодвигается из центрального куба.
	protected PipePartBuilder(int slotPos, Direction direction) {
		this.facing = direction.getUnitVec3();
		// initial position + half pipe + slotPos * width
		float position = (1.0f - 3 * SIDE - 2 * SPACING) / 2.0f + SIDE / 2.0f + slotPos * (SIDE + SPACING);
		this.pos = new Vec3(position, position, position);
		// Find a suitable right direction (both right and up must face inside of the
		// block).
		for (Direction d : Direction.values()) {
			this.right = d.getUnitVec3();
			if (isTowardsInside(this.right) && isTowardsInside(up()))
				break;
		}
		// Move out of the center cube.
		moveForward(SIDE / 2);
	}

	/**
	 * Find out whether the axis direction is far from the sides of the block.
	 */
	// Проверяет, что направление «далеко» от стенок блока (внутри).
	protected boolean isTowardsInside(Vec3 direction) {
		return distanceToSide(direction) > 0.5f - 1e-6;
	}

	/**
	 * Get the distance along some axis direction to the nearest block side.
	 */
	// Расстояние от текущей позиции до ближайшей стенки блока вдоль оси.
	protected float distanceToSide(Vec3 direction) {
		float p = (float) direction.dot(pos);
		if (p > 0) {
			return 1 - p;
		} else {
			return -p;
		}
	}

	/**
	 * Draw a 5-sided pipe.
	 */
	protected final void drawPipe(float length, Intent intent) {
		drawPipe(length, intent, true);
	}

	/**
	 * Draw a pipe.
	 */
	protected abstract void drawPipe(float length, Intent intent, boolean end);

	/**
	 * Move forward.
	 */
	// Сдвигает текущую позицию вперёд по facing.
	void moveForward(float amount) {
		this.pos = this.pos.add(this.facing.scale(amount));
	}

	/**
	 * Get up vector.
	 */
	protected Vec3 up() {
		return right.cross(facing);
	}

	/**
	 * Rotate clockwise around the facing axis.
	 */
	protected void rotateCw() {
		right = up().scale(-1);
	}

	/**
	 * Turn 90° up.
	 */
	protected void turnUp() {
		facing = up();
	}

	/**
	 * Draw a straight line.
	 */
	// Прямая линия: при reduced сдвигаемся на ширину+зазор, рисуем до стенки.
	public void straightLine(boolean reduced, boolean end) {
		if (reduced)
			moveForward(SIDE + SPACING);
		drawPipe(distanceToSide(facing), Intent.STRAIGHT, end);
	}

	/**
	 * Draw a short bend.
	 */
	// Короткий поворот: три колена под 90° (горизонталь — вертикаль —
	// перпендикуляр) с учётом конфликтов соседних труб (Intent.BEND_CONFLICTING).
	public void shortBend(boolean reduced, boolean end) {
		if (reduced)
			moveForward(SIDE + SPACING);
		// horizontal
		float dist = FIRST_POS + 2 * SIDE + SPACING;
		float advDist = distanceToSide(facing) - dist;
		boolean bendConflicting = advDist + SIDE < 0;
		drawPipe(advDist + SIDE, Intent.BEND);
		moveForward(advDist + SIDE / 2);
		turnUp();
		rotateCw();
		// vertical
		moveForward(SIDE / 2);
		drawPipe(SPACING + SIDE, bendConflicting ? Intent.BEND_CONFLICTING : Intent.BEND, !bendConflicting);
		moveForward(SPACING + SIDE / 2);
		turnUp();
		rotateCw();
		// perpendicular
		moveForward(SIDE / 2);
		drawPipe(SPACING + SIDE, Intent.BEND);
		moveForward(SPACING + SIDE / 2);
		turnUp();
		// again vertical
		moveForward(SIDE / 2);
		drawPipe(distanceToSide(facing), Intent.STRAIGHT, end);
	}

	/**
	 * Draw a short bend, on the extra slot.
	 */
	// Короткий поворот в «дальнем» слоте (другая стартовая позиция).
	public void farShortBend(boolean reduced, boolean end) {
		if (reduced)
			moveForward(SIDE + SPACING);
		// horizontal
		float dist = FIRST_POS + SIDE;
		float advDist = distanceToSide(facing) - dist;
		drawPipe(advDist + SIDE, Intent.BEND);
		moveForward(advDist + SIDE / 2);
		turnUp();
		rotateCw();
		// vertical
		moveForward(SIDE / 2);
		drawPipe(SPACING + SIDE, Intent.BEND);
		moveForward(SPACING + SIDE / 2);
		turnUp();
		rotateCw();
		// perpendicular
		moveForward(SIDE / 2);
		drawPipe(SPACING + SIDE, Intent.BEND);
		moveForward(SPACING + SIDE / 2);
		turnUp();
		// again vertical
		moveForward(SIDE / 2);
		drawPipe(distanceToSide(facing), Intent.STRAIGHT, end);
	}

	/**
	 * Draw a long bend.
	 */
	// Длинный поворот: как короткий, но с удлинёнными коленами, чтобы обойти
	// трубу в соседнем слоте.
	public void longBend(boolean reduced, boolean end) {
		if (reduced)
			moveForward(SIDE + SPACING);
		// horizontal
		float dist = FIRST_POS + SIDE;
		float advDist = distanceToSide(facing) - dist;
		drawPipe(advDist + SIDE, Intent.BEND);
		moveForward(advDist + SIDE / 2);
		turnUp();
		rotateCw();
		// vertical
		moveForward(SIDE / 2);
		drawPipe(2 * SPACING + 2 * SIDE, Intent.BEND);
		moveForward(2 * SPACING + 1.5f * SIDE);
		turnUp();
		rotateCw();
		// perpendicular
		moveForward(SIDE / 2);
		drawPipe(2 * SPACING + 2 * SIDE, Intent.BEND);
		moveForward(2 * SPACING + 1.5f * SIDE);
		turnUp();
		// again vertical
		moveForward(SIDE / 2);
		drawPipe(distanceToSide(facing), Intent.STRAIGHT, end);
	}

	// Переводит логический слот (порядок типов труб) в позицию на сетке пяти
	// слотов: слот 0 — центр, 1 — левый край, 2 — правый край.
	public static int getSlotPos(int slot) {
		return slot == 0 ? 1 : slot == 1 ? 0 : 2;
	}

	/**
	 * Get the type of a connection.
	 */
	// Определяет тип сегмента соединения для рендера: 0 — нет соединения,
	// 1 — прямая линия (к блоку или по умолчанию), 2 — короткий поворот,
	// 3 — короткий поворот «далеко», 4 — длинный поворот. Счётчик connSlot —
	// сколько труб того же направления занимают предыдущие слоты.
	public static int getRenderType(int logicalSlot, Direction direction, PipeEndpointType[][] connections) {
		if (connections[logicalSlot][direction.get3DDataValue()] == null) {
			// no connection
			return 0;
		} else if (connections[logicalSlot][direction.get3DDataValue()] != PipeEndpointType.PIPE) {
			// straight line when connecting to a block
			return 1;
		} else {
			int connSlot = 0;
			for (int i = 0; i < logicalSlot; i++) {
				if (connections[i][direction.get3DDataValue()] != null) {
					connSlot++;
				}
			}
			if (logicalSlot == 1) {
				// short bend
				if (connSlot == 0) {
					return 2;
				}
			} else if (logicalSlot == 2) {
				if (connSlot == 0) {
					// short bend, but far if the direction is negative to avoid collisions in some cases.
					return direction.getAxisDirection() == AxisDirection.NEGATIVE ? 3 : 2;
				} else if (connSlot == 1) {
					// long bend
					return 4;
				}
			}
			// default to straight line
			return 1;
		}
	}

	/**
	 * Get the initial direction of a connection.
	 */
	// Возвращает начальное направление поворота для коротких изгибов в
	// дальних слотах (нужно для корректной геометрии рендера).
	public static Direction getInitialDirection(int logicalSlot, Direction connectionDirection, int renderType) {
		if (renderType == 2) { // only for short bend
			if (logicalSlot == 1) {
				if (connectionDirection == Direction.NORTH)
					return Direction.UP;
				if (connectionDirection == Direction.WEST)
					return Direction.SOUTH;
				if (connectionDirection == Direction.DOWN)
					return Direction.EAST;
			} else if (logicalSlot == 2) {
				if (connectionDirection == Direction.UP)
					return Direction.NORTH;
				if (connectionDirection == Direction.SOUTH)
					return Direction.WEST;
				if (connectionDirection == Direction.EAST)
					return Direction.DOWN;
			}
		}
		return connectionDirection;
	}

	/**
	 * Indicates why a particular pipe kind is being used
	 */
	public enum Intent {
		STRAIGHT,
		BEND,
		BEND_CONFLICTING,
	}
}

package com.fabrica.client.render.pipe;

import com.fabrica.conduit.impl.PipePartBuilder;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.List;

import static net.minecraft.core.Direction.*;

/**
 * Builds the pipe geometry (a port of MI's PipeMeshBuilder) directly into a
 * FRAPI QuadEmitter.
 */
public class PipeMeshBuilder extends PipePartBuilder {
	private final QuadEmitter emitter;
	private final Material.Baked material;
	private final int color;
	private final List<PipeMeshCache.InnerQuad> innerQuads;

	private final Vec3[] workPos = new Vec3[] { new Vec3(0, 0, 0), new Vec3(0, 0, 0), new Vec3(0, 0, 0), new Vec3(0, 0, 0) };
	private final float[][] workUv = new float[4][2];

	PipeMeshBuilder(QuadEmitter emitter, Material.Baked material, int color, int slotPos, Direction direction,
			@Nullable List<PipeMeshCache.InnerQuad> innerQuads) {
		super(slotPos, direction);
		this.emitter = emitter;
		this.material = material;
		this.color = color;
		this.innerQuads = innerQuads;
	}

	private static final float CULL_FACE_EPSILON = 0.00001f;

	/**
	 * Compute the four corners of a face of a unit cube, in 0..1 space. Same math
	 * as MI's ModelHelper.square.
	 */
	private void square(Direction nominalFace, float left, float bottom, float right, float top, float depth) {
		if (Math.abs(depth) < CULL_FACE_EPSILON) {
			depth = 0; // avoid any inconsistency for face quads
		}

		switch (nominalFace) {
			case UP:
				depth = 1 - depth;
				top = 1 - top;
				bottom = 1 - bottom;

			case DOWN:
				workPos[0] = new Vec3(left, depth, top);
				workPos[1] = new Vec3(left, depth, bottom);
				workPos[2] = new Vec3(right, depth, bottom);
				workPos[3] = new Vec3(right, depth, top);
				break;

			case EAST:
				depth = 1 - depth;
				left = 1 - left;
				right = 1 - right;

			case WEST:
				workPos[0] = new Vec3(depth, top, left);
				workPos[1] = new Vec3(depth, bottom, left);
				workPos[2] = new Vec3(depth, bottom, right);
				workPos[3] = new Vec3(depth, top, right);
				break;

			case SOUTH:
				depth = 1 - depth;
				left = 1 - left;
				right = 1 - right;

			case NORTH:
				workPos[0] = new Vec3(1 - left, top, depth);
				workPos[1] = new Vec3(1 - left, bottom, depth);
				workPos[2] = new Vec3(1 - right, bottom, depth);
				workPos[3] = new Vec3(1 - right, top, depth);
				break;
		}
	}

	private void quad(Direction direction, float left, float bottom, float right, float top, float depth) {
		// Already emit the fluid quad, the UV will be baked when rendering so it's not needed here.
		if (innerQuads != null) {
			square(direction, left, bottom, right, top, depth + 0.001f);
			innerQuads.add(new PipeMeshCache.InnerQuad(workPos[0], workPos[1], workPos[2], workPos[3], direction));
		}
		square(direction, left, bottom, right, top, depth);
	}

	/**
	 * Add a quad with four corners and the facing direction. It is important that 1
	 * and 4 be opposite corners! UVs are actually (u, v, whatever), relative to
	 * the sprite.
	 */
	private void quad(Vec3 facing, Vec3[] corners, Vec3[] uvs) {
		if (corners.length != 4 || uvs.length != 4) {
			throw new RuntimeException("This is a bug, please report!");
		}
		Vector3f c1 = corners[0].toVector3f();
		Vector3f c4 = corners[3].toVector3f();
		Direction direction = Direction.getApproximateNearest(facing.x, facing.y, facing.z);
		float x = Math.min(c1.x(), c4.x()), y = Math.min(c1.y(), c4.y()), z = Math.min(c1.z(), c4.z());
		float X = Math.max(c1.x(), c4.x()), Y = Math.max(c1.y(), c4.y()), Z = Math.max(c1.z(), c4.z());
		if (direction == UP)
			quad(UP, x, 1 - Z, X, 1 - z, 1 - Y);
		else if (direction == DOWN)
			quad(DOWN, x, z, X, Z, y);
		else if (direction == NORTH)
			quad(NORTH, 1 - X, y, 1 - x, Y, z);
		else if (direction == EAST)
			quad(EAST, 1 - Z, y, 1 - z, Y, 1 - X);
		else if (direction == SOUTH)
			quad(SOUTH, x, y, X, Y, 1 - Z);
		else
			quad(WEST, z, y, Z, Y, x);

		// Map the uvs onto the quad
		for (int i = 0; i < 4; ++i) {
			Vec3 vertexPos = workPos[i];
			for (int j = 0; j < 4; ++j) {
				if (vertexPos.subtract(corners[j]).lengthSqr() < 1e-6) {
					workUv[i][0] = (float) uvs[j].x();
					workUv[i][1] = (float) uvs[j].y();
				}
			}
		}

		emit(direction);
	}

	private void emit(Direction direction) {
		Vector3fc normal = direction.getUnitVec3f();
		for (int i = 0; i < 4; ++i) {
			emitter.pos(i, (float) workPos[i].x, (float) workPos[i].y, (float) workPos[i].z);
			emitter.uv(i, workUv[i][0] * 16.0f, workUv[i][1] * 16.0f);
			emitter.color(i, color);
			emitter.normal(i, normal);
		}
		emitter.nominalFace(direction)
				.cullFace(null)
				.chunkLayer(ChunkSectionLayer.CUTOUT)
				.materialBake(material, 0)
				.diffuseShade(true)
				.ambientOcclusion(TriState.TRUE)
				.emit();
	}

	private static final double COL_WIDTH = 1 / 8.0;
	private static final double[] BEND_COL = new double[] { 0, 1 / 8.0, 0, 2 / 8.0 };
	private static final double[] BEND_CONFLICTING_COL = new double[] { 0, 4 / 8.0, 0, 5 / 8.0 };
	private static final double[] STRAIGHT_COL = new double[] { 6 / 8.0, 6 / 8.0, 6 / 8.0, 6 / 8.0 };

	/**
	 * Draw a 4-sided pipe.
	 */
	@Override
	protected void drawPipe(float length, Intent intent, boolean end) {
		if (length <= 1e-9)
			return;
		// Four sides
		double[] cols = intent == Intent.STRAIGHT ? STRAIGHT_COL : intent == Intent.BEND ? BEND_COL : BEND_CONFLICTING_COL;
		for (int i = 0; i < 4; ++i) {
			if (intent != Intent.STRAIGHT && i == 0)
				length -= SIDE;
			double u = cols[i];
			Vec3 up = up();
			Vec3 base = pos.add(up.scale(SIDE / 2));
			quad(up, new Vec3[] { base.add(right.scale(SIDE / 2)), base.subtract(right.scale(SIDE / 2)),
					base.add(right.scale(SIDE / 2)).add(facing.scale(length)),
					base.subtract(right.scale(SIDE / 2)).add(facing.scale(length)), },
					new Vec3[] { new Vec3(u + COL_WIDTH, length, 0), new Vec3(u, length, 0), new Vec3(u + COL_WIDTH, 0, 0),
							new Vec3(u, 0, 0), });
			rotateCw();
			if (intent != Intent.STRAIGHT && i == 0)
				length += SIDE;
		}
		// End
		if (end) {
			Vec3 up = up();
			Vec3 base = pos.add(facing.scale(length));
			quad(facing,
					new Vec3[] { base.subtract(up.scale(SIDE / 2)).subtract(right.scale(SIDE / 2)),
							base.subtract(up.scale(SIDE / 2)).add(right.scale(SIDE / 2)),
							base.add(up.scale(SIDE / 2)).subtract(right.scale(SIDE / 2)),
							base.add(up.scale(SIDE / 2)).add(right.scale(SIDE / 2)), },
					intent == Intent.STRAIGHT
							? new Vec3[] { new Vec3(4 * COL_WIDTH, 0, 0), new Vec3(3 * COL_WIDTH, 0, 0), new Vec3(4 * COL_WIDTH, COL_WIDTH, 0),
									new Vec3(3 * COL_WIDTH, COL_WIDTH, 0), }
							: new Vec3[] { new Vec3(COL_WIDTH, 1, 0), new Vec3(0, 1, 0), new Vec3(COL_WIDTH, 1 - COL_WIDTH, 0),
									new Vec3(0, 1 - COL_WIDTH, 0), });
		}
	}

	private static final int[][] CENTER_PATTERNS = new int[][] { new int[] { 1, 0, 1, 0 }, new int[] { 0, 1, 1, 0 }, new int[] { 0, 0, 0, 0 },
			new int[] { 1, 1, 1, 1 }, new int[] { 1, 0, 1, 1 }, new int[] { 0, 0, 0, 1 }, };
	private static final double[][] CENTER_UVS = new double[][] { new double[] { 0, 0 }, new double[] { COL_WIDTH, 0 },
			new double[] { 3 * COL_WIDTH, 0 }, new double[] { 3 * COL_WIDTH, COL_WIDTH }, new double[] { 3 * COL_WIDTH, 2 * COL_WIDTH },
			new double[] { 3 * COL_WIDTH, 3 * COL_WIDTH }, };

	/**
	 * Draw a single connection face.
	 *
	 * @param directions: a bitset with the directions
	 */
	void noConnection(int directions) {
		if ((directions & (1 << Direction.getApproximateNearest(facing.x, facing.y, facing.z).get3DDataValue())) > 0) {
			return; // don't render when there is already a connection in this direction
		}
		// Get the 4 connections as '0's and '1's
		int[] sidesDirections = new int[4];
		for (int i = 0; i < 4; ++i) {
			Vec3 up = up();
			Direction sideDir = Direction.getApproximateNearest(up.x, up.y, up.z);
			sidesDirections[i] = (directions >> sideDir.get3DDataValue()) & 1;
			rotateCw();
		}
		// Try to match every pattern
		for (int i = 0; i < CENTER_PATTERNS.length; ++i) {
			// With every possible rotation
			rotations:
			for (int j = 0; j < 4; ++j) {
				for (int k = 0; k < 4; ++k) {
					if (CENTER_PATTERNS[i][k] != sidesDirections[(j + k) % 4]) {
						continue rotations;
					}
				}
				// Render the connection
				Vec3 up = up();
				Vec3[] vertices = new Vec3[] { pos.add(right.scale(SIDE / 2)).subtract(up.scale(SIDE / 2)),
						pos.add(right.scale(SIDE / 2)).add(up.scale(SIDE / 2)),
						pos.subtract(right.scale(SIDE / 2)).subtract(up.scale(SIDE / 2)),
						pos.subtract(right.scale(SIDE / 2)).add(up.scale(SIDE / 2)), };
				double u = CENTER_UVS[i][0];
				double v = CENTER_UVS[i][1];
				Vec3[] uvs = new Vec3[] { new Vec3(u, v + COL_WIDTH, 0), new Vec3(u, v, 0), new Vec3(u + COL_WIDTH, v + COL_WIDTH, 0),
						new Vec3(u + COL_WIDTH, v, 0), };
				for (int k = 0; k < j; ++k) {
					rotate(vertices);
				}
				quad(facing, vertices, uvs);
				return;
			}
		}
	}

	private void rotate(Vec3[] arr) {
		Vec3 tmp = arr[0];
		arr[0] = arr[2];
		arr[2] = arr[3];
		arr[3] = arr[1];
		arr[1] = tmp;
	}
}

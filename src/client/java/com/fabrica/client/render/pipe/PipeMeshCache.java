package com.fabrica.client.render.pipe;

import com.fabrica.conduit.api.PipeEndpointType;
import com.fabrica.conduit.impl.PipePartBuilder;
import net.fabricmc.fabric.api.client.renderer.v1.Renderer;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableMesh;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.MaterialBaker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

/**
 * Кэш готовых (baked) FRAPI-мешей для соединений труб и центральных
 * соединителей. Меши строятся лениво по ключам и переиспользуются при
 * повторной отрисовке одинаковых конфигураций; ключи устроены как в
 * PipeMeshCache из мода MI. Также отвечает за повторную эмиссию внутренних
 * квадов с жидкостью (спрайт и цвет жидкости) внутри заполненных труб.
 */
/**
 * Caches baked FRAPI meshes for the pipe connections and center connectors,
 * keyed like MI's PipeMeshCache.
 */
public class PipeMeshCache implements PipeRenderer {

	/** Ключ меша соединения: тип конечной точки, слот, направление, тип рендера и цвет. */
	private record ConnectionMeshKey(int endpointType, int logicalSlot, int directionId, int renderType, int color) {
	}

	/** Ключ меша центра: слот, битовая маска направлений соединений и цвет. */
	private record CenterMeshKey(int logicalSlot, int bitmask, int color) {
	}

	/** Готовый меш и захваченные внутренние квады (для отрисовки жидкости). */
	private record MeshData(Mesh pipeMesh, List<InnerQuad> innerQuads) {
	}

	/** Квад слегка внутри трубы: переизлучается со спрайтом и цветом жидкости, когда труба заполнена. */
	/**
	 * A quad slightly inside the pipe, re-emitted with the fluid sprite and color
	 * when the pipe contains a fluid, like MI does.
	 */
	record InnerQuad(Vec3 p0, Vec3 p1, Vec3 p2, Vec3 p3, Direction direction) {
		/** Возвращает вершину квада по индексу (0..3). */
		Vec3 position(int i) {
			return switch (i) {
				case 0 -> p0;
				case 1 -> p1;
				case 2 -> p2;
				default -> p3;
			};
		}
	}

	private final MaterialBaker materialBaker;
	private final Material[] materials;
	private final boolean innerQuads;

	/**
	 * The cached meshes for the connections. Indexed by: [endpoint type][logical
	 * slot][direction id]["render type" - 1]. "render type" is 0, 1, 2, 3 for
	 * straight, short bend, far short bend and long bend. Then it is 4, 5, 6, 7
	 * for conflict handling.
	 */
	private final ConcurrentMap<ConnectionMeshKey, MeshData> connectionMeshes = new ConcurrentHashMap<>(128, 0.5f);
	private final Function<ConnectionMeshKey, MeshData> connectionMeshBuilder;

	/**
	 * The meshes for the center connector. Indexed by: [logicalSlot][bitmask]. The
	 * bitmask stores for which direction there is a connection.
	 */
	private final ConcurrentMap<CenterMeshKey, MeshData> centerMeshes = new ConcurrentHashMap<>(128, 0.5f);
	private final Function<CenterMeshKey, MeshData> centerMeshBuilder;

	private static final java.util.Set<PipeMeshCache> ALL_CACHES = ConcurrentHashMap.newKeySet();

	/**
	 * Create a new {@link PipeMeshCache}. The materials array is indexed by
	 * endpoint type id (see {@link PipeEndpointType#getId()}).
	 *
	 * @param innerQuads Whether to add inner quads, e.g. for fluid rendering.
	 */
	/**
	 * Конструктор: сохраняет зависимости и регистрирует кэш в общем множестве
	 * {@link #ALL_CACHES}, затем настраивает ленивые построители мешей:
	 * для соединений — по типу рендера (прямая, короткий/дальний/длинный изгиб,
	 * со сниженной детализацией для конфликтных направлений) и для центра —
	 * по битовой маске направлений без соединений.
	 */
	public PipeMeshCache(MaterialBaker materialBaker, Material[] materials, boolean innerQuads) {
		this.materialBaker = materialBaker;
		this.materials = materials;
		this.innerQuads = innerQuads;
		ALL_CACHES.add(this);

		// Build the connection cache
		connectionMeshBuilder = key -> {
			int endpointType = key.endpointType();
			int logicalSlot = key.logicalSlot();
			Direction direction = Direction.from3DDataValue(key.directionId());
			int renderType = key.renderType();

			Material.Baked material = materialBaker.get(materials[endpointType], () -> "pipe model");

			MutableMesh mesh = Renderer.get().mutableMesh();
			QuadEmitter emitter = mesh.emitter();
			List<InnerQuad> capturedInnerQuads = innerQuads ? new ArrayList<>() : null;
			PipeMeshBuilder pmb = new PipeMeshBuilder(emitter, material, key.color(), PipePartBuilder.getSlotPos(logicalSlot),
					direction, capturedInnerQuads);
			boolean reduced = renderType >= 4;
			boolean end = endpointType != 0;
			int type = renderType % 4;
			if (type == 0) {
				pmb.straightLine(reduced, end);
			} else if (type == 1) {
				pmb.shortBend(reduced, end);
			} else if (type == 2) {
				pmb.farShortBend(reduced, end);
			} else {
				pmb.longBend(reduced, end);
			}
			return new MeshData(mesh.immutableCopy(), capturedInnerQuads == null ? List.of() : capturedInnerQuads);
		};

		// Build the center cache
		centerMeshBuilder = key -> {
			int logicalSlot = key.logicalSlot();
			int mask = key.bitmask();

			MutableMesh mesh = Renderer.get().mutableMesh();
			QuadEmitter emitter = mesh.emitter();
			for (Direction direction : Direction.values()) {
				PipeMeshBuilder pmb = new PipeMeshBuilder(emitter, materialBaker.get(materials[0], () -> "pipe model"),
						key.color(), PipePartBuilder.getSlotPos(logicalSlot), direction, null);
				pmb.noConnection(mask);
			}
			return new MeshData(mesh.immutableCopy(), List.of());
		};
	}

	/** Очищает все кэши мешей; вызывается при инвалидации состояния рендера (например, перезагрузке текстур). */
	public static void clearAll() {
		for (PipeMeshCache cache : ALL_CACHES) {
			cache.connectionMeshes.clear();
			cache.centerMeshes.clear();
		}
	}

	/**
	 * Главная точка отрисовки: вычисляет тип рендера и начальное направление
	 * для каждого соединения, разрешает спрайт/цвет жидкости (если труба
	 * заполнена), берёт готовые меши из кэша (с учётом конфликтов направлений)
	 * и выводит их в emitter, а также рисует центральный соединитель.
	 */
	@Override
	public void draw(
			QuadEmitter emitter,
			@Nullable BlockAndTintGetter view, @Nullable BlockPos pos,
			int logicalSlot, PipeEndpointType[][] connections,
			int color, @Nullable Object customData) {
		// The render type of the connections (0 for no connection, 1 for straight pipe,
		// 2 for short bend, etc...)
		int[] renderTypes = new int[6];
		// The initial direction of the connections
		Direction[] initialDirections = new Direction[6];
		// How many connections actually start in the specified direction
		int[] connectionsInDirection = new int[6];
		// A bitmask for the initial directions
		int directionsMask = 0;

		// Compute these variables
		for (Direction direction : Direction.values()) {
			int i = direction.get3DDataValue();
			renderTypes[i] = PipePartBuilder.getRenderType(logicalSlot, direction, connections);
			if (renderTypes[i] != 0) {
				initialDirections[i] = PipePartBuilder.getInitialDirection(logicalSlot, direction, renderTypes[i]);
				connectionsInDirection[initialDirections[i].get3DDataValue()]++;
				directionsMask |= 1 << initialDirections[i].get3DDataValue();
			}
		}

		// Resolve the fluid sprite and tint, if the pipe contains a fluid.
		Material.Baked stillMaterial = null;
		int fluidColor = 0xFFFFFFFF;
		if (customData instanceof FluidVariant fluid && !fluid.isBlank() && view != null && pos != null) {
			FluidState fluidState = fluid.getFluid().defaultFluidState();
			FluidModel fluidModel = Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(fluidState);
			stillMaterial = fluidModel.stillMaterial();
			fluidColor = fluidModel.tintSource().colorInWorld(fluidState.createLegacyBlock(), view, pos);
		}

		// Render every connection
		for (int i = 0; i < 6; ++i) {
			PipeEndpointType endpointType = connections[logicalSlot][i];
			if (endpointType != null) {
				int renderType = renderTypes[i] - 1;
				if (connectionsInDirection[initialDirections[i].get3DDataValue()] > 1) {
					renderType += 4; // Conflict handling
				}
				MeshData meshData = connectionMeshes.computeIfAbsent(
						new ConnectionMeshKey(endpointType.getId(), logicalSlot, i, renderType, color),
						connectionMeshBuilder);
				meshData.pipeMesh().outputTo(emitter);
				if (stillMaterial != null) {
					emitInnerQuads(emitter, meshData.innerQuads(), stillMaterial, fluidColor);
				}
			}
		}

		// Render the center connector
		MeshData centerMesh = centerMeshes.computeIfAbsent(new CenterMeshKey(logicalSlot, directionsMask, color), centerMeshBuilder);
		centerMesh.pipeMesh().outputTo(emitter);
	}

	/** Эмитит внутренние квады трубы со спрайтом жидкости и её цветом (полупрозрачный слой). */
	private void emitInnerQuads(QuadEmitter emitter, List<InnerQuad> innerQuads, Material.Baked material, int color) {
		for (InnerQuad quad : innerQuads) {
			Direction direction = quad.direction();
			for (int i = 0; i < 4; i++) {
				Vec3 p = quad.position(i);
				emitter.pos(i, (float) p.x, (float) p.y, (float) p.z);
				Vector2f uv = lockUvs(p, direction);
				emitter.uv(i, uv.x * 16.0f, uv.y * 16.0f);
				emitter.color(i, color);
				emitter.normal(i, direction.getUnitVec3f());
			}
			emitter.nominalFace(direction)
					.cullFace(null)
					.chunkLayer(ChunkSectionLayer.TRANSLUCENT)
					.materialBake(material, 0)
					.diffuseShade(true)
					.ambientOcclusion(TriState.TRUE)
					.emit();
		}
	}

	/** Переводит мировые координаты вершины в UV-координаты текстуры (0..1) для заданной грани. */
	private static Vector2f lockUvs(Vec3 pos, Direction face) {
		return switch (face) {
			case EAST -> new Vector2f(1 - (float) pos.z(), 1 - (float) pos.y());
			case WEST -> new Vector2f((float) pos.z(), 1 - (float) pos.y());
			case NORTH -> new Vector2f(1 - (float) pos.x(), 1 - (float) pos.y());
			case SOUTH -> new Vector2f((float) pos.x(), 1 - (float) pos.y());
			case DOWN -> new Vector2f((float) pos.x(), 1 - (float) pos.z());
			case UP -> new Vector2f((float) pos.x(), (float) pos.z());
		};
	}
}

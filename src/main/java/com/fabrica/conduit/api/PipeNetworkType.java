package com.fabrica.conduit.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * A pipe network type.
 */
public final class PipeNetworkType implements Comparable<PipeNetworkType> {
	public static final Codec<PipeNetworkType> CODEC = Identifier.CODEC.comapFlatMap(id -> {
		var type = get(id);
		if (type == null) {
			return DataResult.error(() -> "Unknown pipe network type: " + id);
		}
		return DataResult.success(type);
	}, type -> type.identifier);

	private final Identifier identifier;
	private final BiFunction<Integer, @Nullable PipeNetworkData, PipeNetwork> networkCtor;
	private final MapCodec<PipeNetworkData> dataCodec;
	private final Supplier<PipeNetworkNode> nodeCtor;
	/**
	 * A "serial number" allowing type comparison for rendering.
	 */
	private final int serialNumber;
	private final int color;
	private final boolean opensGui;
	public Object renderer;

	private static final Map<Identifier, PipeNetworkType> types = new HashMap<>();
	private static int nextSerialNumber = 0;

	private PipeNetworkType(
			Identifier identifier,
			BiFunction<Integer, PipeNetworkData, PipeNetwork> networkCtor,
			MapCodec<PipeNetworkData> dataCodec,
			Supplier<PipeNetworkNode> nodeCtor,
			int color,
			int serialNumber,
			boolean opensGui) {
		this.identifier = identifier;
		this.networkCtor = networkCtor;
		this.dataCodec = dataCodec;
		this.nodeCtor = nodeCtor;
		this.color = color;
		this.serialNumber = serialNumber;
		this.opensGui = opensGui;
	}

	public boolean opensGui() {
		return opensGui;
	}

	public Identifier getIdentifier() {
		return identifier;
	}

	BiFunction<Integer, PipeNetworkData, PipeNetwork> getNetworkCtor() {
		return networkCtor;
	}

	public MapCodec<PipeNetworkData> dataCodec() {
		return dataCodec;
	}

	public Supplier<PipeNetworkNode> getNodeCtor() {
		return nodeCtor;
	}

	public int getColor() {
		return color;
	}

	@Nullable
	public static PipeNetworkType get(Identifier identifier) {
		return types.get(identifier);
	}

	public static Map<Identifier, PipeNetworkType> getTypes() {
		return new HashMap<>(types);
	}

	public static <D> PipeNetworkType register(
			Identifier identifier,
			BiFunction<Integer, D, PipeNetwork> networkCtor,
			MapCodec<D> dataCodec,
			Supplier<PipeNetworkNode> nodeCtor,
			int color,
			boolean opensGui) {
		color |= 0xff000000;
		PipeNetworkType type = new PipeNetworkType(identifier, (BiFunction) networkCtor, (MapCodec) dataCodec, nodeCtor, color, nextSerialNumber++, opensGui);
		PipeNetworkType previousType = types.put(identifier, type);
		if (previousType != null) {
			throw new IllegalArgumentException("Attempting to register another PipeNetworkType with the same identifier.");
		}
		return type;
	}

	@Override
	public int compareTo(PipeNetworkType o) {
		return Integer.compare(serialNumber, o.serialNumber);
	}
}

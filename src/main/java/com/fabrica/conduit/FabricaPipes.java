package com.fabrica.conduit;

import com.fabrica.FabricaMod;
import com.fabrica.api.energy.CableTier;
import com.fabrica.conduit.api.PipeNetworkData;
import com.fabrica.conduit.api.PipeNetworkType;
import com.fabrica.conduit.electricity.ElectricityNetwork;
import com.fabrica.conduit.electricity.ElectricityNetworkData;
import com.fabrica.conduit.electricity.ElectricityNetworkNode;
import com.fabrica.conduit.fluid.FluidNetwork;
import com.fabrica.conduit.fluid.FluidNetworkData;
import com.fabrica.conduit.fluid.FluidNetworkNode;
import com.fabrica.conduit.impl.PipeBlock;
import com.fabrica.conduit.impl.PipeBlockEntity;
import com.fabrica.conduit.impl.PipeItem;
import com.fabrica.conduit.item.ItemNetwork;
import com.fabrica.conduit.item.ItemNetworkData;
import com.fabrica.conduit.item.ItemNetworkNode;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.HashMap;
import java.util.Map;

/**
 * Registration of the pipe block, its block entity and all pipe types
 * (seven cable tiers, fluid pipes and item pipes).
 */
public final class FabricaPipes {

	private static final ResourceKey<CreativeModeTab> TAB = ResourceKey.create(
		Registries.CREATIVE_MODE_TAB,
		Identifier.withDefaultNamespace("ingredients")
	);

	private static final Map<PipeNetworkType, PipeNetworkData> DEFAULT_DATA = new HashMap<>();
	private static final Map<PipeNetworkType, PipeItem> PIPE_ITEMS = new HashMap<>();
	private static boolean registered = false;

	public static final PipeNetworkType FLUID_PIPE;
	public static final PipeNetworkType ITEM_PIPE;

	public static final PipeNetworkType COPPER_CABLE;
	public static final PipeNetworkType ALUMINUM_CABLE;
	public static final PipeNetworkType GOLD_CABLE;
	public static final PipeNetworkType ALUMINUM_EV_CABLE;
	public static final PipeNetworkType PLATINUM_CABLE;
	public static final PipeNetworkType TUNGSTEN_CABLE;
	public static final PipeNetworkType SUPERCONDUCTOR_CABLE;

	public static final PipeBlock PIPE_BLOCK;

	public static final BlockEntityType<PipeBlockEntity> BLOCK_ENTITY_TYPE;

	static {
		PIPE_BLOCK = registerPipeBlock();
		BLOCK_ENTITY_TYPE = Registry.register(
			BuiltInRegistries.BLOCK_ENTITY_TYPE,
			FabricaMod.id("pipe"),
			new BlockEntityType<>(PipeBlockEntity::new, java.util.Set.of(PIPE_BLOCK))
		);

		FLUID_PIPE = registerFluidPipe();
		ITEM_PIPE = registerItemPipe();

		COPPER_CABLE = registerCable("Copper Cable", "copper_lv_cable", 0xB87333, CableTier.COPPER_LV);
		ALUMINUM_CABLE = registerCable("Aluminum Cable", "aluminum_mv_cable", 0xC0C0C0, CableTier.ALUMINUM_MV);
		GOLD_CABLE = registerCable("Gold Cable", "gold_hv_cable", 0xFFD700, CableTier.GOLD_HV);
		ALUMINUM_EV_CABLE = registerCable("Aluminum Cable (EV)", "aluminum_ev_cable", 0xE0E0E0, CableTier.ALUMINUM_EV);
		PLATINUM_CABLE = registerCable("Platinum Cable", "platinum_iv_cable", 0xE5E4E2, CableTier.PLATINUM_IV);
		TUNGSTEN_CABLE = registerCable("Tungsten Cable", "tungsten_luv_cable", 0x9BA3AF, CableTier.TUNGSTEN_LUV);
		SUPERCONDUCTOR_CABLE = registerCable("Superconductor Cable", "superconductor_cable", 0x00E5FF, CableTier.SUPERCONDUCTOR);
	}

	private static PipeNetworkType registerFluidPipe() {
		PipeNetworkType type = PipeNetworkType.register(
			FabricaMod.id("fluid_pipe"),
			(id, data) -> new FluidNetwork(id, data, (int) FluidConstants.BUCKET),
			FluidNetworkData.CODEC,
			FluidNetworkNode::new,
			PipeColor.REGULAR.color,
			false);
		registerItem(type, "Fluid Pipe", "fluid_pipe", FluidNetworkData.INSTANCE);
		return type;
	}

	private static PipeNetworkType registerItemPipe() {
		PipeNetworkType type = PipeNetworkType.register(
			FabricaMod.id("item_pipe"),
			ItemNetwork::new,
			ItemNetworkData.CODEC,
			ItemNetworkNode::new,
			PipeColor.REGULAR.color,
			false);
		registerItem(type, "Item Pipe", "item_pipe", new ItemNetworkData());
		return type;
	}

	private static PipeNetworkType registerCable(String englishName, String id, int color, CableTier tier) {
		PipeNetworkType type = PipeNetworkType.register(
			FabricaMod.id(id),
			(networkId, data) -> new ElectricityNetwork(networkId, data, tier),
			ElectricityNetworkData.CODEC,
			ElectricityNetworkNode::new,
			color,
			false);
		registerItem(type, englishName, id, new ElectricityNetworkData());
		return type;
	}

	private static void registerItem(PipeNetworkType type, String englishName, String id, PipeNetworkData defaultData) {
		Identifier identifier = FabricaMod.id(id);
		ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, identifier);

		PipeItem item = new PipeItem(new Item.Properties().setId(itemKey), type, defaultData);
		Registry.register(BuiltInRegistries.ITEM, itemKey, item);

		DEFAULT_DATA.put(type, defaultData);
		PIPE_ITEMS.put(type, item);

		CreativeModeTabEvents.modifyOutputEvent(TAB)
			.register(output -> output.accept(item));
	}

	private static PipeBlock registerPipeBlock() {
		Identifier identifier = FabricaMod.id("pipe");
		ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, identifier);

		PipeBlock block = new PipeBlock(
			BlockBehaviour.Properties.of()
				.strength(1.0F)
				.sound(SoundType.METAL)
				.setId(blockKey)
		);
		Registry.register(BuiltInRegistries.BLOCK, blockKey, block);
		return block;
	}

	public static PipeNetworkData getDefaultData(PipeNetworkType type) {
		PipeNetworkData data = DEFAULT_DATA.get(type);
		if (data == null) {
			throw new IllegalArgumentException("No default data registered for pipe type " + type.getIdentifier());
		}
		return data.clone();
	}

	public static PipeItem getPipeItem(PipeNetworkType type) {
		PipeItem item = PIPE_ITEMS.get(type);
		if (item == null) {
			throw new IllegalArgumentException("No pipe item registered for pipe type " + type.getIdentifier());
		}
		return item;
	}

	public static void register() {
		// All registration happens in the static initializer above.
		if (registered) {
			return;
		}
		registered = true;
	}

	private FabricaPipes() {
	}
}

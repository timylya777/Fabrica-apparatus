package com.fabrica.client;

import com.fabrica.client.gui.ElectricFurnaceScreen;
import com.fabrica.client.gui.GeneratorScreen;
import com.fabrica.client.gui.ItemPipeSettingsScreen;
import com.fabrica.client.gui.MeDriveScreen;
import com.fabrica.client.gui.MeGridScreen;
import com.fabrica.client.render.pipe.PipeMeshCache;
import com.fabrica.client.render.pipe.PipeRenderer;
import com.fabrica.client.render.pipe.PipeUnbakedModel;
import com.fabrica.conduit.FabricaPipes;
import com.fabrica.conduit.api.PipeNetworkType;
import com.fabrica.gui.MeGridMenu;
import com.fabrica.gui.ModMenus;
import com.fabrica.me.MePackets;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.CustomUnbakedBlockStateModel;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.InvalidateRenderStateCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

import java.util.Arrays;
import java.util.List;

public class FabricaModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		MenuScreens.register(ModMenus.GENERATOR, GeneratorScreen::new);
		MenuScreens.register(ModMenus.ELECTRIC_FURNACE, ElectricFurnaceScreen::new);
		MenuScreens.register(ModMenus.ME_DRIVE, MeDriveScreen::new);
		MenuScreens.register(ModMenus.ME_GRID, MeGridScreen::new);
		MenuScreens.register(ModMenus.ITEM_PIPE_SETTINGS, ItemPipeSettingsScreen::new);
		CustomUnbakedBlockStateModel.register(PipeUnbakedModel.TYPE_ID, PipeUnbakedModel.CODEC);
		InvalidateRenderStateCallback.EVENT.register(PipeMeshCache::clearAll);
		registerSyncReceiver();
		registerPipeRenderers();
	}

	private static void registerSyncReceiver() {
		ClientPlayNetworking.registerGlobalReceiver(MePackets.MeGridSyncPayload.TYPE, (payload, context) -> {
			context.client().execute(() -> {
				Player player = Minecraft.getInstance().player;
				if (player != null && player.containerMenu instanceof MeGridMenu menu
						&& menu.containerId == payload.containerId()) {
					menu.applySync(payload.entries(), payload.used(), payload.capacity());
				}
			});
		});
	}

	/**
	 * Assign a renderer factory to every registered pipe type, like MI does.
	 * The sprites are indexed by pipe endpoint type id.
	 */
	private static void registerPipeRenderers() {
		FabricaPipes.register();
		PipeRenderer.Factory itemRenderer = makeRenderer(Arrays.asList("item", "item_item", "item_in", "item_in_out", "item_out"), false);
		PipeRenderer.Factory fluidRenderer = makeRenderer(Arrays.asList("fluid", "fluid_item", "fluid_in", "fluid_in_out", "fluid_out"), true);
		PipeRenderer.Factory electricityRenderer = makeRenderer(Arrays.asList("electricity", "electricity_blocks"), false);

		for (PipeNetworkType type : PipeNetworkType.getTypes().values()) {
			String path = type.getIdentifier().getPath();
			if (path.endsWith("item_pipe")) {
				PipeRenderer.register(type, itemRenderer);
			} else if (path.endsWith("fluid_pipe")) {
				PipeRenderer.register(type, fluidRenderer);
			} else {
				PipeRenderer.register(type, electricityRenderer);
			}
		}
	}

	private static PipeRenderer.Factory makeRenderer(List<String> sprites, boolean innerQuads) {
		return modelBaker -> {
			Material[] materials = sprites.stream()
					.map(name -> new Material(Identifier.fromNamespaceAndPath("fabrica_apparatus", "block/pipes/" + name)))
					.toArray(Material[]::new);
			return new PipeMeshCache(modelBaker.materials(), materials, innerQuads);
		};
	}
}

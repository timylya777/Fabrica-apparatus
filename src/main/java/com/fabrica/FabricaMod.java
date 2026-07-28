package com.fabrica;

import com.fabrica.registry.ModItems;
import com.fabrica.registry.ModBlocks;
import com.fabrica.registry.ModBlockItems;
import com.fabrica.registry.ModBlockEntities;
import com.fabrica.registry.ModCreativeTab;
import com.fabrica.registry.ModMenuTypes;
import com.fabrica.energy.APNetworkManager;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.api.ModInitializer;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class FabricaMod implements ModInitializer {
	public static final String MOD_ID = "fabrica_apparatus";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static final Map<ResourceKey<Level>, APNetworkManager> WORLD_MANAGERS = new HashMap<>();

	@Override
	public void onInitialize() {
		ModItems.register();
		ModBlocks.register();
		ModBlockItems.register();
		ModBlockEntities.register();
		ModMenuTypes.register();
		ModRecipes.register();
		ModCreativeTab.register();

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerLevel world : server.getAllLevels()) {
				APNetworkManager manager = WORLD_MANAGERS.get(world.dimension());
				if (manager != null) {
					manager.tick();
				}
			}
		});

		LOGGER.info("We are alive!");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	public static APNetworkManager getManager(Level level) {
		var key = level.dimension();
		var manager = WORLD_MANAGERS.get(key);
		if (manager == null) {
			manager = new APNetworkManager(level);
			WORLD_MANAGERS.put(key, manager);
		}
		return manager;
	}
}

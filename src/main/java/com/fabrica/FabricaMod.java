package com.fabrica;

import com.fabrica.registry.ModItems;
import com.fabrica.registry.ModBlocks;
import com.fabrica.registry.ModBlockItems;
import com.fabrica.registry.ModBlockEntities;
import com.fabrica.registry.ModCreativeTab;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import java.util.HashMap;
import java.util.Map;

import net.fabricmc.api.ModInitializer;

import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FabricaMod implements ModInitializer {
	public static final String MOD_ID = "fabrica_apparatus";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModItems.register();
		ModBlocks.register();
		ModBlockItems.register();
		ModBlockEntities.register();
		ModCreativeTab.register();
		ServerWorldEvents.LOAD.register((server, world) -> {
        WORLD_MANAGERS.put(world.getRegistryKey(), new APNetworkManager(world));
    });

    ServerTickEvents.END_WORLD_TICK.register(world -> {
        APNetworkManager manager = WORLD_MANAGERS.get(world.getRegistryKey());
        if (manager != null) {
            manager.tick(); // Один вызов на мир, а не на каждую машину!
        }
    });
		LOGGER.info("We are alive!");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
	public static APNetworkManager getManager(net.minecraft.world.World world) {
    return WORLD_MANAGERS.get(world.getRegistryKey());
}
}

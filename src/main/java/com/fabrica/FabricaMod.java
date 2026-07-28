package com.fabrica;

import com.fabrica.registry.ModItems;
import com.fabrica.registry.ModBlocks;
import com.fabrica.registry.ModBlockItems;
import com.fabrica.registry.ModBlockEntities;
import com.fabrica.registry.ModCreativeTab;

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
		LOGGER.info("We are alive!");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}

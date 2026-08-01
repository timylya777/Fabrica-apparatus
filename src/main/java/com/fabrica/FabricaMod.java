package com.fabrica;

import com.fabrica.block.ModBlockEntities;
import com.fabrica.conduit.FabricaPipes;
import com.fabrica.conduit.impl.PipeNetworks;
import com.fabrica.energy.FabricaEnergy;
import com.fabrica.gui.ModMenus;
import com.fabrica.item.ModItems;
import com.fabrica.block.ModBlocks;

import net.fabricmc.api.ModInitializer;

import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FabricaMod implements ModInitializer {
	public static final String MOD_ID = "fabrica_apparatus";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		ModItems.register();
		ModBlocks.register();
		ModBlockEntities.register();
		FabricaEnergy.register();
		FabricaPipes.register();
		PipeNetworks.init();
		ModMenus.register();
		LOGGER.info("We are alive!");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}

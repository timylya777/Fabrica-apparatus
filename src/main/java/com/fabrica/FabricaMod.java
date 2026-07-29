package com.fabrica;

import com.fabrica.item.ModItems; // для айтемов
import com.fabrica.block.ModBlocks; // а тут догодайся с трех раз что значит modBlocks
import com.fabrica.block.custom.CableBlocks;

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
		ModItems.register(); //регистрирую мод айтемы
		ModBlocks.register(); // омагад блоки
		CableBlocks.register();
		LOGGER.info("We are alive!");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}

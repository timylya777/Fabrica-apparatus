package com.fabrica.client;

import com.fabrica.client.mixin.MenuScreensInvoker;
import com.fabrica.client.screen.CoalGeneratorScreen;
import com.fabrica.client.screen.ElectricFurnaceScreen;
import com.fabrica.registry.ModMenuTypes;

import net.fabricmc.api.ClientModInitializer;

public class FabricaModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		MenuScreensInvoker.invokeRegister(ModMenuTypes.COAL_GENERATOR, CoalGeneratorScreen::new);
		MenuScreensInvoker.invokeRegister(ModMenuTypes.ELECTRIC_FURNACE, ElectricFurnaceScreen::new);
	}
}
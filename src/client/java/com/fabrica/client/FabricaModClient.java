package com.fabrica.client;

import com.fabrica.cable.FabricaCables;
import com.fabrica.client.gui.ElectricFurnaceScreen;
import com.fabrica.client.gui.GeneratorScreen;
import com.fabrica.gui.ModMenus;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class FabricaModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		MenuScreens.register(ModMenus.GENERATOR, GeneratorScreen::new);
		MenuScreens.register(ModMenus.ELECTRIC_FURNACE, ElectricFurnaceScreen::new);
		ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
			Minecraft.getInstance().getBlockColors().register(
				List.of(new BlockTintSource() {
					@Override
					public int color(BlockState state) {
						return 0xFFB87333;
					}

					@Override
					public int colorInWorld(BlockState state, net.minecraft.client.renderer.block.BlockAndTintGetter level, BlockPos pos) {
						return 0xFFB87333;
					}
				}),
				FabricaCables.CABLE_BLOCK
			);
		});
	}
}

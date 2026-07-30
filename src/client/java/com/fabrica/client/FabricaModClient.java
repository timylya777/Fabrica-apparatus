package com.fabrica.client;

import com.fabrica.cable.CableBlockEntity;
import com.fabrica.cable.CableNodeSlot;
import com.fabrica.cable.FabricaCables;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class FabricaModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
			Minecraft.getInstance().getBlockColors().register(
				List.of(new BlockTintSource() {
					@Override
					public int color(BlockState state) {
						return 0xFFFFFFFF;
					}

					@Override
					public int colorInWorld(BlockState state, net.minecraft.client.renderer.block.BlockAndTintGetter level, BlockPos pos) {
						if (level == null || pos == null) return 0xFFFFFFFF;
						BlockEntity be = level.getBlockEntity(pos);
						if (be instanceof CableBlockEntity cableBE) {
							for (CableNodeSlot slot : cableBE.getNodes()) {
								if (slot != null) return slot.type().getColor() | 0xFF000000;
							}
						}
						return 0xFFFFFFFF;
					}
				}),
				FabricaCables.CABLE_BLOCK
			);
		});
	}
}
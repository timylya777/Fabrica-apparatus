package com.fabrica.mixin;	

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Миксин на MinecraftServer: внедряется в момент загрузки уровня сервера.
@Mixin(MinecraftServer.class)
public class FabricaMixin {
	// HEAD — начало loadLevel; сюда можно добавить серверную инициализацию.
	@Inject(at = @At("HEAD"), method = "loadLevel")
	private void init(CallbackInfo info) {
		// This code is injected into the start of MinecraftServer.loadLevel()V
	}
}
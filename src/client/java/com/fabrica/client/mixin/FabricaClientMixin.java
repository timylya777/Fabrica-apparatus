package com.fabrica.client.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Миксин на класс {@link Minecraft}: встраивает хук в начало метода
 * {@code run()}. Используется как точка ранней клиентской инициализации;
 * сам по себе ничего не делает.
 */
@Mixin(Minecraft.class)
public class FabricaClientMixin {
	/** Пустой хук в начало Minecraft.run(): сюда можно добавлять раннюю инициализацию. */
	@Inject(at = @At("HEAD"), method = "run")
	private void init(CallbackInfo info) {
		// This code is injected into the start of Minecraft.run()V
	}
}
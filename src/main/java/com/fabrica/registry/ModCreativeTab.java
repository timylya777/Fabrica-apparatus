package com.fabrica.registry;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

public class ModCreativeTab {

    public static void register() {
        var ingredientsKey = ResourceKey.create(
                Registries.CREATIVE_MODE_TAB,
                Identifier.withDefaultNamespace("ingredients")
        );
        CreativeModeTabEvents.modifyOutputEvent(ingredientsKey)
                .register(output -> {
                    output.accept(ModItems.IRON_FIGURE);
                    output.accept(ModItems.COPPER_FIGURE);
                    output.accept(ModItems.CLAY_FIGURE);
                    output.accept(ModItems.BRICK_FIGURE);
                    output.accept(ModItems.TERRACOTTA_FIGURE);
                    output.accept(ModBlockItems.MACHINE_CASING);
                });
    }
}

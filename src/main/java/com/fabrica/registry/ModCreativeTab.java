package com.fabrica.registry;

import com.fabrica.FabricaMod;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ModCreativeTab {

    public static final ResourceKey<CreativeModeTab> MACHINES_KEY = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB,
            FabricaMod.id("machines")
    );

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
                });

        Registry.register(
                BuiltInRegistries.CREATIVE_MODE_TAB,
                MACHINES_KEY,
                CreativeModeTab.builder(CreativeModeTab.Row.TOP, 6)
                        .title(Component.translatable("itemGroup.fabrica_apparatus.machines"))
                        .icon(() -> new ItemStack(ModBlockItems.MACHINE_CASING))
                        .displayItems((params, output) -> {
                            output.accept(ModBlockItems.MACHINE_CASING);
                            output.accept(ModBlockItems.COAL_GENERATOR);
                            output.accept(ModBlockItems.ELECTRIC_FURNACE);
                        })
                        .build()
        );
    }
}

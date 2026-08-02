package com.fabrica;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

// Креативная вкладка мода: все предметы и блоки Fabrica Apparatus.
public final class CreativeTabs {

    // Ключ вкладки; на него ссылаются реестры предметов и блоков.
    public static final ResourceKey<CreativeModeTab> MAIN_TAB = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB, FabricaMod.id("main"));

    // Регистрация вкладки: имя из локализации, иконка — корпус машины.
    public static void register() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, MAIN_TAB,
                CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                        .title(Component.translatable("itemGroup.fabrica_apparatus"))
                        .icon(() -> new ItemStack(com.fabrica.block.ModBlocks.MACHINE_CASING))
                        .build());
    }

    private CreativeTabs() {
    }
}

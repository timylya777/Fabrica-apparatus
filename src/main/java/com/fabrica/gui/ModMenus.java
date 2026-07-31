package com.fabrica.gui;

import com.fabrica.FabricaMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

public final class ModMenus {

    public static final MenuType<GeneratorMenu> GENERATOR = Registry.register(
        BuiltInRegistries.MENU,
        FabricaMod.id("generator"),
        new MenuType<>(GeneratorMenu::new, FeatureFlags.VANILLA_SET)
    );

    public static final MenuType<ElectricFurnaceMenu> ELECTRIC_FURNACE = Registry.register(
        BuiltInRegistries.MENU,
        FabricaMod.id("electric_furnace"),
        new MenuType<>(ElectricFurnaceMenu::new, FeatureFlags.VANILLA_SET)
    );

    public static void register() {
    }

    private ModMenus() {
    }
}

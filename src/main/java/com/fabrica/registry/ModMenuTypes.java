package com.fabrica.registry;

import com.fabrica.FabricaMod;
import com.fabrica.menu.CoalGeneratorMenu;
import com.fabrica.menu.ElectricFurnaceMenu;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.MenuType;

public class ModMenuTypes {

    public static final MenuType<CoalGeneratorMenu> COAL_GENERATOR =
            Registry.register(
                    BuiltInRegistries.MENU,
                    FabricaMod.id("coal_generator"),
                    new MenuType<>(CoalGeneratorMenu::new, FeatureFlagSet.of())
            );

    public static final MenuType<ElectricFurnaceMenu> ELECTRIC_FURNACE =
            Registry.register(
                    BuiltInRegistries.MENU,
                    FabricaMod.id("electric_furnace"),
                    new MenuType<>(ElectricFurnaceMenu::new, FeatureFlagSet.of())
            );

    public static void register() {
    }
}

package com.fabrica.registry;

import com.fabrica.FabricaMod;
import com.fabrica.menu.CoalGeneratorMenu;
import com.fabrica.menu.ElectricFurnaceMenu;
import com.fabrica.menu.ProcessingMachineMenu;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;

public class ModMenuTypes {
    public static final MenuType<ProcessingMachineMenu> CRUSHER_MENU = createCrusherType();

    public static final MenuType<CoalGeneratorMenu> COAL_GENERATOR = Registry.register(
            BuiltInRegistries.MENU, FabricaMod.id("coal_generator"),
            new MenuType<>(CoalGeneratorMenu::new, FeatureFlags.VANILLA_SET)
    );

    public static final MenuType<ElectricFurnaceMenu> ELECTRIC_FURNACE = Registry.register(
            BuiltInRegistries.MENU, FabricaMod.id("electric_furnace"),
            new MenuType<>(ElectricFurnaceMenu::new, FeatureFlags.VANILLA_SET)
    );

    private static MenuType<ProcessingMachineMenu> createCrusherType() {
        MenuType<ProcessingMachineMenu> type = new MenuType<>(
            (containerId, playerInventory) -> createProcessingMenu(containerId, playerInventory),
            FeatureFlags.VANILLA_SET
        );
        return Registry.register(
            BuiltInRegistries.MENU, FabricaMod.id("crusher_menu"), type
        );
    }

    private static ProcessingMachineMenu createProcessingMenu(int containerId, Inventory playerInventory) {
        return new ProcessingMachineMenu(
            CRUSHER_MENU, containerId, playerInventory,
            new SimpleContainer(3),
            new SimpleContainerData(3),
            1, 2
        );
    }

    public static void register() {}
}

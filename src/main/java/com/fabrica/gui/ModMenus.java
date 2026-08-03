package com.fabrica.gui;

import com.fabrica.FabricaMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

// Реестр типов меню (контейнеров) мода. MenuType создаётся из фабрики
// конструктора меню, которая вызывается при открытии на клиенте.
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

    public static final MenuType<MaceratorMenu> MACERATOR = Registry.register(
        BuiltInRegistries.MENU,
        FabricaMod.id("macerator"),
        new MenuType<>(MaceratorMenu::new, FeatureFlags.VANILLA_SET)
    );

    public static final MenuType<MeDriveMenu> ME_DRIVE = Registry.register(
        BuiltInRegistries.MENU,
        FabricaMod.id("me_drive"),
        new MenuType<>(MeDriveMenu::new, FeatureFlags.VANILLA_SET)
    );

    public static final MenuType<MeGridMenu> ME_GRID = Registry.register(
        BuiltInRegistries.MENU,
        FabricaMod.id("me_grid"),
        new MenuType<>(MeGridMenu::new, FeatureFlags.VANILLA_SET)
    );

    public static final MenuType<AnvilMenu> ANVIL = Registry.register(
        BuiltInRegistries.MENU,
        FabricaMod.id("anvil"),
        new MenuType<>(AnvilMenu::new, FeatureFlags.VANILLA_SET)
    );

    public static final MenuType<ItemPipeSettingsMenu> ITEM_PIPE_SETTINGS = Registry.register(
        BuiltInRegistries.MENU,
        FabricaMod.id("item_pipe_settings"),
        new MenuType<>(ItemPipeSettingsMenu::new, FeatureFlags.VANILLA_SET)
    );

    // Регистрация выполнена статическими полями; метод нужен лишь как точка вызова при старте мода.
    public static void register() {
    }

    private ModMenus() {
    }
}

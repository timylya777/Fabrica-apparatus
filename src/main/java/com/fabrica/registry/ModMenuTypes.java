
package com.fabrica.registry;

import com.fabrica.FabricaMod;
import com.fabrica.menu.ProcessingMachineMenu;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;

public class ModMenuTypes {
    public static final MenuType<ProcessingMachineMenu> CRUSHER_MENU = Registry.register(
            BuiltInRegistries.MENU, FabricaMod.id("crusher_menu"),
            new MenuType<>((containerId, playerInventory) -> 
                new ProcessingMachineMenu(
                        CRUSHER_MENU, containerId, playerInventory,
                        new SimpleContainer(3), // 1 input + 2 output
                        new SimpleContainerData(3),
                        1, 2
                )
            )
    );

    public static void register() {}
}
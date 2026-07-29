package com.fabrica.item;

import com.fabrica.FabricaMod;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

public class ModItems {

    // ITEM REGISTERING IN ONE METHOD
    private static Item register(
        String id,
        Item.Properties properties,
        ResourceKey<CreativeModeTab> creativeTab
    ) {
        Identifier identifier = Identifier.fromNamespaceAndPath(FabricaMod.MOD_ID, id);
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, identifier);

        Item item = new Item(properties.setId(itemKey));
        Registry.register(BuiltInRegistries.ITEM, itemKey, item);

        CreativeModeTabEvents.modifyOutputEvent(creativeTab)
                .register(output -> output.accept(item));

        return item;
    }

    public static final Item IRON_FIGURE = register(
        "iron_figure",
        new Item.Properties(),
        ResourceKey.create(
                Registries.CREATIVE_MODE_TAB,
                Identifier.withDefaultNamespace("ingredients")
        )
    );

    /**
     * Метод инициализации. 
     * Вызовите ModItems.register() в вашем главном классе (ModInitializer).
     */
    public static void register() {
    }
}
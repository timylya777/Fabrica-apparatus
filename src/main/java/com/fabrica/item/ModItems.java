package com.fabrica.item;

import com.fabrica.CreativeTabs;
import com.fabrica.FabricaMod;
import com.fabrica.item.material.Material;
import com.fabrica.me.MeStorageDiskItem;
import com.fabrica.me.MeStorageDiskTier;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

public class ModItems {

    // ITEM REGISTERING IN ONE METHOD
    private static <T extends Item> T register(
        String id,
        Function<Item.Properties, T> factory,
        ResourceKey<CreativeModeTab> creativeTab
    ) {
        Identifier identifier = Identifier.fromNamespaceAndPath(FabricaMod.MOD_ID, id);
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, identifier);

        T item = factory.apply(new Item.Properties().setId(itemKey));
        Registry.register(BuiltInRegistries.ITEM, itemKey, item);

        CreativeModeTabEvents.modifyOutputEvent(creativeTab)
                .register(output -> output.accept(item));

        return item;
    }

    public static final Item IRON_FIGURE = register(
        "iron_figure",
        Item::new,
        CreativeTabs.MAIN_TAB
    );

    public static final Item DEBUG_ITEM = register(
        "debug_item",
        DebugItem::new,
        CreativeTabs.MAIN_TAB
    );

    public static final MeStorageDiskItem ME_STORAGE_DISK_BASIC = register(
        "me_storage_disk_basic",
        properties -> new MeStorageDiskItem(properties, MeStorageDiskTier.BASIC),
        CreativeTabs.MAIN_TAB
    );

    public static final MeStorageDiskItem ME_STORAGE_DISK_ADVANCED = register(
        "me_storage_disk_advanced",
        properties -> new MeStorageDiskItem(properties, MeStorageDiskTier.ADVANCED),
        CreativeTabs.MAIN_TAB
    );

    public static final MeStorageDiskItem ME_STORAGE_DISK_ELITE = register(
        "me_storage_disk_elite",
        properties -> new MeStorageDiskItem(properties, MeStorageDiskTier.ELITE),
        CreativeTabs.MAIN_TAB
    );

    public static final KeyItem KEY = register(
        "key",
        KeyItem::new,
        CreativeTabs.MAIN_TAB
    );

    // Материалы: слитки, пыли и пластины для каждого материала мода.
    public static final Map<Material, Item> INGOTS = new EnumMap<>(Material.class);
    public static final Map<Material, Item> DUSTS = new EnumMap<>(Material.class);
    public static final Map<Material, Item> PLATES = new EnumMap<>(Material.class);

    static {
        for (Material material : Material.values()) {
            INGOTS.put(material, register(material.id() + "_ingot", Item::new, CreativeTabs.MAIN_TAB));
            DUSTS.put(material, register(material.id() + "_dust", Item::new, CreativeTabs.MAIN_TAB));
            PLATES.put(material, register(material.id() + "_plate", Item::new, CreativeTabs.MAIN_TAB));
        }
    }

    /**
     * Метод инициализации. 
     * Вызовите ModItems.register() в вашем главном классе (ModInitializer).
     */
    public static void register() {
    }
}

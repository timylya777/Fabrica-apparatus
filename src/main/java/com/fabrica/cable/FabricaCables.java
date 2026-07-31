package com.fabrica.cable;

import com.fabrica.FabricaMod;
import com.fabrica.api.energy.CableTier;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class FabricaCables {

    private static final Map<String, CableNodeFactory> FACTORIES = new HashMap<>();

    public static final CableType COPPER_CABLE_TYPE = registerCableType(
        "copper_cable", "Copper Cable", 0xFFB87333, new EnergyCableFactory("copper_cable", CableTier.COPPER_LV)
    );

    public static final CableType TIN_CABLE_TYPE = registerCableType(
        "tin_cable", "Tin Cable", 0xFFD4D4D4, new EnergyCableFactory("tin_cable", CableTier.ALUMINUM_MV)
    );

    public static final CableType GOLD_CABLE_TYPE = registerCableType(
        "gold_cable", "Gold Cable", 0xFFFFD700, new EnergyCableFactory("gold_cable", CableTier.GOLD_HV)
    );

    public static final CableBlock CABLE_BLOCK = registerCableBlock();

    public static final BlockEntityType<CableBlockEntity> CABLE_BE = Registry.register(
        BuiltInRegistries.BLOCK_ENTITY_TYPE,
        FabricaMod.id("cable"),
        new BlockEntityType<>(CableBlockEntity::new, Set.of(CABLE_BLOCK))
    );

    public static final CableTypeItem COPPER_CABLE_ITEM;
    public static final CableTypeItem TIN_CABLE_ITEM;
    public static final CableTypeItem GOLD_CABLE_ITEM;
    public static final WrenchItem WRENCH;

    private static final ResourceKey<CreativeModeTab> TAB = ResourceKey.create(
        Registries.CREATIVE_MODE_TAB,
        Identifier.withDefaultNamespace("ingredients")
    );

    static {
        COPPER_CABLE_ITEM = registerCableItem("copper_cable", COPPER_CABLE_TYPE);
        TIN_CABLE_ITEM = registerCableItem("tin_cable", TIN_CABLE_TYPE);
        GOLD_CABLE_ITEM = registerCableItem("gold_cable", GOLD_CABLE_TYPE);
        WRENCH = registerWrench();
    }

    private static CableType registerCableType(String name, String englishName, int color, CableNodeFactory factory) {
        Identifier id = FabricaMod.id(name);
        CableType type = new CableType(id, englishName, color, factory);
        FACTORIES.put(id.toString(), factory);
        return type;
    }

    public static @Nullable CableNodeFactory getFactory(String typeId) {
        return FACTORIES.get(typeId);
    }

    private static CableTypeItem registerCableItem(String name, CableType cableType) {
        Identifier id = FabricaMod.id(name);
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id);
        CableTypeItem item = new CableTypeItem(
            CABLE_BLOCK,
            new Item.Properties().setId(itemKey),
            cableType
        );
        Registry.register(BuiltInRegistries.ITEM, itemKey, item);
        CreativeModeTabEvents.modifyOutputEvent(TAB)
            .register(output -> output.accept(item));
        return item;
    }

    private static WrenchItem registerWrench() {
        Identifier id = FabricaMod.id("wrench");
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id);
        WrenchItem item = new WrenchItem(new Item.Properties().setId(itemKey).stacksTo(1));
        Registry.register(BuiltInRegistries.ITEM, itemKey, item);
        CreativeModeTabEvents.modifyOutputEvent(TAB)
            .register(output -> output.accept(item));
        return item;
    }

    private static CableBlock registerCableBlock() {
        Identifier id = FabricaMod.id("cable");
        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, id);

        CableBlock block = new CableBlock(
            BlockBehaviour.Properties.of()
                .strength(1.0F)
                .sound(SoundType.METAL)
                .noOcclusion()
                .setId(blockKey)
        );
        Registry.register(BuiltInRegistries.BLOCK, blockKey, block);

        return block;
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerLevel level : server.getAllLevels()) {
                CableNetworks.get(level).tickAll(level);
            }
        });
    }

    private FabricaCables() {
    }
}

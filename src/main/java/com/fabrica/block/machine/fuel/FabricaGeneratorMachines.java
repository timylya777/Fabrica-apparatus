package com.fabrica.block.machine.fuel;

// ==================== Регистрация готового генератора ====================

import com.fabrica.FabricaMod;
import com.fabrica.api.energy.EnergyTier;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.Set;

public final class FabricaGeneratorMachines {

    // ===== 1. РЕГИСТРИРУЕМ БЛОК =====

    public static final CoalGeneratorBlock COAL_GENERATOR = registerCoalGenerator();

    // Приватный метод: создаёт экземпляр CoalGeneratorBlock, регистрирует его и BlockItem
    private static CoalGeneratorBlock registerCoalGenerator() {
        // Создаём Identifier "fabrica_apparatus:coal_generator"
        Identifier id = FabricaMod.id("coal_generator");

        // ResourceKey нужен для setId() в Properties (обязательно с 1.21.5)
        ResourceKey<net.minecraft.world.level.block.Block> blockKey =
            ResourceKey.create(Registries.BLOCK, id);
        ResourceKey<Item> itemKey =
            ResourceKey.create(Registries.ITEM, id);

        // Создаём блок с параметрами:
        //   - Properties.of() вместо Fabric's Settings
        //   - strength(3.5F) = твёрдость как у железа
        //   - sound(SoundType.METAL) = металлический звук
        //   - requiresCorrectToolForDrops() = нужна кирка
        //   - setId(blockKey) = ОБЯЗАТЕЛЬНО с 1.21.5
        CoalGeneratorBlock block = new CoalGeneratorBlock(
            BlockBehaviour.Properties.of()
                .strength(3.5F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .setId(blockKey),
            4000,           // capacity: буфер 4000 EU
            EnergyTier.LV,  // tier: Low Voltage
            30              // productionRate: 30 EU/t
        );

        // Регистрируем блок в BuiltInRegistries.BLOCK
        Registry.register(BuiltInRegistries.BLOCK, blockKey, block);

        // Создаём BlockItem (предмет в инвентаре)
        Item item = new BlockItem(block, new Item.Properties().setId(itemKey));
        Registry.register(BuiltInRegistries.ITEM, itemKey, item);

        // Добавляем в креативную вкладку Ingredients
        CreativeModeTabEvents.modifyOutputEvent(
            ResourceKey.create(Registries.CREATIVE_MODE_TAB,
                Identifier.withDefaultNamespace("ingredients"))
        ).register(output -> output.accept(item));

        return block;
    }

    // ===== 2. РЕГИСТРИРУЕМ BlockEntityType =====

    public static final BlockEntityType<CoalGeneratorBlockEntity> COAL_GENERATOR_BE = Registry.register(
        BuiltInRegistries.BLOCK_ENTITY_TYPE,
        FabricaMod.id("coal_generator"),

        // В 1.21.5 BlockEntityType.Builder удалён.
        // Используем прямой конструктор:
        //   new BlockEntityType<>(supplier, Set<Block>)
        //
        // Supplier = CoalGeneratorBlockEntity::new
        //   -> вызывает CoalGeneratorBlockEntity(BlockPos, BlockState)
        //   -> второй конструктор (для загрузки)
        //      CoalGeneratorBlockEntity(BlockPos pos, BlockState state)
        //
        // Set.of(COAL_GENERATOR) = список блоков к которым привязан BE
        new BlockEntityType<>(CoalGeneratorBlockEntity::new, Set.of(COAL_GENERATOR))
    );

    // ===== 3. ВЫЗОВ ИЗ ModInitializer =====

    public static void register() {
        // Пустой метод: static иниты уже выполнились при загрузке класса
        // Нужен только для явного вызова из FabricaMod.onInitialize()
    }

    private FabricaGeneratorMachines() {}
}

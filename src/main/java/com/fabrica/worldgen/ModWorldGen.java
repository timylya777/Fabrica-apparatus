package com.fabrica.worldgen;

import com.fabrica.FabricaMod;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.levelgen.GenerationStep;

// Регистрация фичи жил и добавление её в мир: только в биомы верхнего мира.
public final class ModWorldGen {

    public static void register() {
        // Кастомная фича рудных жил; конфигурация задаётся в датапаке
        // (worldgen/configured_feature/ore_vein.json).
        Registry.register(BuiltInRegistries.FEATURE, FabricaMod.id("ore_vein"),
                new VeinFeature(VeinFeatureConfig.CODEC));

        // Привязываем размещённую фичу к биомам верхнего мира.
        BiomeModifications.addFeature(
                context -> context.hasTag(BiomeTags.IS_OVERWORLD),
                GenerationStep.Decoration.UNDERGROUND_ORES,
                ResourceKey.create(Registries.PLACED_FEATURE, FabricaMod.id("ore_vein"))
        );
    }

    private ModWorldGen() {
    }
}

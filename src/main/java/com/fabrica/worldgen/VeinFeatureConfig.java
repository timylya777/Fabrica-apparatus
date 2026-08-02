package com.fabrica.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

import java.util.List;

// Конфигурация фичи рудных жил: список возможных типов жил для размерности.
public record VeinFeatureConfig(List<VeinConfig> veins) implements FeatureConfiguration {

    public static final Codec<VeinFeatureConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            VeinConfig.CODEC.listOf().fieldOf("veins").forGetter(VeinFeatureConfig::veins)
    ).apply(instance, VeinFeatureConfig::new));
}

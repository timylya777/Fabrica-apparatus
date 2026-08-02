package com.fabrica.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

// Описание одного типа GT-жилы: состав (primary/secondary/between/around),
// диапазон высот, вес при выборе типа жилы и радиус в блоках.
// Блоки задаются строками вида "copper_ore"; для глубоких слоёв автоматически
// берётся "deepslate_<id>". Пустая строка означает отсутствие руды в слоте.
public record VeinConfig(
        String id,
        String primary,
        String secondary,
        String between,
        String around,
        int minY,
        int maxY,
        int weight,
        int radius) {

    public static final Codec<VeinConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("id").forGetter(VeinConfig::id),
            Codec.STRING.fieldOf("primary").forGetter(VeinConfig::primary),
            Codec.STRING.optionalFieldOf("secondary", "").forGetter(VeinConfig::secondary),
            Codec.STRING.optionalFieldOf("between", "").forGetter(VeinConfig::between),
            Codec.STRING.optionalFieldOf("around", "").forGetter(VeinConfig::around),
            Codec.INT.fieldOf("min_y").forGetter(VeinConfig::minY),
            Codec.INT.fieldOf("max_y").forGetter(VeinConfig::maxY),
            Codec.INT.fieldOf("weight").forGetter(VeinConfig::weight),
            Codec.INT.fieldOf("radius").forGetter(VeinConfig::radius)
    ).apply(instance, VeinConfig::new));
}

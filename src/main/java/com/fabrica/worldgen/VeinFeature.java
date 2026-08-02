package com.fabrica.worldgen;

import com.fabrica.FabricaMod;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

import java.util.List;

// GT-подобная генерация рудных жил: на выбранной позиции ставится эллипс
// радиусом до ~16 блоков с плотностью, спадающей к краям. Центр жилы —
// primary руда, дальше secondary/between, на окраинах around. Блок, который
// заменяется, определяет вариант руды: deepslate — в глубине, каменный —
// выше. Жилы имеют вертикальную толщину, наибольшую в центре.
public class VeinFeature extends Feature<VeinFeatureConfig> {

    // Заменяемые блоки: стандартные каменные и глубокосланцевые.
    private static final TagKey<Block> STONE_REPLACEABLES = TagKey.create(Registries.BLOCK,
            Identifier.fromNamespaceAndPath("minecraft", "stone_ore_replaceables"));
    private static final TagKey<Block> DEEPSLATE_REPLACEABLES = TagKey.create(Registries.BLOCK,
            Identifier.fromNamespaceAndPath("minecraft", "deepslate_ore_replaceables"));

    public VeinFeature(Codec<VeinFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<VeinFeatureConfig> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();

        VeinConfig vein = pickVein(context.config().veins(), random);
        if (vein == null || resolve(vein.primary()) == null) return false;

        // Центр жилы по высоте выбирается в диапазоне типа жилы.
        int centerY = vein.minY() + random.nextInt(vein.maxY() - vein.minY() + 1);
        int radius = vein.radius();
        boolean placed = false;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                double dist = Math.sqrt(dx * dx + dz * dz) / radius;
                if (dist > 1.0) continue;

                // Вертикальная толщина: толще в центре, тоньше у краёв.
                int thickness = (int) Math.round(3 + (1.0 - dist) * 9);
                int top = centerY + thickness / 2;
                int bottom = centerY - thickness / 2;

                // Края жилы прореживаются случайностью — форма становится органичной.
                if (random.nextDouble() > 0.45 + 0.5 * (1.0 - dist)) continue;

                for (int y = bottom; y <= top; y++) {
                    BlockPos pos = new BlockPos(origin.getX() + dx, y, origin.getZ() + dz);
                    BlockState target = level.getBlockState(pos);
                    boolean deepslate = target.is(DEEPSLATE_REPLACEABLES);
                    if (!deepslate && !target.is(STONE_REPLACEABLES)) continue;

                    // Выбор руды по кольцу жилы и разрешение блока по id.
                    String oreId = pickOreId(vein, dist, random);
                    if (oreId.isEmpty()) continue;
                    Block ore = resolve(deepslate ? "deepslate_" + oreId : oreId);
                    if (ore == null) continue;

                    level.setBlock(pos, ore.defaultBlockState(), 2);
                    placed = true;
                }
            }
        }
        return placed;
    }

    // Взвешенный выбор типа жилы: чем больше weight, тем чаще жила встречается.
    private static VeinConfig pickVein(List<VeinConfig> veins, RandomSource random) {
        int total = veins.stream().mapToInt(VeinConfig::weight).sum();
        if (total <= 0) return null;
        int roll = random.nextInt(total);
        for (VeinConfig vein : veins) {
            roll -= vein.weight();
            if (roll < 0) return vein;
        }
        return veins.getFirst();
    }

    // Руда по кольцу: центр — primary, среднее кольцо — secondary/between,
    // окраина — around (при её отсутствии кольцо сужается к центру).
    private static String pickOreId(VeinConfig vein, double dist, RandomSource random) {
        if (dist < 0.35) return vein.primary();
        if (dist < 0.7) {
            if (!vein.between().isEmpty() && random.nextInt(3) == 0) return vein.between();
            if (!vein.secondary().isEmpty()) return vein.secondary();
            if (!vein.between().isEmpty()) return vein.between();
            return vein.primary();
        }
        if (!vein.around().isEmpty()) return vein.around();
        if (!vein.secondary().isEmpty() && random.nextInt(2) == 0) return vein.secondary();
        return vein.primary();
    }

    // Разрешение id руды в блок; неизвестный id даёт null и пропускается.
    private static Block resolve(String id) {
        return BuiltInRegistries.BLOCK.getValue(Identifier.fromNamespaceAndPath(FabricaMod.MOD_ID, id));
    }
}

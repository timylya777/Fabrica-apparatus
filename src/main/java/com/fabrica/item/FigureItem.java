package com.fabrica.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

// «Человечек» — фигурка-рабочий для наковальни. Имеет прочность (durability):
// при каждой операции наковальни прочность уменьшается на damage рецепта,
// а когда заканчивается — фигурка ломается. Разные фигурки дают разную
// скорость ковки (speedMultiplier): чем лучше материал, тем быстрее работает.
public class FigureItem extends Item {

    private final int speedMultiplier;

    public FigureItem(Item.Properties properties, int speedMultiplier, int durability) {
        // durability() включает полосу прочности на иконке и механику damage.
        super(properties.stacksTo(1).durability(durability));
        this.speedMultiplier = speedMultiplier;
    }

    /**
     * Множитель скорости наковальни: итоговое время операции = time / multiplier.
     * Глиняный 1x, кирпичный 2x, медный 3x, терракотовый 4x, железный 5x.
     */
    public int getSpeedMultiplier() {
        return speedMultiplier;
    }

    // Подпись под названием предмета: баф скорости.
    // Строку прочности НЕ добавляем — ваниль сама показывает "Прочность: X / Y",
    // когда предмет повреждён, и это единственная строка прочности.
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay tooltipDisplay,
                                Consumer<Component> consumer, TooltipFlag flag) {
        consumer.accept(Component.translatable("item.fabrica_apparatus.figure.tooltip.speed",
                speedMultiplier));
    }

}
package com.fabrica.me;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Предмет "ME-диск" — носитель данных сети.
 * Вся информация о содержимом хранится в NBT (CUSTOM_DATA) самого стака:
 * список записей {id: идентификатор предмета, count: количество}.
 * Класс отвечает за чтение/запись этих NBT-данных и показ подсказки в инвентаре.
 */
public class MeStorageDiskItem extends Item {
    /** Ключ NBT-списка с записями предметов на диске. */
    public static final String KEY_ITEMS = "Items";

    /** Тип (тир) данного диска, задаётся при создании предмета. */
    private final MeStorageDiskTier tier;

    public MeStorageDiskItem(Item.Properties properties, MeStorageDiskTier tier) {
        super(properties);
        this.tier = tier;
    }

    /** Тир диска (от него зависит ёмкость). */
    public MeStorageDiskTier getTier() {
        return tier;
    }

    /** Дополнительная строка подсказки: ёмкость и занятый объём диска. */
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay,
                                Consumer<Component> consumer, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltipDisplay, consumer, flag);
        consumer.accept(Component.translatable("tooltip.fabrica_apparatus.me_disk_capacity", tier.getCapacity()));
        consumer.accept(Component.translatable("tooltip.fabrica_apparatus.me_disk_used", getUsed(stack)));
    }

    /** Прочитать записи из NBT стака диска в список MeItemStack. */
    public static List<MeItemStack> readEntries(ItemStack diskStack) {
        List<MeItemStack> entries = new ArrayList<>();
        for (Tag raw : getItemsTag(diskStack)) {
            if (raw instanceof CompoundTag entry) {
                String id = entry.getStringOr("id", "");
                if (id.isEmpty()) {
                    continue;
                }
                net.minecraft.resources.Identifier identifier = net.minecraft.resources.Identifier.tryParse(id);
                if (identifier == null) {
                    continue;
                }
                Item item = net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(identifier);
                long count = entry.getLongOr("count", 0);
                if (item != null && count > 0) {
                    entries.add(new MeItemStack(item, count));
                }
            }
        }
        return entries;
    }

    /** Записать список записей (ListTag) в NBT стака диска. */
    public static void writeEntries(ItemStack diskStack, ListTag list) {
        CompoundTag tag = new CompoundTag();
        tag.put(KEY_ITEMS, list);
        CustomData.set(DataComponents.CUSTOM_DATA, diskStack, tag);
    }

    /** Сумма всех count — сколько штук всего занято на диске. */
    public static long getUsed(ItemStack diskStack) {
        long used = 0;
        for (Tag raw : getItemsTag(diskStack)) {
            if (raw instanceof CompoundTag entry) {
                used += entry.getLongOr("count", 0);
            }
        }
        return used;
    }

    /** Достать NBT-список "Items" из стака диска (пустой, если данных нет). */
    public static ListTag getItemsTag(ItemStack diskStack) {
        CustomData data = diskStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        if (data.isEmpty()) {
            return new ListTag();
        }
        Tag items = data.copyTag().get(KEY_ITEMS);
        return items instanceof ListTag list ? list : new ListTag();
    }
}

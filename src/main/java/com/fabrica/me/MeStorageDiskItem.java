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

public class MeStorageDiskItem extends Item {
    public static final String KEY_ITEMS = "Items";

    private final MeStorageDiskTier tier;

    public MeStorageDiskItem(Item.Properties properties, MeStorageDiskTier tier) {
        super(properties);
        this.tier = tier;
    }

    public MeStorageDiskTier getTier() {
        return tier;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay,
                                Consumer<Component> consumer, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltipDisplay, consumer, flag);
        consumer.accept(Component.translatable("tooltip.fabrica_apparatus.me_disk_capacity", tier.getCapacity()));
        consumer.accept(Component.translatable("tooltip.fabrica_apparatus.me_disk_used", getUsed(stack)));
    }

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

    public static void writeEntries(ItemStack diskStack, ListTag list) {
        CompoundTag tag = new CompoundTag();
        tag.put(KEY_ITEMS, list);
        CustomData.set(DataComponents.CUSTOM_DATA, diskStack, tag);
    }

    public static long getUsed(ItemStack diskStack) {
        long used = 0;
        for (Tag raw : getItemsTag(diskStack)) {
            if (raw instanceof CompoundTag entry) {
                used += entry.getLongOr("count", 0);
            }
        }
        return used;
    }

    public static ListTag getItemsTag(ItemStack diskStack) {
        CustomData data = diskStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        if (data.isEmpty()) {
            return new ListTag();
        }
        Tag items = data.copyTag().get(KEY_ITEMS);
        return items instanceof ListTag list ? list : new ListTag();
    }
}

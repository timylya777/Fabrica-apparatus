package com.fabrica.me;

import com.fabrica.FabricaMod;
import com.fabrica.gui.MeGridMenu;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Сетевые пакеты ME-сетки: приём с клиента запросов take/insert,
 * рассылка синхронизации (sync) состояния сети клиенту.
 * Плюс утилиты сериализации записей сети в NBT и фильтрации по запросу поиска.
 */
public final class MePackets {

    /**
     * Пакет от клиента: взять предметы из ME-сети.
     * containerId — идентификатор открытого меню, query — поисковый запрос,
     * index — индекс записи в отфильтрованном списке, count — количество.
     */
    public record MeGridTakePayload(int containerId, String query, int index, int count)
            implements CustomPacketPayload {
        public static final Type<MeGridTakePayload> TYPE = new Type<>(FabricaMod.id("me_grid_take"));
        public static final StreamCodec<RegistryFriendlyByteBuf, MeGridTakePayload> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, MeGridTakePayload::containerId,
                ByteBufCodecs.STRING_UTF8, MeGridTakePayload::query,
                ByteBufCodecs.VAR_INT, MeGridTakePayload::index,
                ByteBufCodecs.VAR_INT, MeGridTakePayload::count,
                MeGridTakePayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /**
     * Пакет от клиента: вставить предметы, которые игрок держит на курсоре,
     * в ME-сеть (count — количество из стака на курсоре).
     */
    public record MeGridInsertPayload(int containerId, int count) implements CustomPacketPayload {
        public static final Type<MeGridInsertPayload> TYPE = new Type<>(FabricaMod.id("me_grid_insert"));
        public static final StreamCodec<RegistryFriendlyByteBuf, MeGridInsertPayload> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, MeGridInsertPayload::containerId,
                ByteBufCodecs.VAR_INT, MeGridInsertPayload::count,
                MeGridInsertPayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /**
     * Пакет с сервера: синхронизация состояния сети клиенту
     * (занято, ёмкость и все записи сети в NBT).
     */
    public record MeGridSyncPayload(int containerId, long used, long capacity, CompoundTag entries)
            implements CustomPacketPayload {
        public static final Type<MeGridSyncPayload> TYPE = new Type<>(FabricaMod.id("me_grid_sync"));
        public static final StreamCodec<RegistryFriendlyByteBuf, MeGridSyncPayload> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, MeGridSyncPayload::containerId,
                ByteBufCodecs.VAR_LONG, MeGridSyncPayload::used,
                ByteBufCodecs.VAR_LONG, MeGridSyncPayload::capacity,
                ByteBufCodecs.COMPOUND_TAG, MeGridSyncPayload::entries,
                MeGridSyncPayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /** Зарегистрировать типы пакетов и обработчики запросов на сервере. */
    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(MeGridTakePayload.TYPE, MeGridTakePayload.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(MeGridInsertPayload.TYPE, MeGridInsertPayload.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(MeGridSyncPayload.TYPE, MeGridSyncPayload.STREAM_CODEC);

        // Обработчик take: выполняем на серверном потоке, валидируем открытое меню.
        ServerPlayNetworking.registerGlobalReceiver(MeGridTakePayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            player.level().getServer().execute(() -> {
                if (player.containerMenu instanceof MeGridMenu menu
                        && menu.containerId == payload.containerId()) {
                    menu.takeFromGrid(player, payload.query(), payload.index(), payload.count());
                    sendSync(player, menu);
                }
            });
        });

        // Обработчик insert: выполняем на серверном потоке, валидируем открытое меню.
        ServerPlayNetworking.registerGlobalReceiver(MeGridInsertPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            player.level().getServer().execute(() -> {
                if (player.containerMenu instanceof MeGridMenu menu
                        && menu.containerId == payload.containerId()) {
                    menu.insertCarried(player, payload.count());
                    sendSync(player, menu);
                }
            });
        });
    }

    /** Отправить клиенту актуальное состояние сети его меню (used/capacity/entries). */
    public static void sendSync(ServerPlayer player, MeGridMenu menu) {
        if (menu.getBlockEntity() == null || !ServerPlayNetworking.canSend(player, MeGridSyncPayload.TYPE)) {
            return;
        }
        MeStorage storage = menu.getBlockEntity().getMeStorage();
        player.level().getServer().execute(() -> ServerPlayNetworking.send(player, new MeGridSyncPayload(
                menu.containerId,
                storage.getItemCount(),
                storage.getCapacity(),
                entriesToTag(storage.getEntries())
        )));
    }

    /** Сериализовать записи сети в NBT-тег (список {id, count}). */
    public static CompoundTag entriesToTag(List<MeItemStack> entries) {
        CompoundTag root = new CompoundTag();
        ListTag list = new ListTag();
        for (MeItemStack entry : entries) {
            CompoundTag tag = new CompoundTag();
            tag.put("id", StringTag.valueOf(BuiltInRegistries.ITEM.getKey(entry.item()).toString()));
            tag.put("count", LongTag.valueOf(entry.count()));
            list.add(list.size(), tag);
        }
        root.put("Items", list);
        return root;
    }

    /** Десериализовать NBT-тег обратно в список записей сети. */
    public static List<MeItemStack> entriesFromTag(CompoundTag root) {
        if (root == null) {
            return List.of();
        }
        Tag items = root.get("Items");
        if (!(items instanceof ListTag list)) {
            return List.of();
        }
        List<MeItemStack> entries = new ArrayList<>();
        for (Tag raw : list) {
            if (!(raw instanceof CompoundTag tag)) {
                continue;
            }
            String id = tag.getStringOr("id", "");
            if (id.isEmpty()) {
                continue;
            }
            Identifier identifier = Identifier.tryParse(id);
            if (identifier == null) {
                continue;
            }
            Item item = BuiltInRegistries.ITEM.getValue(identifier);
            long count = tag.getLongOr("count", 0);
            if (item != null && count > 0) {
                entries.add(new MeItemStack(item, count));
            }
        }
        return entries;
    }

    /** Отфильтровать записи сети по текстовому запросу (по имени предмета). */
    public static List<MeItemStack> filterEntries(List<MeItemStack> entries, String query) {
        if (query == null || query.isBlank()) {
            return entries;
        }
        String lower = query.toLowerCase(Locale.ROOT);
        return entries.stream()
                .filter(entry -> entry.item().getDefaultInstance().getHoverName()
                        .getString().toLowerCase(Locale.ROOT).contains(lower))
                .toList();
    }

    private MePackets() {
    }
}

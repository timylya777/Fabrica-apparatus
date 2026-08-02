package com.fabrica.gui;

import com.fabrica.FabricaMod;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

/**
 * Packets for the item pipe connection settings menu.
 */
public final class PipeSettingsPackets {

    // Пакет клиент → сервер: переключение белого/чёрного списка фильтра трубы.
    public record ItemPipeWhitelistPayload(int containerId, boolean whitelist)
            implements CustomPacketPayload {
        public static final Type<ItemPipeWhitelistPayload> TYPE = new Type<>(FabricaMod.id("item_pipe_whitelist"));
        public static final StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, ItemPipeWhitelistPayload> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, ItemPipeWhitelistPayload::containerId,
                ByteBufCodecs.BOOL, ItemPipeWhitelistPayload::whitelist,
                ItemPipeWhitelistPayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    // Регистрация типа пакета и его глобального обработчика на сервере.
    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(ItemPipeWhitelistPayload.TYPE, ItemPipeWhitelistPayload.STREAM_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(ItemPipeWhitelistPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            // Выполняется на главном потоке сервера; применяется только если открытое
            // меню игрока — это меню настроек того же соединения (проверка по id меню).
            player.level().getServer().execute(() -> {
                if (player.containerMenu instanceof ItemPipeSettingsMenu menu
                        && menu.containerId == payload.containerId()) {
                    menu.setWhitelist(payload.whitelist());
                }
            });
        });
    }

    private PipeSettingsPackets() {
    }
}

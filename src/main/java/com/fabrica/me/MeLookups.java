package com.fabrica.me;

import com.fabrica.block.ModBlockEntities;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;

/**
 * Регистрация ItemStorage (Fabric transfer API) для ME-блоков:
 * привод (ME_DRIVE) получает хранилище своих слотов с дисками,
 * сетка (ME_GRID) — хранилище всей ME-сети.
 */
public final class MeLookups {

    /** Зарегистрировать lookup-и для ME-блоков (вызывается при инициализации мода). */
    public static void register() {
        // Привод: доступ к слотам контейнера дисков с любой стороны.
        ItemStorage.SIDED.registerForBlockEntity(
                (be, direction) -> new MeDriveContainerStorage(be.getDisks()),
                ModBlockEntities.ME_DRIVE
        );
        // Сетка: доступ к объединённому хранилищу ME-сети.
        ItemStorage.SIDED.registerForBlockEntity(
                (be, direction) -> new MeNetworkItemStorage(be),
                ModBlockEntities.ME_GRID
        );
    }

    private MeLookups() {
    }
}

package com.fabrica.me;

import com.fabrica.block.ModBlockEntities;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;

public final class MeLookups {

    public static void register() {
        ItemStorage.SIDED.registerForBlockEntity(
                (be, direction) -> new MeDriveContainerStorage(be.getDisks()),
                ModBlockEntities.ME_DRIVE
        );
        ItemStorage.SIDED.registerForBlockEntity(
                (be, direction) -> new MeNetworkItemStorage(be),
                ModBlockEntities.ME_GRID
        );
    }

    private MeLookups() {
    }
}

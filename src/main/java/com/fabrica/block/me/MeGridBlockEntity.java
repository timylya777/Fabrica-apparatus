package com.fabrica.block.me;

import com.fabrica.block.FabricaBlockEntity;
import com.fabrica.block.ModBlockEntities;
import com.fabrica.gui.MeGridMenu;
import com.fabrica.me.MeNetwork;
import com.fabrica.me.MeNetworkNode;
import com.fabrica.me.MeNetworkStorage;
import com.fabrica.me.MePackets;
import com.fabrica.me.MeStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

// Сущность ME-сетки: подключается к глобальной MeNetwork по своей позиции.
public class MeGridBlockEntity extends FabricaBlockEntity implements MenuProvider, MeNetworkNode {

    public MeGridBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ME_GRID, pos, state);
    }

    // Хранилище берём из MeNetwork по позиции блока; вне мира возвращаем пустое.
    @Override
    public MeStorage getMeStorage() {
        Level level = getLevel();
        return level == null ? MeNetworkStorage.empty() : MeNetwork.getStorage(level, worldPosition);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.fabrica_apparatus.me_grid");
    }

    // Открытие GUI на сервере сопровождается синхронизацией содержимого сети клиенту.
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        MeGridMenu menu = new MeGridMenu(containerId, inventory, this);
        if (player instanceof ServerPlayer serverPlayer) {
            MePackets.sendSync(serverPlayer, menu);
        }
        return menu;
    }
}

package com.fabrica.block.me;

import com.fabrica.block.FabricaBlockEntity;
import com.fabrica.block.ModBlockEntities;
import com.fabrica.gui.MeDriveMenu;
import com.fabrica.me.MeDriveStorage;
import com.fabrica.me.MeNetworkNode;
import com.fabrica.me.MeStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class MeDriveBlockEntity extends FabricaBlockEntity implements MenuProvider, MeNetworkNode {

    protected final SimpleContainer disks = new SimpleContainer(8) {
        @Override
        public void setChanged() {
            MeDriveBlockEntity.this.setChanged();
        }
    };
    private MeDriveStorage storage;

    public MeDriveBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ME_DRIVE, pos, state);
    }

    public SimpleContainer getDisks() {
        return disks;
    }

    @Override
    public MeStorage getMeStorage() {
        if (storage == null) {
            storage = new MeDriveStorage(disks);
        }
        return storage;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.fabrica_apparatus.me_drive");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new MeDriveMenu(containerId, inventory, this);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        disks.storeAsItemList(output.list("Disks", ItemStack.OPTIONAL_CODEC));
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        disks.fromItemList(input.listOrEmpty("Disks", ItemStack.OPTIONAL_CODEC));
    }
}

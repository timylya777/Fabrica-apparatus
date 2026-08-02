package com.fabrica.block.me;

import com.fabrica.block.FabricaBlockEntity;
import com.fabrica.block.ModBlockEntities;
import com.fabrica.gui.MeDriveMenu;
import com.fabrica.me.MeDriveStorage;
import com.fabrica.me.MeNetworkNode;
import com.fabrica.me.MeStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

// Дисковод ME: хранит диски и предоставляет MeStorage (виртуальное содержимое дисков).
public class MeDriveBlockEntity extends FabricaBlockEntity implements MenuProvider, MeNetworkNode {

    // 8 слотов для дисков ME.
    protected final SimpleContainer disks = new SimpleContainer(8) {
        @Override
        public void setChanged() {
            MeDriveBlockEntity.this.setChanged();
        }
    };
    // Кэш хранилища: создаётся лениво при первом запросе.
    private MeDriveStorage storage;

    public MeDriveBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ME_DRIVE, pos, state);
    }

    public SimpleContainer getDisks() {
        return disks;
    }

    // Создаём MeStorage поверх инвентаря дисков (только один раз).
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

    // При удалении блока (включая ломание) выбрасываем все диски наружу,
    // иначе содержимое дисковода терялось бы бесследно.
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        Level level = getLevel();
        if (level != null && !level.isClientSide()) {
            Containers.dropContents(level, worldPosition, disks);
        }
    }

    // Сохраняем диски списком предметов в NBT.
    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        disks.storeAsItemList(output.list("Disks", ItemStack.OPTIONAL_CODEC));
    }

    // Восстанавливаем диски из NBT.
    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        disks.fromItemList(input.listOrEmpty("Disks", ItemStack.OPTIONAL_CODEC));
    }
}

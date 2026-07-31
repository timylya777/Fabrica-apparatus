package com.fabrica.block.machine.generator;

import com.fabrica.api.energy.EnergyTier;
import com.fabrica.block.ModBlockEntities;
import com.fabrica.block.machine.fuel.AbstractFuelGeneratorBlockEntity;
import com.fabrica.gui.GeneratorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.block.state.BlockState;

public class GeneratorBlockEntity extends AbstractFuelGeneratorBlockEntity implements MenuProvider {

    public GeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GENERATOR, pos, state, 0, EnergyTier.LV, 0);
        if (state.getBlock() instanceof GeneratorBlock block) {
            this.productionRate = block.getProductionRate();
            this.produceTier = block.getTier();
            this.energyStorage = new com.fabrica.api.energy.EnergyStorageComponent(block.getCapacity(), block.getTier()) {
                @Override
                protected void onEnergyChanged() {
                    setChanged();
                }
            };
        }
    }

    public GeneratorBlockEntity(BlockPos pos, BlockState state, long capacity, EnergyTier tier, long productionRate) {
        super(ModBlockEntities.GENERATOR, pos, state, capacity, tier, productionRate);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.fabrica_apparatus.generator");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new GeneratorMenu(containerId, inventory, this, new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> (int) energyStorage.getEnergy();
                    case 1 -> (int) energyStorage.getCapacity();
                    case 2 -> burnTime;
                    case 3 -> totalBurnTime;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
            }

            @Override
            public int getCount() {
                return 4;
            }
        });
    }
}

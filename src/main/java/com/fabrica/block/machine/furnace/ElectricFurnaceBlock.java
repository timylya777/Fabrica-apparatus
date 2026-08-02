package com.fabrica.block.machine.furnace;

import com.fabrica.api.energy.EnergyTier;
import com.fabrica.block.machine.MachineBlock;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

// Электропечь: потребляет энергию для переплавки предметов.
public class ElectricFurnaceBlock extends MachineBlock {
    public static final MapCodec<ElectricFurnaceBlock> CODEC = simpleCodec(ElectricFurnaceBlock::new);

    // Параметры печи из реестра: ёмкость, тир, потребление (FE/тик).
    private final long capacity;
    private final EnergyTier tier;
    private final long consumptionRate;

    public ElectricFurnaceBlock(BlockBehaviour.Properties properties) {
        this(properties, 0, EnergyTier.LV, 0);
    }

    public ElectricFurnaceBlock(BlockBehaviour.Properties properties, long capacity, EnergyTier tier, long consumptionRate) {
        super(properties);
        this.capacity = capacity;
        this.tier = tier;
        this.consumptionRate = consumptionRate;
    }

    @Override
    public MapCodec<? extends ElectricFurnaceBlock> codec() {
        return CODEC;
    }

    public long getCapacity() { return capacity; }
    public EnergyTier getTier() { return tier; }
    public long getConsumptionRate() { return consumptionRate; }

    // Клик по печи открывает её GUI (только на сервере).
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            if (level.getBlockEntity(pos) instanceof MenuProvider menuProvider) {
                player.openMenu(menuProvider);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof MenuProvider menuProvider) {
            return menuProvider;
        }
        return super.getMenuProvider(state, level, pos);
    }

    // Передаём параметры энергии в сущность печи.
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ElectricFurnaceBlockEntity(pos, state, capacity, tier, consumptionRate);
    }
}

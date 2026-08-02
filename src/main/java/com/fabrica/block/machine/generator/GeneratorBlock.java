package com.fabrica.block.machine.generator;

import com.fabrica.api.energy.EnergyTier;
import com.fabrica.block.machine.MachineBlock;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

// Генератор: сжигает топливо и вырабатывает энергию для сети.
public class GeneratorBlock extends MachineBlock {
    public static final MapCodec<GeneratorBlock> CODEC = simpleCodec(GeneratorBlock::new);

    // Параметры генератора из реестра: ёмкость, тир, производство (FE/тик).
    private final long capacity;
    private final EnergyTier tier;
    private final long productionRate;

    public GeneratorBlock(BlockBehaviour.Properties properties) {
        this(properties, 0, EnergyTier.LV, 0);
    }

    public GeneratorBlock(BlockBehaviour.Properties properties, long capacity, EnergyTier tier, long productionRate) {
        super(properties);
        this.capacity = capacity;
        this.tier = tier;
        this.productionRate = productionRate;
    }

    @Override
    public MapCodec<? extends GeneratorBlock> codec() {
        return CODEC;
    }

    public long getCapacity() { return capacity; }
    public EnergyTier getTier() { return tier; }
    public long getProductionRate() { return productionRate; }

    // Генератор отдаёт энергию в любом направлении.
    @Override
    public boolean canConnectEnergy(BlockPos pos, BlockState state, Direction fromNeighborToUs) {
        return true;
    }

    // Клик по генератору открывает его GUI (только на сервере).
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

    // Передаём параметры энергии в сущность генератора.
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GeneratorBlockEntity(pos, state, capacity, tier, productionRate);
    }
}

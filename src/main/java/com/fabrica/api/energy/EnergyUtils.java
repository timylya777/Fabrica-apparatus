package com.fabrica.api.energy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Вспомогательные методы работы с энергией.
 */
public final class EnergyUtils {

    private EnergyUtils() {
    }

    /**
     * Получить энергетическое хранилище соседнего блока.
     */
    public static IEnergyStorage getNeighborEnergyStorage(
            Level world,
            BlockPos pos,
            Direction direction
    ) {

        BlockPos target = pos.relative(direction);

        BlockEntity be = world.getBlockEntity(target);

        if (be instanceof IEnergyStorage storage) {
            return storage;
        }

        return null;
    }

    /**
     * Передать энергию между двумя блоками.
     *
     * @return количество реально переданной энергии
     */
    public static long transferEnergy(
            Level world,
            BlockPos from,
            BlockPos to,
            long maxAmount
    ) {

        BlockEntity fromEntity = world.getBlockEntity(from);
        BlockEntity toEntity = world.getBlockEntity(to);

        if (!(fromEntity instanceof IEnergyStorage source))
            return 0;

        if (!(toEntity instanceof IEnergyStorage destination))
            return 0;

        if (!source.canExtract())
            return 0;

        if (!destination.canReceive())
            return 0;

        long extracted = source.extractEnergy(maxAmount, true);

        if (extracted <= 0)
            return 0;

        long accepted = destination.receiveEnergy(extracted, true);

        if (accepted <= 0)
            return 0;

        source.extractEnergy(accepted, false);
        destination.receiveEnergy(accepted, false);

        return accepted;
    }
}
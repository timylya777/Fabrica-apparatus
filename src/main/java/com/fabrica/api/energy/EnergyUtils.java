package com.fabrica.apparatus.api.energy;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

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
            World world,
            BlockPos pos,
            Direction direction
    ) {

        BlockPos target = pos.offset(direction);

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
            World world,
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
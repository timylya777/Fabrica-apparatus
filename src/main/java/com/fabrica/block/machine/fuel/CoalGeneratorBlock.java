package com.fabrica.block.machine.fuel;

// Импортируем готовые блоки и API
import com.fabrica.api.energy.EnergyTier;
import com.fabrica.block.machine.MachineBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Concrete block for a coal generator
 */
public class CoalGeneratorBlock extends MachineBlock {

    // Храним конфиг блока: ёмкость буфера, тир, скорость генерации
    private final long capacity;
    private final EnergyTier tier;
    private final long productionRate;

    // Конструктор принимает Properties (звук, прочность) + параметры генератора
    public CoalGeneratorBlock(BlockBehaviour.Properties properties, long capacity, EnergyTier tier, long productionRate) {
        // super() передаёт настройки в MachineBlock → FabricaBlock → Block
        super(properties);
        this.capacity = capacity;
        this.tier = tier;
        this.productionRate = productionRate;
    }

    // Геттеры нужны, чтобы BlockEntity мог прочитать конфиг своего блока
    public long getCapacity() { return capacity; }
    public EnergyTier getTier() { return tier; }
    public long getProductionRate() { return productionRate; }

    @Nullable
    @Override
    // EntityBlock требует: создать BlockEntity для этой позиции
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        // Создаём CoalGeneratorBlockEntity с параметрами из блока
        return new CoalGeneratorBlockEntity(pos, state, capacity, tier, productionRate);
    }

    // Можно переопределить getTicker() из MachineBlock, если нужна своя логика тиков
    // Но MachineBlock.getTicker() уже вызывает serverTick() у MachineBlockEntity
    // CoalGeneratorBlockEntity наследует это — тикать будет само
}

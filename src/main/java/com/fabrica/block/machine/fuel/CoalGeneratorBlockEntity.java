package com.fabrica.block.machine.fuel;

import com.fabrica.api.energy.EnergyTier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * CoalGenerator — сжигает уголь, производит EU.
 *
 * Наследуется от AbstractFuelGeneratorBlockEntity, который уже содержит:
 *   - EnergyStorageComponent (буфер энергии)
 *   - fuelInventory (1 слот для топлива)
 *   - burnTime / totalBurnTime (сколько тиков ещё гореть)
 *   - saveAdditional / loadAdditional (сохранение энергии + burnTime через ValueOutput/ValueInput)
 *   - serverTick() → produceEnergy()
 */
public class CoalGeneratorBlockEntity extends AbstractFuelGeneratorBlockEntity {

    // ===== КОНСТРУКТОР #1: для размещения (вызывается из newBlockEntity) =====

    /**
     * Вызывается из CoalGeneratorBlock.newBlockEntity(),
     * когда игрок ставит блок руками.
     *
     * Параметры (capacity, tier, productionRate) берутся из
     * конфига блока CoalGeneratorBlock.
     *
     * super(idBE, pos, state, capacity, tier, productionRate):
     *   - idBE = FabricaGeneratorMachines.COAL_GENERATOR_BE (зарегистрированный тип)
     *   - pos/state = координаты и состояние блока
     *   - capacity = 4000 (макс. буфер)
     *   - tier = LV (напряжение)
     *   - productionRate = 30 (EU/t при горении)
     */
    public CoalGeneratorBlockEntity(BlockPos pos, BlockState state, long capacity, EnergyTier tier, long productionRate) {
        super(FabricaGeneratorMachines.COAL_GENERATOR_BE, pos, state, capacity, tier, productionRate);
    }

    // ===== КОНСТРУКТОР #2: для загрузки с диска (вызывается из BlockEntityType) =====

    /**
     * BlockEntityType.BlockEntitySupplier — это functional interface:
     *   (BlockPos, BlockState) -> BlockEntity
     *
     * Minecraft сам вызывает этот конструктор, когда:
     *   - Мир загружается и читает BlockEntity из сохранения
     *   - Чанк прогружается
     *
     * После этого конструктора Minecraft вызывает loadAdditional(ValueInput),
     * который восстановит:
     *   - energyStorage (energy, capacity, tier)
     *   - burnTime / totalBurnTime
     *
     * Временные значения (0, LV, 0) — просто заглушка.
     * loadAdditional() перезапишет их реальными.
     */
    public CoalGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(FabricaGeneratorMachines.COAL_GENERATOR_BE, pos, state, 0, EnergyTier.LV, 0);

        // Читаем конфиг из блока, чтобы создать правильный storage
        if (state.getBlock() instanceof CoalGeneratorBlock block) {
            this.energyStorage = new com.fabrica.api.energy.EnergyStorageComponent(
                block.getCapacity(), block.getTier()
            ) {
                @Override
                protected void onEnergyChanged() {
                    setChanged(); // при изменении энергии — помечаемся на сохранение
                }
            };
        }
    }

    // ===== ЕДИНСТВЕННЫЙ МЕТОД, КОТОРЫЙ НУЖНО РЕАЛИЗОВАТЬ =====

    /**
     * AbstractFuelGeneratorBlockEntity.produceEnergy() вызывает этот метод,
     * когда burnTime == 0 и в fuelInventory есть предмет.
     *
     * Нужно вернуть: сколько тиков будет гореть этот предмет (тик = 1/20 сек).
     *
     * В 1.21.5 все печные burn times хранятся в FuelValues — синглтон мира.
     *
     * Как это работает:
     *   level (унаследовано от BlockEntity) — мир, в котором стоит блок.
     *   level.fuelValues() -> FuelValues (содержит все burn times)
     *   fuelValues.burnDuration(ItemStack) -> int (тики)
     *
     * Coal = 1600 тиков (80 секунд).
     * Coal Block = 16000 тиков (800 секунд).
     * Lava Bucket = 20000 тиков.
     * Любой печной предмет работает.
     *
     * Если level ещё null (на этапе загрузки) — вернём 0,
     * в следующем тике вызовется снова и найдёт топливо.
     */
    @Override
    protected int getFuelBurnTime(ItemStack fuel) {
        if (level == null) return 0;
        return level.fuelValues().burnDuration(fuel);
    }
}

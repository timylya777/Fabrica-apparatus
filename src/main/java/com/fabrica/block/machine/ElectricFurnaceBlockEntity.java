package com.fabrica.block.entity.machine;

import com.fabrica.block.entity.OverloadableMachineBlockEntity; // Или просто BlockEntity, если убрал наследование
import com.fabrica.energy.APConsumer;
import com.fabrica.energy.APStorage;
import com.fabrica.registry.ModBlockEntities;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext; // Если будешь использовать Fabric Transfer API позже
import net.minecraft.block.BlockState;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

// Реализуем APConsumer, чтобы сеть знала, что эта машина может принимать энергию
public class ElectricFurnaceBlockEntity extends OverloadableMachineBlockEntity implements APConsumer, SidedInventory {
    
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(2, ItemStack.EMPTY); // 0: Вход, 1: Выход
    private int progress = 0;
    
    private static final int MAX_PROGRESS = 200; // 10 секунд (20 тиков * 10)
    private static final int AP_COST_PER_TICK = 5; // Потребляет 5 AP за тик

    public ElectricFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ELECTRIC_FURNACE, pos, state, 
              // Ёмкость 5000 AP, может ПРИНЯТЬ макс. 32 AP/t (LV тир), не может ОТДАВАТЬ (0)
              new APStorage(5000, 32, 0));
    }

    // --- ОБЯЗАТЕЛЬНЫЙ МЕТОД ДЛЯ ИНТЕРФЕЙСА APConsumer ---
    @Override
    public APStorage getStorage() {
        return this.energyStorage; // Используем хранилище из родительского класса
    }

    // --- ЖИЗНЕННЫЙ ЦИКЛ: Подключение к сети при появлении в мире ---
    @Override
    public void setWorld(World world) {
        super.setWorld(world);
        if (!world.isClient) {
            // Регистрируем эту машину в глобальном менеджере сетей этого мира
            com.fabrica.FabricaMod.getManager(world).onBlockAdded(getPos(), this);
        }
    }

    // --- ЖИЗНЕННЫЙ ЦИКЛ: Отключение от сети при разрушении ---
    @Override
    public void markRemoved() {
        super.markRemoved();
        if (getWorld() != null && !getWorld().isClient) {
            com.fabrica.FabricaMod.getManager(getWorld()).onBlockRemoved(getPos());
        }
    }

    // --- ЛОГИКА МАШИНЫ ---
    @Override
    protected void serverTick(World world, BlockPos pos, BlockState state) {
        // 1. Сначала отрабатывает логика перегруза из родительского класса
        super.serverTick(world, pos, state);
        if (this.isRemoved()) return;

        ItemStack input = inventory.get(0);
        ItemStack output = inventory.get(1);

        // Заглушка для проверки рецепта (позже заменишь на RecipeManager)
        boolean hasRecipe = input.isOf(Items.IRON_ORE); 
        boolean canAcceptOutput = output.isEmpty() || (output.isOf(Items.IRON_INGOT) && output.getCount() < output.getMaxCount());

        if (hasRecipe && canAcceptOutput) {
            // Проверяем, есть ли у нас достаточно AP внутри нашего хранилища
            if (this.energyStorage.getAP() >= AP_COST_PER_TICK) {
                // Списываем энергию
                this.energyStorage.extractAP(AP_COST_PER_TICK, false);
                
                progress++;
                // Машина работает штатно, уровень перегруза падает (остывает)
                this.overloadLevel = Math.max(0, this.overloadLevel - 1);

                // Если рецепт завершен
                if (progress >= MAX_PROGRESS) {
                    if (output.isEmpty()) {
                        inventory.set(1, new ItemStack(Items.IRON_INGOT));
                    } else {
                        output.increment(1);
                    }
                    input.decrement(1);
                    progress = 0;
                }
            } else {
                // Энергии не хватает, прогресс падает, машина остывает
                progress = Math.max(0, progress - 2);
                this.overloadLevel = Math.max(0, this.overloadLevel - 1); 
            }
        } else {
            // Нет рецепта или выход полон: сброс прогресса и остывание
            progress = Math.max(0, progress - 2);
            this.overloadLevel = Math.max(0, this.overloadLevel - 2);
        }

        markDirty();
    }

    @Override
    protected boolean isOperatingNormally() {
        // Работает нормально, если не достигнут предел перегруза
        return this.overloadLevel < this.maxOverload;
    }

    // --- NBT: Сохранение и загрузка ---
    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        Inventories.readNbt(nbt, inventory);
        this.progress = nbt.getInt("Progress");
        this.overloadLevel = nbt.getInt("OverloadLevel");
        this.energyStorage.readNbt(nbt); // Важно: читаем AP
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, inventory);
        nbt.putInt("Progress", progress);
        nbt.putInt("OverloadLevel", overloadLevel);
        this.energyStorage.writeNbt(nbt); // Важно: пишем AP
    }

    // --- SidedInventory (для воронок) ---
    @Override public DefaultedList<ItemStack> getItems() { return inventory; }
    @Override public int[] getAvailableSlots(Direction side) { 
        return side == Direction.DOWN ? new int[]{1} : new int[]{0}; // Вверх/бока - вход, низ - выход
    }
    @Override public boolean canInsert(int slot, ItemStack stack, Direction dir) { return slot == 0; }
    @Override public boolean canExtract(int slot, ItemStack stack, Direction dir) { return slot == 1; }
}
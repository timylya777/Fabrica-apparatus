package com.fabrica.block.entity.machine;

import com.fabrica.block.entity.OverloadableMachineBlockEntity;
import com.fabrica.energy.APProvider;
import com.fabrica.energy.APStorage;
import com.fabrica.registry.ModBlockEntities;
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

// Реализуем APProvider, чтобы сеть знала, что эта машина ОТДАЁТ энергию
public class CoalGeneratorBlockEntity extends OverloadableMachineBlockEntity implements APProvider, SidedInventory {
    
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(1, ItemStack.EMPTY); // Только слот для топлива
    private int burnTime = 0;
    
    private static final int AP_PER_TICK = 10;       // Генерирует 10 AP за тик
    private static final int COAL_BURN_TIME = 1600;  // Уголь горит 1600 тиков (80 секунд)
    private static final int MAX_EXTRACT = 32;       // LV тир: отдаёт максимум 32 AP/t

    public CoalGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COAL_GENERATOR, pos, state, 
              // Ёмкость 10000 AP, НЕ принимает извне (0), отдаёт максимум 32 AP/t
              new APStorage(10000, 0, MAX_EXTRACT));
    }

    // --- ОБЯЗАТЕЛЬНЫЙ МЕТОД ДЛЯ ИНТЕРФЕЙСА APProvider ---
    @Override
    public APStorage getStorage() {
        return this.energyStorage;
    }

    // --- ЖИЗНЕННЫЙ ЦИКЛ: Подключение к сети при появлении в мире ---
    @Override
    public void setWorld(World world) {
        super.setWorld(world);
        if (!world.isClient) {
            // Регистрируем генератор в глобальном менеджере сетей
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
        // 1. Сначала отрабатывает логика перегруза/взрыва из родительского класса
        super.serverTick(world, pos, state);
        if (this.isRemoved()) return;

        ItemStack fuel = inventory.get(0);

        if (burnTime > 0) {
            // Генератор пытается добавить 10 AP в свой внутренний буфер
            long generated = this.energyStorage.generateAP(AP_PER_TICK);
            
            // Если добавить не удалось (generated < 10), значит буфер ПОЛОН.
            // Это происходит, когда сеть не забирает энергию (нет потребителей или кабели переполнены).
            if (generated < AP_PER_TICK) {
                this.overloadLevel += 2; // БЫСТРЫЙ ПЕРЕГРЕВ!
            } else {
                burnTime--; // Всё ок, энергия ушла в буфер, горим дальше
            }
        } 
        // Если не горим, но в слоте есть уголь или древесный уголь
        else if (fuel.isOf(Items.COAL) || fuel.isOf(Items.CHARCOAL)) {
            fuel.decrement(1);
            burnTime = COAL_BURN_TIME;
        }

        markDirty();
    }

    @Override
    protected boolean isOperatingNormally() {
        // Работает нормально, если не достигнут предел перегруза 
        // И (либо не горит, либо в буфере есть место для новой энергии)
        return this.overloadLevel < this.maxOverload && (burnTime == 0 || !this.energyStorage.isFull());
    }

    // --- NBT: Сохранение и загрузка ---
    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        Inventories.readNbt(nbt, inventory);
        this.burnTime = nbt.getInt("BurnTime");
        this.overloadLevel = nbt.getInt("OverloadLevel");
        this.energyStorage.readNbt(nbt);
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, inventory);
        nbt.putInt("BurnTime", burnTime);
        nbt.putInt("OverloadLevel", overloadLevel);
        this.energyStorage.writeNbt(nbt);
    }

    // --- SidedInventory (для воронок) ---
    @Override 
    public DefaultedList<ItemStack> getItems() { 
        return inventory; 
    }
    
    @Override 
    public int[] getAvailableSlots(Direction side) { 
        return new int[]{0}; // Только один слот доступен для автоматики
    }
    
    @Override 
    public boolean canInsert(int slot, ItemStack stack, Direction dir) { 
        // Разрешаем вставлять только уголь или древесный уголь
        return slot == 0 && (stack.isOf(Items.COAL) || stack.isOf(Items.CHARCOAL)); 
    }
    
    @Override 
    public boolean canExtract(int slot, ItemStack stack, Direction dir) { 
        return false; // Из генератора ничего нельзя достать воронкой
    }
}
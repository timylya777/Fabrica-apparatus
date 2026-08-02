package com.fabrica.block.machine.macerator;

import com.fabrica.api.energy.EnergyConsumer;
import com.fabrica.api.energy.EnergyStorageComponent;
import com.fabrica.api.energy.EnergyTier;
import com.fabrica.block.ModBlockEntities;
import com.fabrica.block.machine.EnergyMachineBlockEntity;
import com.fabrica.gui.MaceratorMenu;
import com.fabrica.recipe.MaceratingRecipe;
import com.fabrica.recipe.ModRecipeTypes;
import com.fabrica.recipe.ProcessingInput;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.List;

// Мацератор: дробит сырьё (руду) в пыль, потребляя энергию.
public class MaceratorBlockEntity extends EnergyMachineBlockEntity implements EnergyConsumer, MenuProvider {

    // Входной слот (0): сырьё для дробления.
    protected final SimpleContainer inputInventory = new SimpleContainer(1) {
        @Override
        public void setChanged() {
            MaceratorBlockEntity.this.setChanged();
        }
    };
    // Выходной слот (0): готовая пыль.
    protected final SimpleContainer outputInventory = new SimpleContainer(1) {
        @Override
        public void setChanged() {
            MaceratorBlockEntity.this.setChanged();
        }
    };
    // Потребление энергии за тик и тир приёма энергии.
    protected long consumptionRate;
    protected EnergyTier consumeTier;

    // Текущий и полный прогресс дробления (в тиках).
    protected int progress = 0;
    protected int totalTime = 0;
    protected RecipeHolder<MaceratingRecipe> currentRecipe;

    // Конструктор без параметров (для CODEC): настройки берутся из блока.
    public MaceratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MACERATOR, pos, state, 0, EnergyTier.LV);
        if (state.getBlock() instanceof MaceratorBlock block) {
            this.consumptionRate = block.getConsumptionRate();
            this.consumeTier = block.getTier();
            this.energyStorage = new EnergyStorageComponent(block.getCapacity(), block.getTier()) {
                @Override
                protected void onEnergyChanged() {
                    setChanged();
                }
            };
        } else {
            this.consumptionRate = 0;
            this.consumeTier = EnergyTier.LV;
        }
    }

    public MaceratorBlockEntity(BlockPos pos, BlockState state, long capacity, EnergyTier tier, long consumptionRate) {
        super(ModBlockEntities.MACERATOR, pos, state, capacity, tier);
        this.consumptionRate = consumptionRate;
        this.consumeTier = tier;
    }

    // Запрос энергии у сети: двойное потребление за тик, но не больше свободной ёмкости.
    @Override
    public long getEnergyDemand() {
        return Math.min(consumptionRate * 2, energyStorage.getCapacity() - energyStorage.getEnergy());
    }

    @Override
    public void receiveEnergy(long amount) {
        energyStorage.addEnergy(amount);
    }

    @Override
    public EnergyTier getConsumeTier() {
        return consumeTier;
    }

    @Override
    public EnergyConsumer getEnergyConsumer() {
        return this;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.fabrica_apparatus.macerator");
    }

    @Override
    // Данные для GUI: энергия, ёмкость, текущий и полный прогресс.
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new MaceratorMenu(containerId, inventory, this, new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> (int) energyStorage.getEnergy();
                    case 1 -> (int) energyStorage.getCapacity();
                    case 2 -> progress;
                    case 3 -> totalTime;
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

    // Каждый серверный тик: без энергии мацератор не работает, иначе дробит по рецепту.
    @Override
    public void serverTick() {
        if (energyStorage.getEnergy() <= 0) return;

        ItemStack input = inputInventory.getItem(0);
        if (input.isEmpty()) {
            currentRecipe = null;
            progress = 0;
            totalTime = 0;
            return;
        }

        RecipeHolder<MaceratingRecipe> recipe = findRecipe(input);
        // Предмет сменился: сбрасываем прогресс и начинаем новый рецепт.
        if (currentRecipe != recipe) {
            currentRecipe = recipe;
            progress = 0;
            totalTime = recipe != null ? recipe.value().getTime() : 0;
        }

        if (recipe == null || totalTime <= 0) return;

        ItemStack result = recipe.value().assemble(new ProcessingInput(List.of(input)));
        if (!canPlaceResult(result)) return;

        // Тратим до consumptionRate энергии за тик и двигаем прогресс.
        long used = Math.min(consumptionRate, energyStorage.getEnergy());
        if (used <= 0) return;
        energyStorage.removeEnergy(used);
        progress++;

        // Дробление завершено: кладём результат в выход и тратим один входной предмет.
        if (progress >= totalTime) {
            ItemStack current = outputInventory.getItem(0);
            if (current.isEmpty()) {
                outputInventory.setItem(0, result.copy());
            } else {
                current.grow(result.getCount());
            }
            input.shrink(1);
            progress = 0;
            currentRecipe = null;
            totalTime = 0;
        }
    }

    // Результат помещается в выход: слот пуст либо совпадает по типу и влезает по стеку.
    private boolean canPlaceResult(ItemStack result) {
        ItemStack current = outputInventory.getItem(0);
        if (current.isEmpty()) return true;
        if (!ItemStack.isSameItemSameComponents(current, result)) return false;
        return current.getCount() + result.getCount() <= current.getMaxStackSize();
    }

    // Ищем рецепт мацерации для входного предмета в серверном менеджере рецептов.
    private RecipeHolder<MaceratingRecipe> findRecipe(ItemStack input) {
        if (level == null || level.getServer() == null) return null;
        return level.getServer().getRecipeManager()
                .getRecipeFor(ModRecipeTypes.MACERATING, new ProcessingInput(List.of(input)), level)
                .orElse(null);
    }

    public SimpleContainer getInputInventory() {
        return inputInventory;
    }

    public SimpleContainer getOutputInventory() {
        return outputInventory;
    }

    public int getProgress() {
        return progress;
    }

    public int getTotalTime() {
        return totalTime;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("progress", progress);
        output.putInt("totalTime", totalTime);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.progress = input.getIntOr("progress", 0);
        this.totalTime = input.getIntOr("totalTime", 0);
    }
}

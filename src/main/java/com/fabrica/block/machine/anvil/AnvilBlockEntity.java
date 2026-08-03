package com.fabrica.block.machine.anvil;

import com.fabrica.block.ModBlockEntities;
import com.fabrica.block.machine.MachineBlockEntity;
import com.fabrica.gui.AnvilMenu;
import com.fabrica.item.FigureItem;
import com.fabrica.recipe.AnvilRecipe;
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

public class AnvilBlockEntity extends MachineBlockEntity implements MenuProvider {
    protected final SimpleContainer figureInventory = new SimpleContainer(1) {
        @Override
        public void setChanged() { AnvilBlockEntity.this.setChanged(); }
    };
    protected final SimpleContainer inputInventory = new SimpleContainer(1) {
        @Override
        public void setChanged() { AnvilBlockEntity.this.setChanged(); }
    };
    protected final SimpleContainer outputInventory = new SimpleContainer(1) {
        @Override
        public void setChanged() { AnvilBlockEntity.this.setChanged(); }
    };

    protected int progress = 0;
    protected int totalTime = 0;
    protected RecipeHolder<AnvilRecipe> currentRecipe;

    public AnvilBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ANVIL, pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.fabrica_apparatus.anvil");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new AnvilMenu(containerId, inventory, this, new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> progress;
                    case 1 -> totalTime;
                    case 2 -> getFigureDamage();
                    case 3 -> getFigureMaxDamage();
                    default -> 0;
                };
            }
            @Override
            public void set(int index, int value) { }
            @Override
            public int getCount() { return 4; }
        });
    }

    @Override
    public void serverTick() {
        ItemStack figure = figureInventory.getItem(0);
        ItemStack input = inputInventory.getItem(0);
        if (figure.isEmpty() || input.isEmpty()) {
            currentRecipe = null; progress = 0; totalTime = 0; return;
        }
        if (!(figure.getItem() instanceof FigureItem figureItem)) {
            currentRecipe = null; progress = 0; totalTime = 0; return;
        }
        RecipeHolder<AnvilRecipe> recipe = findRecipe(input);
        if (currentRecipe != recipe) {
            currentRecipe = recipe;
            progress = 0;
            totalTime = recipe != null
                ? Math.max(1, recipe.value().getTime() / figureItem.getSpeedMultiplier())
                : 0;
        }
        if (recipe == null || totalTime <= 0) return;
        ItemStack result = recipe.value().assemble(new ProcessingInput(List.of(input)));
        if (!canPlaceResult(result)) return;
        progress++;
        if (progress >= totalTime) {
            ItemStack current = outputInventory.getItem(0);
            if (current.isEmpty()) outputInventory.setItem(0, result.copy());
            else current.grow(result.getCount());
            input.shrink(1);
            damageFigure(figure, recipe.value().getDamage());
            progress = 0; currentRecipe = null; totalTime = 0;
        }
    }

    private void damageFigure(ItemStack figure, int damage) {
        figure.setDamageValue(figure.getDamageValue() + damage);
        if (figure.getDamageValue() >= figure.getMaxDamage()) {
            figureInventory.setItem(0, ItemStack.EMPTY);
        }
    }

    private boolean canPlaceResult(ItemStack result) {
        ItemStack current = outputInventory.getItem(0);
        if (current.isEmpty()) return true;
        if (!ItemStack.isSameItemSameComponents(current, result)) return false;
        return current.getCount() + result.getCount() <= current.getMaxStackSize();
    }

    private RecipeHolder<AnvilRecipe> findRecipe(ItemStack input) {
        if (level == null || level.getServer() == null) return null;
        return level.getServer().getRecipeManager()
                .getRecipeFor(ModRecipeTypes.ANVIL, new ProcessingInput(List.of(input)), level)
                .orElse(null);
    }

    public int getFigureDamage() {
        ItemStack figure = figureInventory.getItem(0);
        return figure.isEmpty() ? 0 : figure.getDamageValue();
    }

    public int getFigureMaxDamage() {
        ItemStack figure = figureInventory.getItem(0);
        return figure.isEmpty() ? 0 : figure.getMaxDamage();
    }

    public SimpleContainer getFigureInventory() { return figureInventory; }
    public SimpleContainer getInputInventory() { return inputInventory; }
    public SimpleContainer getOutputInventory() { return outputInventory; }
    public int getProgress() { return progress; }
    public int getTotalTime() { return totalTime; }

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

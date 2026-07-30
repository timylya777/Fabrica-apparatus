package com.fabrica.api.machine;

import net.minecraft.world.item.ItemStack;

public interface FabricationRecipe {
    ItemStack getInput();
    ItemStack getOutput();
    long getEnergyCost();
    int getProcessTime();

    default boolean matches(ItemStack input) {
        return ItemStack.isSameItemSameComponents(getInput(), input);
    }
}

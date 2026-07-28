package com.fabrica.client.screen;

import com.fabrica.menu.ElectricFurnaceMenu;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ElectricFurnaceScreen extends AbstractContainerScreen<ElectricFurnaceMenu> {

    public ElectricFurnaceScreen(ElectricFurnaceMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void extractMenuBackground(GuiGraphicsExtractor extractor) {
        super.extractMenuBackground(extractor);
    }
}

package com.fabrica.client.screen;

import com.fabrica.menu.CoalGeneratorMenu;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class CoalGeneratorScreen extends AbstractContainerScreen<CoalGeneratorMenu> {

    public CoalGeneratorScreen(CoalGeneratorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void extractMenuBackground(GuiGraphicsExtractor extractor) {
        super.extractMenuBackground(extractor);
    }
}

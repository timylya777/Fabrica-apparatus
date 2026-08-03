package com.fabrica.client.gui;

import com.fabrica.FabricaMod;
import com.fabrica.gui.AnvilMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class AnvilScreen extends AbstractContainerScreen<AnvilMenu> {
    private static final Identifier TEXTURE = FabricaMod.id("textures/gui/gui_base.png");

    public AnvilScreen(AnvilMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(extractor, mouseX, mouseY, partialTick);
        extractor.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0.0F, 0.0F,
            imageWidth, imageHeight, 256, 256);
    }

    @Override
    public void extractContents(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
        super.extractContents(extractor, mouseX, mouseY, partialTick);
        int total = menu.getTotalTime();
        if (total > 0) {
            int width = menu.getProgress() * 24 / total;
            extractor.fill(leftPos + 79, topPos + 34, leftPos + 79 + width, topPos + 51, 0xFF00AA00);
        }
        if (menu.getFigureMaxDamage() > 0) {
            int width = (menu.getFigureMaxDamage() - menu.getFigureDamage()) * 48 / menu.getFigureMaxDamage();
            extractor.fill(leftPos + 8, topPos + 70, leftPos + 8 + width, topPos + 73, 0xFFFFAA00);
        }
    }
}
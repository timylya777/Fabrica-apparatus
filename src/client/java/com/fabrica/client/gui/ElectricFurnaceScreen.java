package com.fabrica.client.gui;

import com.fabrica.FabricaMod;
import com.fabrica.gui.ElectricFurnaceMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class ElectricFurnaceScreen extends AbstractContainerScreen<ElectricFurnaceMenu> {
    private static final Identifier TEXTURE = FabricaMod.id("textures/gui/blast_furnace.png");

    public ElectricFurnaceScreen(ElectricFurnaceMenu menu, Inventory inventory, Component title) {
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
        String energy = menu.getEnergy() + " / " + menu.getCapacity() + " AP";
        extractor.text(font, energy, leftPos + 8, topPos + 70, 0x404040);

        int total = menu.getTotalTime();
        if (total > 0) {
            int progress = menu.getProgress();
            int width = progress * 24 / total;
            extractor.fill(leftPos + 79, topPos + 34, leftPos + 79 + width, topPos + 34 + 17, 0xFF00AA00);
        }
    }
}

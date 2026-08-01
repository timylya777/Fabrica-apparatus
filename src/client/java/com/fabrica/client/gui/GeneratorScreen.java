package com.fabrica.client.gui;

import com.fabrica.FabricaMod;
import com.fabrica.gui.GeneratorMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class GeneratorScreen extends AbstractContainerScreen<GeneratorMenu> {
    private static final Identifier TEXTURE = FabricaMod.id("textures/gui/gui_base.png");

    public GeneratorScreen(GeneratorMenu menu, Inventory inventory, Component title) {
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
        String energy = menu.getStoredEnergy() + " / " + menu.getEnergyCapacity() + " AP";
        extractor.text(font, energy, leftPos + 8, topPos + 70, 0x404040);

        int total = menu.getTotalBurnTime();
        if (total > 0) {
            int burn = menu.getBurnTime();
            int height = burn * 14 / total;
            int x = leftPos + 56;
            int y = topPos + 36 + (14 - height);
            extractor.fill(x, y, x + 14, y + height, 0xFFFF6A00);
        }
    }
}

package com.fabrica.client.screen;

import com.fabrica.FabricaMod;
import com.fabrica.menu.ElectricFurnaceMenu;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class ElectricFurnaceScreen extends AbstractContainerScreen<ElectricFurnaceMenu> {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            FabricaMod.MOD_ID, "textures/gui/electric_furnace.png");

    public ElectricFurnaceScreen(ElectricFurnaceMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 166);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(extractor, mouseX, mouseY, partialTick);

        extractor.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos,
                0.0f, 0.0f, imageWidth, imageHeight, imageWidth, imageHeight);

        if (this.menu.getTotalCookTime() > 0) {
            int cookProgress = this.menu.getCookProgress();
            extractor.blit(RenderPipelines.GUI_TEXTURED, TEXTURE,
                    leftPos + 79, topPos + 35,
                    176.0f, 0.0f, cookProgress + 1, 16,
                    256, 256);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(extractor, mouseX, mouseY, partialTick);

        int energy = this.menu.getEnergyAmount();
        Component apText = Component.literal(energy + " AP");
        extractor.text(this.font, apText, leftPos + 8, topPos + 6, 0x404040, false);
    }
}

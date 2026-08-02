package com.fabrica.client.gui;

import com.fabrica.FabricaMod;
import com.fabrica.gui.MeDriveMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/**
 * Экран ME-диска (хранилища): простой контейнерный экран, который рисует
 * только фоновую текстуру. Слоты и содержимое диска обрабатывает
 * связанное меню {@link MeDriveMenu}.
 */
public class MeDriveScreen extends AbstractContainerScreen<MeDriveMenu> {
    private static final Identifier TEXTURE = FabricaMod.id("textures/gui/gui_base.png");

    public MeDriveScreen(MeDriveMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    /** Рисует подложку экрана: стандартную текстуру интерфейса. */
    @Override
    public void extractBackground(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(extractor, mouseX, mouseY, partialTick);
        extractor.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0.0F, 0.0F,
            imageWidth, imageHeight, 256, 256);
    }
}

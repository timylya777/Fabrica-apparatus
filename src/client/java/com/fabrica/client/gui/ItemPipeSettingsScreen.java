package com.fabrica.client.gui;

import com.fabrica.FabricaMod;
import com.fabrica.gui.ItemPipeSettingsMenu;
import com.fabrica.gui.PipeSettingsPackets.ItemPipeWhitelistPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/**
 * Экран настроек соединения предметной трубы: четыре слота фильтра и кнопка
 * переключения режима "белый/чёрный список". Режим показывается цветом кнопки
 * (зелёная — белый список, красная — чёрный) и меняется нажатием, после чего
 * на сервер отправляется пакет {@link ItemPipeWhitelistPayload}.
 */
/**
 * The connection settings screen of an item pipe: four filter slots and a
 * white/black list toggle.
 */
public class ItemPipeSettingsScreen extends AbstractContainerScreen<ItemPipeSettingsMenu> {
    private static final Identifier TEXTURE = FabricaMod.id("textures/gui/gui_base.png");
    private static final int FILTER_SLOT_X = 62;
    private static final int FILTER_SLOT_Y = 35;
    private static final int BUTTON_X = 62;
    private static final int BUTTON_Y = 58;
    private static final int BUTTON_W = 52;
    private static final int BUTTON_H = 16;

    public ItemPipeSettingsScreen(ItemPipeSettingsMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    /** Рисует подложку экрана и серые подложки четырёх слотов фильтра. */
    @Override
    public void extractBackground(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(extractor, mouseX, mouseY, partialTick);
        extractor.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0.0F, 0.0F,
            imageWidth, imageHeight, 256, 256);
        // Draw the four filter slot backgrounds
        for (int i = 0; i < 4; i++) {
            extractor.fill(leftPos + FILTER_SLOT_X + i * 18, topPos + FILTER_SLOT_Y,
                leftPos + FILTER_SLOT_X + i * 18 + 16, topPos + FILTER_SLOT_Y + 16, 0xFF6B6B6B);
        }
    }

    /**
     * Рисует кнопку режима: заливает её цветом в зависимости от текущего
     * состояния (белый/чёрный список) и выводит текст подписи по центру кнопки.
     */
    @Override
    public void extractContents(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
        super.extractContents(extractor, mouseX, mouseY, partialTick);
        int x = leftPos + BUTTON_X;
        int y = topPos + BUTTON_Y;
        boolean whitelist = menu.isWhitelist();
        extractor.fill(x, y, x + BUTTON_W, y + BUTTON_H, whitelist ? 0xFF2E8B57 : 0xFF8B0000);
        String label = whitelist
            ? Component.translatable("gui.fabrica_apparatus.item_pipe_settings.whitelist").getString()
            : Component.translatable("gui.fabrica_apparatus.item_pipe_settings.blacklist").getString();
        extractor.text(font, label, x + (BUTTON_W - font.width(label)) / 2, y + 4, 0xFFFFFF);
    }

    /**
     * Клик по кнопке переключает режим: отправляет на сервер пакет с новым
     * значением (инверсия текущего состояния белого/чёрного списка).
     */
    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int x = leftPos + BUTTON_X;
        int y = topPos + BUTTON_Y;
        if (event.x() >= x && event.x() < x + BUTTON_W && event.y() >= y && event.y() < y + BUTTON_H) {
            ClientPlayNetworking.send(new ItemPipeWhitelistPayload(menu.containerId, !menu.isWhitelist()));
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }
}

package com.fabrica.client.gui;

import com.fabrica.FabricaMod;
import com.fabrica.gui.MeGridMenu;
import com.fabrica.me.MeItemStack;
import com.fabrica.me.MePackets;
import com.fabrica.me.MePackets.MeGridInsertPayload;
import com.fabrica.me.MePackets.MeGridTakePayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Экран ME-сетки (GUI): показывает записи ME-хранилища сеткой 9x6 ячеек
 * с иконками предметов и их количеством, поддерживает поиск по строке
 * (поле ввода вверху), прокрутку колесом мыши и операции с записями:
 * take (взять предметы в руку) и insert (положить предметы из руки в сетку).
 * Записи фильтруются по поисковому запросу, серверу отправляются пакеты
 * {@link MeGridTakePayload} / {@link MeGridInsertPayload}.
 */
public class MeGridScreen extends AbstractContainerScreen<MeGridMenu> {
    private static final Identifier TEXTURE = FabricaMod.id("textures/gui/me_grid.png");
    private static final int COLS = 9;
    private static final int ROWS = 6;
    private static final int CELL = 18;
    private static final int GRID_X = 7;
    private static final int GRID_Y = 30;

    private EditBox searchBox;
    private String query = "";
    private int scroll;

    public MeGridScreen(MeGridMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 244);
    }

    /**
     * Создаёт поле поиска: введённый текст запоминается в нижнем регистре
     * (фильтр для {@link #visibleEntries()}) и сбрасывает прокрутку в начало.
     */
    @Override
    protected void init() {
        super.init();
        this.searchBox = new EditBox(font, leftPos + 8, topPos + 8, 160, 14,
                Component.translatable("gui.fabrica_apparatus.me_grid.search"));
        searchBox.setResponder(text -> {
            this.query = text == null ? "" : text.toLowerCase(Locale.ROOT);
            this.scroll = 0;
        });
        addRenderableWidget(searchBox);
        searchBox.setFocused(true);
    }

    /**
     * Возвращает записи, видимые на экране: фильтрует все записи меню по
     * поисковому запросу, ограничивает прокрутку допустимым максимумом и
     * берёт срез длиной до COLS*ROWS записей, начиная с позиции прокрутки.
     */
    private List<MeItemStack> visibleEntries() {
        List<MeItemStack> filtered = MePackets.filterEntries(menu.getEntries(), query);
        int maxScroll = Math.max(0, (int) Math.ceil((filtered.size() - COLS * ROWS) / (double) COLS));
        this.scroll = Math.max(0, Math.min(maxScroll, this.scroll));
        int start = Math.min(scroll * COLS, filtered.size());
        int end = Math.min(filtered.size(), start + COLS * ROWS);
        return filtered.subList(start, end);
    }

    /**
     * Рисует содержимое экрана: текст занятости/ёмкости в правом верхнем углу,
     * сетку из иконок предметов с количеством (декорации стака) и подсветку
     * наведённой ячейки с тултипом, включающим количество записей.
     */
    @Override
    public void extractContents(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
        super.extractContents(extractor, mouseX, mouseY, partialTick);

        String usedText = menu.getUsed() + " / " + menu.getCapacity();
        extractor.text(font, usedText, leftPos + 176 - font.width(usedText) - 8, topPos + 8, 0x404040);

        List<MeItemStack> entries = visibleEntries();
        for (int i = 0; i < entries.size(); i++) {
            int cellX = leftPos + GRID_X + (i % COLS) * CELL;
            int cellY = topPos + GRID_Y + (i / COLS) * CELL;
            MeItemStack entry = entries.get(i);
            ItemStack stack = entry.item().getDefaultInstance();
            stack.setCount((int) Math.min(entry.count(), Integer.MAX_VALUE));
            extractor.item(stack, cellX, cellY);
            extractor.itemDecorations(font, stack, cellX, cellY);
        }

        int hovered = cellIndex(mouseX, mouseY);
        if (hovered >= 0 && hovered < entries.size()) {
            int cellX = leftPos + GRID_X + (hovered % COLS) * CELL;
            int cellY = topPos + GRID_Y + (hovered / COLS) * CELL;
            extractor.fill(cellX, cellY, cellX + CELL, cellY + CELL, 0x80FFFFFF);
            MeItemStack entry = entries.get(hovered);
            ItemStack stack = entry.item().getDefaultInstance();
            List<Component> lines = new ArrayList<>(Screen.getTooltipFromItem(minecraft, stack));
            lines.add(Component.translatable("gui.fabrica_apparatus.me_grid.tooltip_count", entry.count()));
            extractor.setComponentTooltipForNextFrame(font, lines, mouseX, mouseY);
        }
    }

    /**
     * Обработка клика по ячейке сетки: если в руке пусто — отправляет пакет
     * take (Shift = 64 шт., иначе 1 шт.) для взятой записи; если в руке есть
     * предмет — отправляет пакет insert с количеством из руки.
     */
    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int index = cellIndex(event.x(), event.y());
        if (index >= 0) {
            List<MeItemStack> entries = visibleEntries();
            if (index < entries.size()) {
                if (menu.getCarried().isEmpty()) {
                    int count = event.hasShiftDown() ? 64 : 1;
                    ClientPlayNetworking.send(new MeGridTakePayload(
                            menu.containerId, query, index + scroll * COLS, count));
                } else {
                    ClientPlayNetworking.send(new MeGridInsertPayload(
                            menu.containerId, menu.getCarried().getCount()));
                }
            }
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    /**
     * Прокрутка колесом мыши: если курсор над сеткой, смещает позицию
     * прокрутки на одну строку вверх/вниз в пределах допустимого диапазона.
     */
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (cellIndex(mouseX, mouseY) >= 0) {
            List<MeItemStack> filtered = MePackets.filterEntries(menu.getEntries(), query);
            int maxScroll = Math.max(0, (int) Math.ceil((filtered.size() - COLS * ROWS) / (double) COLS));
            this.scroll = Math.max(0, Math.min(maxScroll, this.scroll + (verticalAmount > 0 ? -1 : 1)));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    /**
     * Пересчитывает координаты курсора в индекс ячейки сетки (0..COLS*ROWS-1);
     * возвращает -1, если курсор вне сетки.
     */
    private int cellIndex(double mouseX, double mouseY) {
        int x = (int) Math.floor(mouseX - leftPos - GRID_X);
        int y = (int) Math.floor(mouseY - topPos - GRID_Y);
        if (x < 0 || y < 0) {
            return -1;
        }
        int col = x / CELL;
        int row = y / CELL;
        if (col >= COLS || row >= ROWS) {
            return -1;
        }
        return row * COLS + col;
    }
}

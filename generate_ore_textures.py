#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Генератор текстур руд для мода Fabrica Apparatus v5 — «перекраска ванильных руд».

Принцип: каждая мод-руда использует структуру ОДНОЙ из ванильных руд
(уголь = мелкие точки, железо = средние жилы, золото = редкие крупные,
редстоун = маленькие жилы, алмаз = редкие кристаллы, лазурит = жилы).
Никакого морфизма, поворотов или «раздувания» — форма жил 1-в-1 ванильная.

Как это работает:
1. Извлекаем из jar 1.20.1: stone.png, deepslate.png (фоны) и 6 ванильных руд.
2. Для каждого рудного пикселя ванильной руды считаем дельту от среднего цвета:
   delta = vanilla_px - vanilla_avg.
3. Новый рудный пиксель = material_color + delta. Фон остаётся нетронутым.
4. Цвет материала корректируется под ванильный контраст (stone ~26, deepslate ~38),
   чтобы светлые материалы не «терялись», а тёмные читались.
"""

import os
import sys
import zipfile
import math

try:
    from PIL import Image
except ImportError:
    print("PIL (Pillow) не установлен. Установите: py -m pip install Pillow")
    sys.exit(1)


# ------------------------------------------------------------
# 1. Конфигурация
# ------------------------------------------------------------

MINECRAFT_JAR = os.path.expandvars(
    r"%APPDATA%\.minecraft\versions\1.20.1\1.20.1.jar"
)

RESOURCE_DIR = os.path.join(
    os.getcwd(),
    "src", "main", "resources", "assets", "fabrica_apparatus",
    "textures", "block"
)

PREVIEW_DIR = os.path.join(os.getcwd(), "texture_preview")

# Ванильные текстуры: фоны + 6 руд-эталонов (stone и deepslate варианты)
VANILLA_TEXTURES = {
    "stone":       "assets/minecraft/textures/block/stone.png",
    "deepslate":   "assets/minecraft/textures/block/deepslate.png",
    "coal":        "assets/minecraft/textures/block/coal_ore.png",
    "iron":        "assets/minecraft/textures/block/iron_ore.png",
    "gold":        "assets/minecraft/textures/block/gold_ore.png",
    "redstone":    "assets/minecraft/textures/block/redstone_ore.png",
    "diamond":     "assets/minecraft/textures/block/diamond_ore.png",
    "lapis":       "assets/minecraft/textures/block/lapis_ore.png",
    "deep_coal":   "assets/minecraft/textures/block/deepslate_coal_ore.png",
    "deep_iron":   "assets/minecraft/textures/block/deepslate_iron_ore.png",
    "deep_gold":   "assets/minecraft/textures/block/deepslate_gold_ore.png",
    "deep_redstone": "assets/minecraft/textures/block/deepslate_redstone_ore.png",
    "deep_diamond":"assets/minecraft/textures/block/deepslate_diamond_ore.png",
    "deep_lapis":  "assets/minecraft/textures/block/deepslate_lapis_ore.png",
}

# Медь исключена: её руда уже есть в ванильном Minecraft.
# Каждая мод-руда использует структуру конкретной ванильной руды.
MOD_ORE_SOURCES = {
    "tin":      "coal",       # уголь: мелкие компактные точки
    "lead":     "iron",       # железо: мелкие жилы
    "aluminum": "gold",       # золото: редкие крупные вкрапления
    "nickel":   "redstone",   # редстоун: маленькие жилки
    "silver":   "diamond",    # алмаз: редкие кристаллики
    "zinc":     "lapis",      # лазурит: жилы
}

# Цвета материалов (адаптированы под читаемость на фоне)
MOD_ORE_COLORS = {
    "tin":      (210, 210, 215),     # очень светлое серебро
    "lead":     (125, 115, 200),     # светло-фиолетовый
    "aluminum": (210, 210, 218),     # светлый холодный серый
    "nickel":   (150, 140, 82),      # приглушённый оливковый
    "silver":   (228, 228, 236),     # яркий белый
    "zinc":     (170, 182, 200),     # голубовато-серый
}

# Минимальный средний RGB-контраст руды к фону (как в ванильной iron_ore)
VANILLA_CONTRAST = {
    "stone":     26.0,
    "deepslate": 38.0,
}

ORE_MASK_THRESHOLD = 40.0


# ------------------------------------------------------------
# 2. Утилиты
# ------------------------------------------------------------

def load_vanilla():
    """Загружает ванильные текстуры из jar."""
    if not os.path.exists(MINECRAFT_JAR):
        print(f"Файл не найден: {MINECRAFT_JAR}")
        sys.exit(1)

    images = {}
    with zipfile.ZipFile(MINECRAFT_JAR, "r") as zf:
        for name, path in VANILLA_TEXTURES.items():
            try:
                with zf.open(path) as f:
                    images[name] = Image.open(f).convert("RGBA")
                    print(f"  Извлечено: {path} -> {name}")
            except KeyError:
                print(f"  ВНИМАНИЕ: не найдено: {path}")
    return images


def clamp(v):
    return max(0, min(255, int(v)))


def rgb_dist(a, b):
    return ((a[0] - b[0]) ** 2 + (a[1] - b[1]) ** 2 + (a[2] - b[2]) ** 2) ** 0.5


def extract_ore_data(ore_img, base_img, threshold=ORE_MASK_THRESHOLD):
    """
    Возвращает:
      mask      — list of list (0/1) рудных пикселей
      ore_avg   — средний RGB рудных пикселей
      deltas    — dict {(x,y): (dr,dg,db)}
    """
    w, h = ore_img.size
    op = ore_img.load()
    bp = base_img.load()

    mask = []
    ore_pixels = []
    for y in range(h):
        row = []
        for x in range(w):
            r1, g1, b1, _ = op[x, y]
            r2, g2, b2, _ = bp[x, y]
            if rgb_dist((r1, g1, b1), (r2, g2, b2)) > threshold:
                row.append(1)
                ore_pixels.append((x, y, r1, g1, b1))
            else:
                row.append(0)
        mask.append(row)

    if not ore_pixels:
        raise ValueError("В ванильной руде не найдено рудных пикселей")

    ore_avg = tuple(
        sum(p[i] for p in ore_pixels) // len(ore_pixels)
        for i in range(2, 5)
    )

    deltas = {}
    for x, y, r, g, b in ore_pixels:
        deltas[(x, y)] = (r - ore_avg[0], g - ore_avg[1], b - ore_avg[2])

    return mask, ore_avg, deltas


def adjust_material_color(mat_color, bg_color, target_contrast):
    """
    Масштабирует цвет материала от цвета фона так, чтобы средний RGB-контраст
    к фону был не ниже ванильного (чтобы руда читалась), сохраняя оттенок.
    """
    bg = bg_color[:3]
    vec = (mat_color[0] - bg[0], mat_color[1] - bg[1], mat_color[2] - bg[2])
    length = math.sqrt(sum(v * v for v in vec))
    if length < 1e-6:
        return mat_color
    if length < target_contrast:
        k = target_contrast / length
        return (
            clamp(bg[0] + vec[0] * k),
            clamp(bg[1] + vec[1] * k),
            clamp(bg[2] + vec[2] * k),
        )
    return mat_color


# ------------------------------------------------------------
# 3. Основная логика
# ------------------------------------------------------------

def main():
    print("=== Генератор текстур руд v5 (перекраска ванильных руд) ===")
    print()

    if not os.path.isdir(RESOURCE_DIR):
        print(f"Папка ресурсов не найдена: {RESOURCE_DIR}")
        sys.exit(1)

    os.makedirs(PREVIEW_DIR, exist_ok=True)

    print("Загружаю ванильные текстуры из jar...")
    vanilla = load_vanilla()
    if "stone" not in vanilla:
        print("Ошибка: не удалось загрузить текстуры из jar.")
        sys.exit(1)

    stone = vanilla["stone"]
    deepslate = vanilla["deepslate"]

    # Извлекаем маски/дельты для всех ванильных руд
    ore_data = {}
    for key in ("coal", "iron", "gold", "redstone", "diamond", "lapis"):
        ore_data[key] = extract_ore_data(vanilla[key], stone)
    for key in ("deep_coal", "deep_iron", "deep_gold", "deep_redstone",
                "deep_diamond", "deep_lapis"):
        ore_data[key] = extract_ore_data(vanilla[key], deepslate)

    # Базовые цвета фона
    stone_bg = stone.load()[0, 0]
    deep_bg = deepslate.load()[0, 0]

    processed = 0
    errors = []

    for ore_name, source_key in MOD_ORE_SOURCES.items():
        ore_color = MOD_ORE_COLORS[ore_name]

        for is_deepslate in (False, True):
            if is_deepslate:
                fname = f"deepslate_{ore_name}_ore.png"
                base_img = deepslate
                src_key = "deep_" + source_key
                bg_color = deep_bg
                target_contrast = VANILLA_CONTRAST["deepslate"]
            else:
                fname = f"{ore_name}_ore.png"
                base_img = stone
                src_key = source_key
                bg_color = stone_bg
                target_contrast = VANILLA_CONTRAST["stone"]

            if src_key not in ore_data:
                errors.append(f"{fname}: ванильная руда {src_key} не загружена")
                continue

            filepath = os.path.join(RESOURCE_DIR, fname)
            mask, _, deltas = ore_data[src_key]

            total = sum(sum(row) for row in mask)
            density = total / 256 * 100
            if total == 0:
                errors.append(f"{fname}: маска пустая")
                continue

            # Цвет материала с нужным контрастом к фону
            mat_color = adjust_material_color(
                ore_color, bg_color, target_contrast
            )

            result = base_img.copy().convert("RGBA")
            res_px = result.load()

            # Переносим ванильные дельты на цвет материала
            for (x, y), (dr, dg, db) in deltas.items():
                if mask[y][x] == 1:
                    res_px[x, y] = (
                        clamp(mat_color[0] + dr),
                        clamp(mat_color[1] + dg),
                        clamp(mat_color[2] + db),
                        255,
                    )

            result.save(filepath, "PNG")
            print(f"  [OK] {fname}  (плотность: {density:.0f}%, источник: {src_key})")
            processed += 1

            # Превью
            preview = result.resize((64, 64), Image.Resampling.NEAREST)
            preview.save(os.path.join(PREVIEW_DIR, f"new_{fname}"))

    # Коллаж "после"
    ore_files = []
    for ore_name in MOD_ORE_COLORS:
        ore_files.append(f"{ore_name}_ore.png")
    for ore_name in MOD_ORE_COLORS:
        ore_files.append(f"deepslate_{ore_name}_ore.png")

    n = len(MOD_ORE_COLORS)
    collage = Image.new("RGBA", (n * 48, 48 * 2), (0, 0, 0, 0))
    for idx, fname in enumerate(ore_files):
        fpath = os.path.join(RESOURCE_DIR, fname)
        if os.path.exists(fpath):
            img = Image.open(fpath).convert("RGBA").resize((48, 48), Image.Resampling.NEAREST)
            row = idx // n
            col = idx % n
            collage.paste(img, (col * 48, row * 48))

    collage_path = os.path.join(PREVIEW_DIR, "collage_new.png")
    collage.save(collage_path)
    print(f"\nКоллаж сохранён: {collage_path}")

    print()
    print(f"Обработано файлов: {processed}")
    if errors:
        print("Ошибки:")
        for e in errors:
            print(f"  - {e}")
    print("Готово!")


if __name__ == "__main__":
    main()
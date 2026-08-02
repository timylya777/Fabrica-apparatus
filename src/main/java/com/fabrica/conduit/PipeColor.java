package com.fabrica.conduit;

/**
 * Перечень цветов труб (16 цветов Minecraft + REGULAR — обычная серая труба).
 * Отвечает за хранение:
 * - числового кода цвета (color) — используется для окраски текстуры/модели трубы;
 * - английского названия и префикса (englishName/englishNamePrefix) — для локализации;
 * - технического имени и префикса (name/prefix) — для генерации id предметов вроде "white_fluid_pipe".
 * Конструктор без строк используется для обычной трубы (REGULAR): все строковые
 * поля остаются пустыми, чтобы не добавлять префикс к названию.
 */
public enum PipeColor {
	REGULAR(14599002),
	WHITE("White", "white", 16383998),
	LIGHT_GRAY("Light Gray", "light_gray", 10329495),
	GRAY("Gray", "gray", 4673362),
	BLACK("Black", "black", 1908001),
	BROWN("Brown", "brown", 8606770),
	RED("Red", "red", 11546150),
	ORANGE("Orange", "orange", 16351261),
	YELLOW("Yellow", "yellow", 16701501),
	LIME("Lime", "lime", 8439583),
	GREEN("Green", "green", 6192150),
	CYAN("Cyan", "cyan", 1481884),
	LIGHT_BLUE("Light Blue", "light_blue", 3847130),
	BLUE("Blue", "blue", 3949738),
	PURPLE("Purple", "purple", 8991416),
	MAGENTA("Magenta", "magenta", 13061821),
	PINK("Pink", "pink", 15961002);

	// Числовой ARGB-код цвета (используется в моделях/текстурах).
	public final int color;
	// Английское название, например "White" (для GUI и локализации).
	public final String englishName;
	// Техническое имя, например "white" (для id предмета/трубы).
	public final String name;
	// Английский префикс "White " (готов к подстановке перед названием трубы).
	public final String englishNamePrefix;
	// Технический префикс "white_" (готов к подстановке перед id трубы).
	public final String prefix;

	// Полный конструктор для цветных труб: запоминает названия, префиксы и код цвета.
	PipeColor(String englishName, String name, int color) {
		this.englishName = englishName;
		this.name = name;
		this.color = color;
		this.englishNamePrefix = englishName + " ";
		this.prefix = name + "_";
	}

	// Упрощённый конструктор для обычной (бесцветной) трубы: названия и префиксы пустые.
	PipeColor(int color) {
		this.englishName = "";
		this.name = "";
		this.englishNamePrefix = "";
		this.prefix = "";
		this.color = color;
	}
}

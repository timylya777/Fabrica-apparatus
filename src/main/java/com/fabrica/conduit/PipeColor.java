package com.fabrica.conduit;

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

	public final int color;
	public final String englishName;
	public final String name;
	public final String englishNamePrefix;
	public final String prefix;

	PipeColor(String englishName, String name, int color) {
		this.englishName = englishName;
		this.name = name;
		this.color = color;
		this.englishNamePrefix = englishName + " ";
		this.prefix = name + "_";
	}

	PipeColor(int color) {
		this.englishName = "";
		this.name = "";
		this.englishNamePrefix = "";
		this.prefix = "";
		this.color = color;
	}
}

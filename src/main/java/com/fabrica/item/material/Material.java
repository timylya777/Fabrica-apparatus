package com.fabrica.item.material;

// Материалы мода: основные металлы с рудами и сплав бронзы.
// hasOre — существует ли блок руды этого материала,
// color — базовый цвет (ARGB) для текстур и иконок.
public enum Material {
    COPPER("copper", true, 0xC8783C),
    TIN("tin", true, 0xCFCFCF),
    LEAD("lead", true, 0x5F5FA8),
    ALUMINUM("aluminum", true, 0xC8C8D0),
    NICKEL("nickel", true, 0xA8A898),
    SILVER("silver", true, 0xE0E0E8),
    ZINC("zinc", true, 0xB8BCC0),
    BRONZE("bronze", false, 0xB07A45);

    private final String id;
    private final boolean hasOre;
    private final int color;

    Material(String id, boolean hasOre, int color) {
        this.id = id;
        this.hasOre = hasOre;
        this.color = color;
    }

    public String id() {
        return id;
    }

    public boolean hasOre() {
        return hasOre;
    }

    public int color() {
        return color;
    }
}

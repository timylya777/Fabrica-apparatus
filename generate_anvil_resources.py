import json, os, shutil

base = "src/main/resources"
materials = ["aluminum", "bronze", "copper", "lead", "nickel", "silver", "tin", "zinc"]

recipe_dir = os.path.join(base, "data/fabrica_apparatus/recipe/anvil")
os.makedirs(recipe_dir, exist_ok=True)
for m in materials:
    recipe = {
        "type": "fabrica_apparatus:anvil",
        "ingredients": [f"fabrica_apparatus:{m}_ingot"],
        "result": {"id": f"fabrica_apparatus:{m}_plate", "count": 1},
        "time": 200,
        "damage": 1
    }
    with open(os.path.join(recipe_dir, f"{m}_plate_from_ingot.json"), "w", encoding="utf-8") as f:
        json.dump(recipe, f, indent=4)

shutil.copy(
    os.path.join(base, "assets/fabrica_apparatus/textures/block/macerator.png"),
    os.path.join(base, "assets/fabrica_apparatus/textures/block/anvil.png")
)

for lang in ["en_us", "ru_ru"]:
    path = os.path.join(base, f"assets/fabrica_apparatus/lang/{lang}.json")
    with open(path, "r", encoding="utf-8") as f:
        data = json.load(f)
    if lang == "en_us":
        data["block.fabrica_apparatus.anvil"] = "Anvil"
        data["item.fabrica_apparatus.figure.tooltip.speed"] = "Work speed: x%s"
        data["item.fabrica_apparatus.figure.tooltip.durability"] = "Durability: %s"
    else:
        data["block.fabrica_apparatus.anvil"] = "Наковальня"
        data["item.fabrica_apparatus.figure.tooltip.speed"] = "Скорость работы: x%s"
        data["item.fabrica_apparatus.figure.tooltip.durability"] = "Прочность: %s"
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=4, ensure_ascii=False)

print("OK")
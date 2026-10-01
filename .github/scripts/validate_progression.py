"""Check ore progression resources and cross-version parity without starting Minecraft."""
import argparse
import json
import pathlib
import re

ORES = ("copper", "lead", "cobalt", "tungsten", "pyrite")
GENERATED = {"copper": (11, 8, 10, 69), "lead": (10, 8, 2, 79),
             "tungsten": (8, 8, 20, 59), "pyrite": (9, 9, 30, 99)}

def validate(files, target):
    modern = target == "26.3"
    root = "port/src/main/resources/"
    main = files["port/src/main/java/com/foundations/magneticraft/FoundationsMagneticraft.java"]
    registered = set(re.findall(r'registerSimple(?:Item|BlockItem|Block)\("([^"]+)"', main))
    blocks = {f"{metal}_{kind}" for metal in ORES for kind in ("ore", "block")}
    assert blocks <= registered
    assert len(registered) == 92, (target, len(registered))
    resources = {p[len(root):]: json.loads(v) for p, v in files.items()
                 if p.startswith(root) and p.endswith(".json")}

    def resource(path):
        assert path in resources, f"Missing resource: {path}"
        return resources[path]

    def item(identifier):
        if identifier.startswith("magneticraft:"):
            assert identifier.split(":")[1] in registered, f"Unregistered item {identifier}"

    def ingredient(value):
        if modern:
            assert isinstance(value, str), f"26.3 ingredient uses old syntax: {value}"
            identifier = value
        else:
            assert isinstance(value, dict) and set(value) in ({"item"}, {"tag"})
            identifier = value.get("item", "#" + value.get("tag", ""))
        if identifier.startswith("#"):
            namespace, path = identifier[1:].split(":")
            if namespace != "minecraft":
                resource(f"data/{namespace}/tags/item/{path}.json")
        else:
            item(identifier)

    recipes = {p: v for p, v in resources.items() if p.startswith("data/magneticraft/recipe/")}
    assert len(recipes) == 67
    for path, recipe in recipes.items():
        assert recipe["type"] in ("minecraft:smelting", "minecraft:crafting_shaped",
                                   "minecraft:crafting_shapeless"), path
        item(recipe["result"]["id"])
        assert 1 <= recipe["result"].get("count", 1) <= 64
        if recipe["type"] == "minecraft:smelting":
            ingredient(recipe["ingredient"])
            assert recipe["experience"] == 0.1 and recipe["cookingtime"] == 200
        elif recipe["type"] == "minecraft:crafting_shaped":
            for value in recipe["key"].values():
                ingredient(value)
        else:
            for value in recipe["ingredients"]:
                ingredient(value)

    for metal in ORES:
        compression = resource(f"data/magneticraft/recipe/{metal}_block.json")
        assert compression["pattern"] == ["AAA"] * 3 and compression["result"]["count"] == 1
        expansion = resource(f"data/magneticraft/recipe/{metal}_from_block.json")
        assert expansion["result"]["count"] == 9
        assert expansion["result"]["id"] == f"magneticraft:{'sulfur' if metal == 'pyrite' else metal + '_ingot'}"
        for kind in ("ore", "block"):
            identifier = f"{metal}_{kind}"
            state = resource(f"assets/magneticraft/blockstates/{identifier}.json")
            assert state["variants"][""]["model"] == f"magneticraft:block/{identifier}"
            model = resource(f"assets/magneticraft/models/block/{identifier}.json")
            assert model["textures"]["all"] == f"magneticraft:block/{identifier}"
            loot = resource(f"data/magneticraft/loot_table/blocks/{identifier}.json")["pools"][0]
            assert loot["entries"][0]["name"] == f"magneticraft:{identifier}"
            assert ("condition" in loot) == modern
            assert ("conditions" in loot) != modern
            if modern:
                definition = resource(f"assets/magneticraft/items/{identifier}.json")
                assert definition["model"]["model"] == f"magneticraft:item/{identifier}"
    for path, recipe in recipes.items():
        if path.endswith("_chunk_smelting.json"):
            expected = 2 if "_rocky_chunk_" not in path or "/galena_rocky_chunk_" in path else 1
            assert recipe["result"]["count"] == expected, path
    assert "data/magneticraft/recipe/pyrite_ore_smelting.json" not in resources
    for metal, (count, size, low, high) in GENERATED.items():
        folder = "feature" if modern else "configured_feature"
        feature = resource(f"data/magneticraft/worldgen/{folder}/{metal}_ore.json")
        config = feature if modern else feature["config"]
        assert config["size"] == size
        assert isinstance(config["targets"][0]["state"], str) == modern
        placed = resource(f"data/magneticraft/worldgen/placed_feature/{metal}_ore.json")
        assert placed["placement"][0]["count"] == count
        height = placed["placement"][2]["height"]
        assert height["min_inclusive"]["absolute"] == low
        assert height["max_inclusive"]["absolute"] == high
        modifier = resource(f"data/magneticraft/neoforge/biome_modifier/{metal}_ore.json")
        assert modifier["biomes"] == "#minecraft:is_overworld"
    assert not any("worldgen/" in path and "cobalt" in path for path in resources)
    crushing = {p: v for p, v in resources.items() if p.startswith("data/magneticraft/magneticraft/crushing/")}
    assert len(crushing) == 34, len(crushing)
    for path, recipe in crushing.items():
        item(recipe["result"]["id"])
        assert 1 <= recipe["result"]["count"] <= 64
        assert 0 <= recipe["mining_level"] <= 4
        value = recipe["ingredient"]
        if value.startswith("#"):
            ns, tag = value[1:].split(":")
            resource(f"data/{ns}/tags/item/{tag}.json")
        else:
            item(value)
    assert resource("data/magneticraft/magneticraft/crushing/pyrite_ore.json")["result"]["count"] == 2
    for metal in ("iron", "gold", "copper", "lead", "tungsten"):
        assert resource(f"data/magneticraft/magneticraft/crushing/{metal}_block.json")["result"]["count"] == 5
    for tool, durability in (("stone", 130), ("iron", 250), ("steel", 750)):
        assert f'.durability({durability})' in main
        resource(f"assets/magneticraft/models/item/{tool}_hammer.json")
        if modern:
            resource(f"assets/magneticraft/items/{tool}_hammer.json")
    model = resource("assets/magneticraft/models/block/crushing_table.json")
    assert len(model["elements"]) == 3
    assert model["elements"][2]["to"] == [16, 14, 16]
    print(f"{target}: 92 registrations, 67 recipes, mining/loot/models/worldgen checks passed")

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--bundle")
    args = parser.parse_args()
    if args.bundle:
        for target, files in json.loads(pathlib.Path(args.bundle).read_text()).items():
            validate(files, target)
    else:
        props = dict(line.split("=", 1) for line in pathlib.Path("gradle.properties").read_text().splitlines()
                     if "=" in line and not line.startswith("#"))
        files = {str(p).replace("\\", "/"): p.read_text(encoding="utf-8")
                 for p in pathlib.Path("port").rglob("*") if p.suffix in (".java", ".json")}
        validate(files, props["minecraft_version"])

if __name__ == "__main__":
    main()

"""Generate data/ JSON: recipes, loot tables, tags, advancements, worldgen, loot modifiers."""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
D = ROOT / "src/main/resources/data"
M = "true_metallurgy"

def w(path, obj):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(obj, indent=2) + "\n")

def mc(name):
    return f"minecraft:{name}"

def tm(name):
    return f"{M}:{name}"

# ---------------- recipes (mirror TMRecipes) ----------------
R = D / M / "recipes"

def shaped(name, result, pattern, key, count=1, category="misc"):
    w(R / f"{name}.json", {
        "type": "minecraft:crafting_shaped", "category": category,
        "pattern": pattern,
        "key": {k: ({"tag": v[1:]} if v.startswith("#") else {"item": v}) for k, v in key.items()},
        "result": {"id": result, "count": count}})

def shapeless(name, result, ingredients, count=1, category="misc"):
    w(R / f"{name}.json", {
        "type": "minecraft:crafting_shapeless", "category": category,
        "ingredients": [{"tag": i[1:]} if i.startswith("#") else {"item": i} for i in ingredients],
        "result": {"id": result, "count": count}})

def cooking(kind, name, ingredient, result, xp, time):
    w(R / f"{name}.json", {
        "type": f"minecraft:{kind}", "category": "misc",
        "cookingtime": time, "experience": xp,
        "ingredient": {"item": ingredient}, "result": {"id": result}})

# tools
shaped("blacksmith_tongs", tm("blacksmith_tongs"), ["I I", " I ", " I "], {"I": mc("iron_ingot")}, category="equipment")
shaped("primitive_hammer", tm("primitive_hammer"), ["CC ", "CC ", " S "], {"C": mc("cobblestone"), "S": mc("stick")}, category="equipment")
shaped("copper_hammer", tm("copper_hammer"), ["CC ", "CC ", " S "], {"C": mc("copper_ingot"), "S": mc("stick")}, category="equipment")
shaped("iron_hammer", tm("iron_hammer"), ["II ", "II ", " S "], {"I": mc("iron_ingot"), "S": mc("stick")}, category="equipment")
shaped("steel_hammer", tm("steel_hammer"), ["BB ", "BB ", " S "], {"B": tm("steel_billet"), "S": mc("stick")}, category="equipment")
shaped("hardened_hammer", tm("hardened_hammer"), ["BB ", "BB ", " S "], {"B": tm("hardened_billet"), "S": mc("stick")}, category="equipment")
shaped("masterwork_hammer", tm("masterwork_hammer"), ["HHH", "HS ", " S "], {"H": tm("hardened_billet"), "S": mc("stick")}, category="equipment")
# workstations
shaped("primitive_forge", tm("primitive_forge"), ["BBB", "BFB", "BBB"], {"B": mc("stone_bricks"), "F": mc("furnace")})
shaped("iron_forge", tm("iron_forge"), ["III", "IFI", "III"], {"I": mc("iron_block"), "F": tm("primitive_forge")})
shaped("steel_forge", tm("steel_forge"), ["IBI", "BFB", "IBI"], {"I": mc("iron_block"), "B": tm("steel_billet"), "F": tm("iron_forge")})
shaped("master_forge", tm("master_forge"), ["HOH", "OFO", "HOH"], {"H": tm("hardened_billet"), "O": mc("obsidian"), "F": tm("steel_forge")})
shaped("bellows", tm("bellows"), ["PPP", "PLP", "PIP"], {"P": "#minecraft:planks", "L": mc("leather"), "I": mc("iron_ingot")})
shaped("basic_anvil", tm("basic_anvil"), ["III", " I ", "CCC"], {"I": mc("iron_ingot"), "C": mc("cobblestone")})
shaped("iron_anvil", tm("iron_anvil"), ["III", "IAI", "III"], {"I": mc("iron_ingot"), "A": tm("basic_anvil")})
shaped("steel_anvil", tm("steel_anvil"), ["SSS", "SAS", "SSS"], {"S": tm("steel_billet"), "A": tm("iron_anvil")})
shaped("master_anvil", tm("master_anvil"), ["HHH", "HAH", "OOO"], {"H": tm("hardened_billet"), "A": tm("steel_anvil"), "O": mc("obsidian")})
shaped("quenching_barrel", tm("quenching_barrel"), ["PIP", "P P", "PIP"], {"P": "#minecraft:planks", "I": mc("iron_ingot")})
shaped("grinding_wheel", tm("grinding_wheel"), ["S S", "SXS", "S S"], {"S": mc("stone"), "X": mc("stick")})
shaped("assembly_table", tm("assembly_table"), ["PPP", "PTP", "PPP"], {"P": "#minecraft:planks", "T": mc("crafting_table")})
shaped("tool_rack", tm("tool_rack"), ["S S", "PPP", "S S"], {"S": mc("stick"), "P": "#minecraft:planks"})
shaped("steel_block", tm("steel_block"), ["BB", "BB"], {"B": tm("steel_billet")}, category="building")
# pipeline
shapeless("crushed_iron_ore", tm("crushed_iron_ore"), [mc("raw_iron")])
shapeless("crushed_copper_ore", tm("crushed_copper_ore"), [mc("raw_copper")])
cooking("smelting", "iron_bloom_smelting", tm("crushed_iron_ore"), tm("iron_bloom"), 0.7, 200)
cooking("blasting", "iron_bloom_blasting", tm("crushed_iron_ore"), tm("iron_bloom"), 0.7, 100)
cooking("smelting", "copper_bloom_smelting", tm("crushed_copper_ore"), tm("copper_bloom"), 0.7, 200)
cooking("blasting", "copper_bloom_blasting", tm("crushed_copper_ore"), tm("copper_bloom"), 0.7, 100)
cooking("smelting", "coke_smelting", mc("coal"), tm("coke"), 0.5, 200)
shapeless("quench_oil", tm("quench_oil"), [mc("coal"), mc("charcoal"), mc("dried_kelp"), mc("glass_bottle")], count=2)
# handles + fittings
for wood, planks in (("oak", mc("oak_planks")), ("spruce", mc("spruce_planks")),
                     ("birch", mc("birch_planks")), ("dark_oak", mc("dark_oak_planks"))):
    shaped(f"{wood}_handle", tm(f"{wood}_handle"), ["P", "P"], {"P": planks}, count=2)
shaped("bamboo_handle", tm("bamboo_handle"), ["B", "B"], {"B": mc("bamboo")}, count=2)
shapeless("reinforced_handle", tm("reinforced_handle"), [tm("oak_handle"), mc("iron_ingot")])
for wood in ("oak", "spruce", "birch", "dark_oak", "bamboo"):
    shapeless(f"wrapped_{wood}_handle", tm("leather_wrapped_handle"), [tm(f"{wood}_handle"), mc("leather"), mc("string")])
shaped("bone_handle", tm("bone_handle"), ["B", "B"], {"B": mc("bone")}, count=2)
shaped("long_shaft", tm("long_shaft"), ["S", "S"], {"S": mc("stick")})
shapeless("binding", tm("binding"), [mc("leather"), mc("string")], count=2)
shaped("iron_guard", tm("iron_guard"), [" I ", "III", " I "], {"I": mc("iron_ingot")})
shaped("steel_guard", tm("steel_guard"), [" B ", "BBB", " B "], {"B": tm("steel_billet")})
shaped("iron_pommel", tm("iron_pommel"), [" I ", "I I"], {"I": mc("iron_ingot")})
shaped("steel_pommel", tm("steel_pommel"), [" B ", "B B"], {"B": tm("steel_billet")})
shapeless("blacksmith_journal", tm("blacksmith_journal"), [mc("book"), mc("leather")])

# custom: alloy
w(R / "alloy_steel_bloom.json", {"type": tm("alloy"),
    "primary": {"item": tm("iron_bloom")}, "carbon": {"item": tm("coke")},
    "min_temp": 1200, "time": 240, "result": {"id": tm("steel_bloom")}})
w(R / "alloy_hardened_bloom.json", {"type": tm("alloy"),
    "primary": {"item": tm("steel_bloom")}, "carbon": {"item": tm("coke")},
    "min_temp": 1320, "time": 300, "result": {"id": tm("hardened_bloom")}})
# custom: assembly
w(R / "assembly_sword.json", {"type": tm("assembly"), "category": "sword",
    "blade": {"item": tm("sword_blade")}, "slot_b": {"tag": tm("guards")},
    "slot_c": {"tag": tm("handles")}, "slot_d": {"tag": tm("pommels")}})
w(R / "assembly_axe.json", {"type": tm("assembly"), "category": "axe",
    "blade": {"item": tm("axe_head")}, "slot_b": {"tag": tm("handles")}})
w(R / "assembly_pickaxe.json", {"type": tm("assembly"), "category": "pickaxe",
    "blade": {"item": tm("pickaxe_head")}, "slot_b": {"tag": tm("handles")}})
w(R / "assembly_spear.json", {"type": tm("assembly"), "category": "spear",
    "blade": {"item": tm("spear_head")}, "slot_b": {"item": tm("long_shaft")},
    "slot_c": {"item": tm("binding")}})

print(f"recipes: {len(list(R.glob('*.json')))}")

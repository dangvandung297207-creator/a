#!/usr/bin/env python3
"""Generate item models (incl. heat-override chains) for The Master Blacksmith."""
import json, os

MOD = "masterblacksmith"
A = "src/main/resources/assets/masterblacksmith"
METALS = ["copper", "tin", "bronze", "iron", "steel", "hardened_steel", "damascus_steel", "starfall_steel"]
HANDLES = ["oak", "spruce", "birch", "dark_oak", "bamboo", "reinforced", "leather_wrapped", "bone"]

def w(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(obj, f, indent=2)
        f.write("\n")

HANDHELD = {
    "hammer_copper", "hammer_iron", "hammer_steel", "hammer_hardened", "hammer_masterwork",
    "tongs", "forged_sword", "forged_axe", "forged_pickaxe", "forged_spear",
}

# shape of each heat-capable item (hot variants are per-shape, like real glowing steel)
HOT_SHAPE = {}
for m in METALS:
    HOT_SHAPE[f"billet_{m}"] = "billet"
    HOT_SHAPE[f"bloom_{m}"] = "bloom"
for s in ["blade_blank", "axe_head_blank", "pick_head_blank", "spear_head_blank",
          "armor_plate_blank", "sword_blade", "axe_head", "pickaxe_head",
          "spear_head", "armor_plate"]:
    HOT_SHAPE[s] = s

ITEMS = list(HANDHELD)
for m in METALS:
    ITEMS += [f"processed_{m}", f"bloom_{m}", f"billet_{m}"]
ITEMS += ["raw_tin", "starfall_shard", "tin_ingot", "bronze_ingot", "steel_ingot",
          "hardened_steel_ingot", "damascus_steel_ingot", "starfall_steel_ingot",
          "refractory_brick", "coke", "leather_strip", "metal_rivet", "slag"]
ITEMS += ["blade_blank", "axe_head_blank", "pick_head_blank", "spear_head_blank",
          "armor_plate_blank", "sword_blade", "axe_head", "pickaxe_head",
          "spear_head", "armor_plate"]
ITEMS += ["guard_iron", "guard_steel", "guard_brass", "grip_leather", "grip_bone",
          "grip_exotic", "pommel_iron", "pommel_steel", "pommel_brass"]
ITEMS += [f"handle_{h}" for h in HANDLES]
ITEMS += ["forged_helmet", "forged_chestplate", "forged_leggings", "forged_boots"]
ITEMS += ["oil_bucket", "salt_water_bucket", "herbal_oil_bucket", "mineral_oil_bucket",
          "alchemical_oil_bucket", "blood_quench_vial", "starfall_quench_vial",
          "blacksmith_journal"]
ITEMS += ["blueprint_kings_edge", "blueprint_oathkeeper", "blueprint_stonesplitter",
          "blueprint_skypiercer", "blueprint_aegis"]

for name in sorted(set(ITEMS)):
    parent = "minecraft:item/handheld" if name in HANDHELD else "minecraft:item/generated"
    model = {"parent": parent, "textures": {"layer0": f"{MOD}:item/{name}"}}
    if name in HOT_SHAPE:
        shape = HOT_SHAPE[name]
        model["overrides"] = [
            {"predicate": {f"{MOD}:heat": 0.33}, "model": f"{MOD}:item/hot/{shape}_0"},
            {"predicate": {f"{MOD}:heat": 0.66}, "model": f"{MOD}:item/hot/{shape}_1"},
            {"predicate": {f"{MOD}:heat": 1.0}, "model": f"{MOD}:item/hot/{shape}_2"},
        ]
    w(f"{A}/models/item/{name}.json", model)

for shape in sorted(set(HOT_SHAPE.values())):
    for lvl in range(3):
        w(f"{A}/models/item/hot/{shape}_{lvl}.json", {
            "parent": "minecraft:item/generated",
            "textures": {"layer0": f"{MOD}:item/hot/{shape}_{lvl}"}})

print(f"item models OK: {len(set(ITEMS))} items + {len(set(HOT_SHAPE.values()))*3} hot variants")

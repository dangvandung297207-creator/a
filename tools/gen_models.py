#!/usr/bin/env python3
"""Generate blockstates + block/item models for The Master Blacksmith."""
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

def cube_all(name, tex=None):
    tex = tex or name
    w(f"{A}/models/block/{name}.json", {
        "parent": "minecraft:block/cube_all",
        "textures": {"all": f"{MOD}:block/{tex}"}})
    w(f"{A}/blockstates/{name}.json", {"variants": {"": {"model": f"{MOD}:block/{name}"}}})
    w(f"{A}/models/item/{name}.json", {"parent": f"{MOD}:block/{name}"})

CUBES = ["refractory_brick_block", "tin_ore", "deepslate_tin_ore", "starfall_ore",
         "tin_block", "raw_tin_block", "bronze_block", "steel_block", "starfall_block"]
for c in CUBES:
    cube_all(c)

# ---------- Forge hearths: per-tier sides/top, per-heat firebox ----------
TIERS = ["primitive", "iron", "steel", "master"]
YROT = {"north": 0, "east": 90, "south": 180, "west": 270}
for tier in TIERS:
    for heat in range(6):
        w(f"{A}/models/block/forge_hearth_{tier}_{heat}.json", {
            "parent": "minecraft:block/cube",
            "textures": {
                "particle": f"{MOD}:block/forge_side_{tier}",
                "down": f"{MOD}:block/forge_side_{tier}",
                "up": f"{MOD}:block/forge_top_{tier}",
                "north": f"{MOD}:block/forge_front_{heat}",
                "south": f"{MOD}:block/forge_side_{tier}",
                "east": f"{MOD}:block/forge_side_{tier}",
                "west": f"{MOD}:block/forge_side_{tier}"}})
    variants = {}
    for facing, rot in YROT.items():
        for heat in range(6):
            v = {"model": f"{MOD}:block/forge_hearth_{tier}_{heat}"}
            if rot:
                v["y"] = rot
            variants[f"facing={facing},heat={heat}"] = v
    w(f"{A}/blockstates/forge_hearth_{tier}.json", {"variants": variants})
    w(f"{A}/models/item/forge_hearth_{tier}.json",
      {"parent": f"{MOD}:block/forge_hearth_{tier}_0"})

# ---------- Bellows ----------
def bellows_model(name, bag_from, bag_to, lid_from, lid_to):
    w(f"{A}/models/block/{name}.json", {
        "parent": "minecraft:block/block",
        "textures": {"particle": f"{MOD}:block/bellows_wood",
                     "wood": f"{MOD}:block/bellows_wood",
                     "leather": f"{MOD}:block/bellows_leather"},
        "elements": [
            {"from": [1, 0, 1], "to": [15, 2, 15],
             "faces": {d: {"uv": [0, 0, 16, 16], "texture": "#wood"} for d in
                       ["down", "up", "north", "south", "west", "east"]}},
            {"from": [2, bag_from, 2], "to": [14, bag_to, 14],
             "faces": {d: {"uv": [0, 0, 16, 16], "texture": "#leather"} for d in
                       ["down", "up", "north", "south", "west", "east"]}},
            {"from": [1, lid_from, 1], "to": [15, lid_to, 15],
             "faces": {d: {"uv": [0, 0, 16, 16], "texture": "#wood"} for d in
                       ["down", "up", "north", "south", "west", "east"]}},
            {"from": [6, 1, -3], "to": [10, 4, 1],
             "faces": {d: {"uv": [0, 0, 8, 8], "texture": "#wood"} for d in
                       ["down", "up", "north", "south", "west", "east"]}},
        ]})
bellows_model("bellows", 2, 6, 6, 8)
bellows_model("bellows_pressed", 2, 4, 4, 6)
variants = {}
for facing, rot in YROT.items():
    for pressed in ("false", "true"):
        v = {"model": f"{MOD}:block/bellows" + ("_pressed" if pressed == "true" else "")}
        if rot:
            v["y"] = rot
        variants[f"facing={facing},pressed={pressed}"] = v
w(f"{A}/blockstates/bellows.json", {"variants": variants})
w(f"{A}/models/item/bellows.json", {"parent": f"{MOD}:block/bellows"})

# ---------- Anvils ----------
ANVILS = ["basic", "iron", "steel", "master"]
for tier in ANVILS:
    w(f"{A}/models/block/anvil_{tier}.json", {
        "parent": "minecraft:block/block",
        "textures": {"particle": f"{MOD}:block/anvil_side_{tier}",
                     "side": f"{MOD}:block/anvil_side_{tier}",
                     "top": f"{MOD}:block/anvil_top_{tier}"},
        "elements": [
            {"from": [1, 0, 1], "to": [15, 4, 15],
             "faces": {d: {"uv": [0, 0, 16, 16], "texture": "#side"} for d in
                       ["down", "up", "north", "south", "west", "east"]}},
            {"from": [4, 4, 4], "to": [12, 8, 12],
             "faces": {d: {"uv": [0, 0, 16, 16], "texture": "#side"} for d in
                       ["down", "up", "north", "south", "west", "east"]}},
            {"from": [0, 8, 2], "to": [16, 13, 14],
             "faces": {
                 "down": {"uv": [0, 0, 16, 16], "texture": "#side"},
                 "up": {"uv": [0, 0, 16, 16], "texture": "#top"},
                 "north": {"uv": [0, 0, 16, 16], "texture": "#side"},
                 "south": {"uv": [0, 0, 16, 16], "texture": "#side"},
                 "west": {"uv": [0, 0, 16, 16], "texture": "#side"},
                 "east": {"uv": [0, 0, 16, 16], "texture": "#side"}}},
        ]})
    variants = {}
    for facing, rot in YROT.items():
        for hot in ("false", "true"):
            v = {"model": f"{MOD}:block/anvil_{tier}"}
            if rot:
                v["y"] = rot
            variants[f"facing={facing},hot={hot}"] = v
    w(f"{A}/blockstates/anvil_{tier}.json", {"variants": variants})
    w(f"{A}/models/item/anvil_{tier}.json", {"parent": f"{MOD}:block/anvil_{tier}"})

# ---------- Quenching barrel ----------
for disp, top in [("empty", "barrel_top_empty"), ("water", "liquid_water"),
                  ("oil", "liquid_oil"), ("special", "liquid_special")]:
    w(f"{A}/models/block/quenching_barrel_{disp}.json", {
        "parent": "minecraft:block/cube",
        "textures": {"particle": f"{MOD}:block/barrel_side",
                     "down": f"{MOD}:block/barrel_side",
                     "up": f"{MOD}:block/{top}",
                     "north": f"{MOD}:block/barrel_side",
                     "south": f"{MOD}:block/barrel_side",
                     "east": f"{MOD}:block/barrel_side",
                     "west": f"{MOD}:block/barrel_side"}})
variants = {}
for liquid in ["none", "water", "oil", "special"]:
    disp = "empty" if liquid == "none" else liquid
    for lvl in range(4):
        variants[f"liquid={liquid},level={lvl}"] = {"model": f"{MOD}:block/quenching_barrel_{disp}"}
w(f"{A}/blockstates/quenching_barrel.json", {"variants": variants})
w(f"{A}/models/item/quenching_barrel.json", {"parent": f"{MOD}:block/quenching_barrel_empty"})

# ---------- Grinding wheel ----------
w(f"{A}/models/block/grinding_wheel.json", {
    "parent": "minecraft:block/block",
    "textures": {"particle": f"{MOD}:block/wheel_stone",
                 "frame": f"{MOD}:block/wheel_frame",
                 "stone": f"{MOD}:block/wheel_stone"},
    "elements": [
        {"from": [1, 0, 5], "to": [4, 12, 11],
         "faces": {d: {"uv": [0, 0, 8, 16], "texture": "#frame"} for d in
                   ["down", "up", "north", "south", "west", "east"]}},
        {"from": [12, 0, 5], "to": [15, 12, 11],
         "faces": {d: {"uv": [0, 0, 8, 16], "texture": "#frame"} for d in
                   ["down", "up", "north", "south", "west", "east"]}},
        {"from": [6, 3, 2], "to": [10, 15, 14],
         "faces": {
             "down": {"uv": [0, 0, 16, 16], "texture": "#stone"},
             "up": {"uv": [0, 0, 16, 16], "texture": "#stone"},
             "north": {"uv": [0, 0, 16, 16], "texture": "#stone"},
             "south": {"uv": [0, 0, 16, 16], "texture": "#stone"},
             "west": {"uv": [0, 0, 16, 16], "texture": "#stone"},
             "east": {"uv": [0, 0, 16, 16], "texture": "#stone"}}},
        {"from": [2, 0, 12], "to": [14, 3, 15],
         "faces": {d: {"uv": [0, 0, 16, 4], "texture": "#frame"} for d in
                   ["down", "up", "north", "south", "west", "east"]}},
    ]})
variants = {}
for facing, rot in YROT.items():
    for spin in ("false", "true"):
        v = {"model": f"{MOD}:block/grinding_wheel"}
        if rot:
            v["y"] = rot
        variants[f"facing={facing},spinning={spin}"] = v
w(f"{A}/blockstates/grinding_wheel.json", {"variants": variants})
w(f"{A}/models/item/grinding_wheel.json", {"parent": f"{MOD}:block/grinding_wheel"})

# ---------- Assembly table ----------
w(f"{A}/models/block/assembly_table.json", {
    "parent": "minecraft:block/block",
    "textures": {"particle": f"{MOD}:block/table_top",
                 "top": f"{MOD}:block/table_top",
                 "leg": f"{MOD}:block/table_side"},
    "elements": [
        {"from": [0, 12, 0], "to": [16, 14, 16],
         "faces": {
             "down": {"uv": [0, 0, 16, 16], "texture": "#leg"},
             "up": {"uv": [0, 0, 16, 16], "texture": "#top"},
             "north": {"uv": [0, 0, 16, 2], "texture": "#leg"},
             "south": {"uv": [0, 0, 16, 2], "texture": "#leg"},
             "west": {"uv": [0, 0, 16, 2], "texture": "#leg"},
             "east": {"uv": [0, 0, 16, 2], "texture": "#leg"}}},
        *[{"from": [x, 0, z], "to": [x + 2, 12, z + 2],
           "faces": {d: {"uv": [0, 0, 4, 16], "texture": "#leg"} for d in
                     ["down", "up", "north", "south", "west", "east"]}}
          for x, z in [(1, 1), (13, 1), (1, 13), (13, 13)]],
    ]})
w(f"{A}/blockstates/assembly_table.json", {"variants": {
    f"facing={f}": ({"model": f"{MOD}:block/assembly_table", "y": r} if r else {"model": f"{MOD}:block/assembly_table"})
    for f, r in YROT.items()}})
w(f"{A}/models/item/assembly_table.json", {"parent": f"{MOD}:block/assembly_table"})

# ---------- Tool rack ----------
w(f"{A}/models/block/tool_rack.json", {
    "parent": "minecraft:block/block",
    "textures": {"particle": f"{MOD}:block/rack_wood", "wood": f"{MOD}:block/rack_wood"},
    "elements": [
        {"from": [1, 0, 12], "to": [3, 16, 14],
         "faces": {d: {"uv": [0, 0, 4, 16], "texture": "#wood"} for d in
                   ["down", "up", "north", "south", "west", "east"]}},
        {"from": [13, 0, 12], "to": [15, 16, 14],
         "faces": {d: {"uv": [0, 0, 4, 16], "texture": "#wood"} for d in
                   ["down", "up", "north", "south", "west", "east"]}},
        {"from": [1, 11, 12], "to": [15, 13, 14],
         "faces": {d: {"uv": [0, 0, 16, 4], "texture": "#wood"} for d in
                   ["down", "up", "north", "south", "west", "east"]}},
        {"from": [1, 4, 12], "to": [15, 6, 14],
         "faces": {d: {"uv": [0, 0, 16, 4], "texture": "#wood"} for d in
                   ["down", "up", "north", "south", "west", "east"]}},
    ]})
w(f"{A}/blockstates/tool_rack.json", {"variants": {
    f"facing={f}": ({"model": f"{MOD}:block/tool_rack", "y": r} if r else {"model": f"{MOD}:block/tool_rack"})
    for f, r in YROT.items()}})
w(f"{A}/models/item/tool_rack.json", {"parent": f"{MOD}:block/tool_rack"})

# ---------- Metal shelf ----------
w(f"{A}/models/block/metal_shelf.json", {
    "parent": "minecraft:block/block",
    "textures": {"particle": f"{MOD}:block/shelf_metal",
                 "metal": f"{MOD}:block/shelf_metal", "wood": f"{MOD}:block/shelf_wood"},
    "elements": [
        {"from": [0, 0, 0], "to": [2, 16, 16],
         "faces": {d: {"uv": [0, 0, 16, 16], "texture": "#metal"} for d in
                   ["down", "up", "north", "south", "west", "east"]}},
        {"from": [14, 0, 0], "to": [16, 16, 16],
         "faces": {d: {"uv": [0, 0, 16, 16], "texture": "#metal"} for d in
                   ["down", "up", "north", "south", "west", "east"]}},
        *[{"from": [0, y, 0], "to": [16, y + 1, 16],
           "faces": {d: {"uv": [0, 0, 16, 16], "texture": "#wood"} for d in
                     ["down", "up", "north", "south", "west", "east"]}}
          for y in [3, 7, 11, 15]],
    ]})
w(f"{A}/blockstates/metal_shelf.json", {"variants": {
    f"facing={f}": ({"model": f"{MOD}:block/metal_shelf", "y": r} if r else {"model": f"{MOD}:block/metal_shelf"})
    for f, r in YROT.items()}})
w(f"{A}/models/item/metal_shelf.json", {"parent": f"{MOD}:block/metal_shelf"})

print("blockstates + block models OK")

"""Generate blockstates, block models, item models and particle descriptors."""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
A = ROOT / "src/main/resources/assets/true_metallurgy"
M = "true_metallurgy"

def w(path, obj):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(obj, indent=2) + "\n")

def face(tex):
    return {"uv": [0, 0, 16, 16], "texture": tex}

def faces_all(tex):
    return {d: face(tex) for d in ("down", "up", "north", "south", "west", "east")}

def el(box, tex):
    return {"from": box[:3], "to": box[3:], "faces": faces_all(tex)}

ROT = {"north": None, "east": 90, "south": 180, "west": 270}
def variant(model, facing):
    v = {"model": model}
    if ROT[facing]:
        v["y"] = ROT[facing]
    return v

# ---------------- blockstates ----------------
TIERS = ["primitive", "iron", "steel", "master"]
for tier in TIERS:
    variants = {}
    for heat in range(6):
        for f in ROT:
            variants[f"heat={heat},facing={f}"] = variant(f"{M}:block/forge_{tier}_{heat}", f)
    w(A / f"blockstates/forge_{tier}.json", {"variants": variants})

variants = {}
for stage in range(4):
    for f in ROT:
        variants[f"stage={stage},facing={f}"] = variant(f"{M}:block/bellows_{stage}", f)
w(A / "blockstates/bellows.json", {"variants": variants})

for tier in ["basic", "iron", "steel", "master"]:
    name = f"{tier}_anvil" if tier != "basic" else "basic_anvil"
    reg = {"basic": "basic_anvil", "iron": "iron_anvil", "steel": "steel_anvil", "master": "master_anvil"}[tier]
    w(A / f"blockstates/{reg}.json", {"variants": {f"facing={f}": variant(f"{M}:block/anvil_{tier}", f) for f in ROT}})

w(A / "blockstates/quenching_barrel.json", {"variants": {
    f"liquid={liq}": {"model": f"{M}:block/quenching_barrel_{liq}"} for liq in ("none", "water", "oil")}})

variants = {}
for spin in range(4):
    for f in ROT:
        variants[f"spin={spin},facing={f}"] = variant(f"{M}:block/grinding_wheel_{spin}", f)
w(A / "blockstates/grinding_wheel.json", {"variants": variants})

for simple in ("assembly_table", "tool_rack"):
    w(A / f"blockstates/{simple}.json", {"variants": {f"facing={f}": variant(f"{M}:block/{simple}", f) for f in ROT}})
w(A / "blockstates/steel_block.json", {"variants": {"": {"model": f"{M}:block/steel_block"}}})

# ---------------- block models ----------------
for tier in TIERS:
    side = f"{M}:block/forge_{tier}_side"
    top = f"{M}:block/forge_{tier}_top"
    for heat in range(6):
        w(A / f"models/block/forge_{tier}_{heat}.json", {
            "parent": "minecraft:block/cube",
            "textures": {"down": side, "up": top, "north": f"{M}:block/forge_front_{heat}",
                         "south": side, "east": side, "west": side, "particle": side}})

# Bellows stages: base + bag (varying height) + handle.
BAG = {0: (3, 8), 1: (3, 10), 2: (3, 5), 3: (3, 7)}
for stage in range(4):
    y0, y1 = BAG[stage]
    w(A / f"models/block/bellows_{stage}.json", {
        "parent": "minecraft:block/block",
        "textures": {"wood": f"{M}:block/bellows_wood", "leather": f"{M}:block/bellows_leather",
                     "particle": f"{M}:block/bellows_wood"},
        "elements": [
            el([1, 0, 1, 15, 3, 15], "#wood"),
            el([2, y0, 2, 14, y1, 14], "#leather"),
            el([6, y1, 6, 10, y1 + 4, 10], "#wood"),
        ]})

for tier in ("basic", "iron", "steel", "master"):
    w(A / f"models/block/anvil_{tier}.json", {
        "parent": "minecraft:block/block",
        "textures": {"metal": f"{M}:block/anvil_{tier}", "particle": f"{M}:block/anvil_{tier}"},
        "elements": [
            el([2, 0, 2, 14, 4, 14], "#metal"),
            el([5, 4, 5, 11, 9, 11], "#metal"),
            el([1, 9, 4, 15, 13, 12], "#metal"),
        ]})

for liq in ("none", "water", "oil"):
    side = f"{M}:block/barrel_side"
    w(A / f"models/block/quenching_barrel_{liq}.json", {
        "parent": "minecraft:block/cube",
        "textures": {"down": side, "up": f"{M}:block/barrel_top_{liq}", "north": side,
                     "south": side, "east": side, "west": side, "particle": side}})

SPIN_ANGLE = {0: 0, 1: 0, 2: 22.5, 3: 45}
for spin in range(4):
    wheel = el([6, 2, 7, 10, 14, 9], "#stone")
    if SPIN_ANGLE[spin]:
        wheel["rotation"] = {"angle": SPIN_ANGLE[spin], "axis": "x", "origin": [8, 8, 8]}
    w(A / f"models/block/grinding_wheel_{spin}.json", {
        "parent": "minecraft:block/block",
        "textures": {"wood": f"{M}:block/wheel_wood", "stone": f"{M}:block/wheel_stone",
                     "particle": f"{M}:block/wheel_stone"},
        "elements": [
            el([1, 0, 2, 3, 12, 4], "#wood"),
            el([13, 0, 2, 15, 12, 4], "#wood"),
            el([1, 0, 12, 3, 12, 14], "#wood"),
            el([13, 0, 12, 15, 12, 14], "#wood"),
            el([0, 7, 7, 16, 9, 9], "#wood"),
            wheel,
        ]})

top_el = {"from": [0, 12, 0, 16, 16, 16], "faces": {
    "down": face("#side"), "up": face("#top"), "north": face("#side"),
    "south": face("#side"), "west": face("#side"), "east": face("#side")}}
w(A / "models/block/assembly_table.json", {
    "parent": "minecraft:block/block",
    "textures": {"top": f"{M}:block/assembly_top", "side": f"{M}:block/assembly_side",
                 "particle": f"{M}:block/assembly_side"},
    "elements": [top_el,
        el([1, 0, 1, 4, 12, 4], "#side"), el([12, 0, 1, 15, 12, 4], "#side"),
        el([1, 0, 12, 4, 12, 15], "#side"), el([12, 0, 12, 15, 12, 15], "#side")]})

w(A / "models/block/tool_rack.json", {
    "parent": "minecraft:block/block",
    "textures": {"wood": f"{M}:block/rack_wood", "particle": f"{M}:block/rack_wood"},
    "elements": [
        el([2, 0, 6, 4, 16, 10], "#wood"),
        el([12, 0, 6, 14, 16, 10], "#wood"),
        el([2, 11, 7, 14, 14, 9], "#wood"),
    ]})

w(A / "models/block/steel_block.json", {
    "parent": "minecraft:block/cube_all",
    "textures": {"all": f"{M}:block/steel_block"}})

print("blockstates + block models done")

"""Generate loot tables, tags, advancements, worldgen, loot modifiers."""
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

BLOCKS = ["primitive_forge", "iron_forge", "steel_forge", "master_forge", "bellows",
          "basic_anvil", "iron_anvil", "steel_anvil", "master_anvil", "quenching_barrel",
          "grinding_wheel", "assembly_table", "steel_block", "tool_rack"]

# ---------------- loot tables ----------------
for b in BLOCKS:
    w(D / M / "loot_table/blocks" / f"{b}.json", {
        "type": "minecraft:block",
        "pools": [{"rolls": 1.0, "bonus_rolls": 0.0,
                   "entries": [{"type": "minecraft:item", "name": tm(b)}],
                   "conditions": [{"condition": "minecraft:survives_explosion"}]}]})

w(D / M / "loot_table/chests/abandoned_forge.json", {
    "type": "minecraft:chest",
    "pools": [
        {"rolls": {"min": 2.0, "max": 4.0}, "bonus_rolls": 0.0, "entries": [
            {"type": "minecraft:item", "name": tm("coke"), "weight": 10,
             "functions": [{"function": "minecraft:set_count", "count": {"min": 1.0, "max": 4.0}}]},
            {"type": "minecraft:item", "name": tm("quench_oil"), "weight": 8,
             "functions": [{"function": "minecraft:set_count", "count": {"min": 1.0, "max": 2.0}}]},
            {"type": "minecraft:item", "name": tm("crushed_iron_ore"), "weight": 10,
             "functions": [{"function": "minecraft:set_count", "count": {"min": 2.0, "max": 6.0}}]},
            {"type": "minecraft:item", "name": tm("iron_bloom"), "weight": 6,
             "functions": [{"function": "minecraft:set_count", "count": {"min": 1.0, "max": 2.0}}]},
            {"type": "minecraft:item", "name": tm("oak_handle"), "weight": 6},
            {"type": "minecraft:item", "name": mc("iron_ingot"), "weight": 8,
             "functions": [{"function": "minecraft:set_count", "count": {"min": 1.0, "max": 3.0}}]},
        ]},
        {"rolls": 1.0, "bonus_rolls": 0.0, "entries": [
            {"type": "minecraft:item", "name": tm("blacksmith_journal"), "weight": 3},
            {"type": "minecraft:item", "name": tm("iron_hammer"), "weight": 3,
             "functions": [{"function": "minecraft:set_damage", "damage": {"min": 0.2, "max": 0.7}}]},
            {"type": "minecraft:item", "name": tm("kings_edge_blueprint"), "weight": 1},
            {"type": "minecraft:empty", "weight": 6},
        ]},
    ]})

# ---------------- tags ----------------
def tag(path, values):
    w(D / path, {"replace": False, "values": values})

tag("minecraft/tags/block/mineable/pickaxe.json", [tm(b) for b in (
    "primitive_forge", "iron_forge", "steel_forge", "master_forge", "basic_anvil",
    "iron_anvil", "steel_anvil", "master_anvil", "grinding_wheel", "steel_block")])
tag("minecraft/tags/block/mineable/axe.json", [tm(b) for b in (
    "bellows", "quenching_barrel", "assembly_table", "tool_rack")])
tag("minecraft/tags/block/needs_stone_tool.json", [tm(b) for b in (
    "primitive_forge", "iron_forge", "basic_anvil", "iron_anvil", "grinding_wheel")])
tag("minecraft/tags/block/needs_iron_tool.json", [tm(b) for b in (
    "steel_forge", "master_forge", "steel_anvil", "master_anvil", "steel_block")])

tag(f"{M}/tags/item/hammers.json", [tm(f"{h}_hammer") for h in (
    "primitive", "copper", "iron", "steel", "hardened", "masterwork")])
tag(f"{M}/tags/item/handles.json", [tm(f"{h}_handle") for h in (
    "oak", "spruce", "birch", "dark_oak", "bamboo", "reinforced", "leather_wrapped", "bone")])
tag(f"{M}/tags/item/billets.json", [tm(f"{m}_billet") for m in ("copper", "iron", "steel", "hardened")])
tag(f"{M}/tags/item/blooms.json", [tm(f"{m}_bloom") for m in ("copper", "iron", "steel", "hardened")])
tag(f"{M}/tags/item/guards.json", [tm("iron_guard"), tm("steel_guard")])
tag(f"{M}/tags/item/pommels.json", [tm("iron_pommel"), tm("steel_pommel")])
tag(f"{M}/tags/item/components.json", [tm("sword_blade"), tm("axe_head"), tm("pickaxe_head"), tm("spear_head")])

FINISHED = [tm(f"{m}_{t}") for m in ("copper", "iron", "steel", "hardened") for t in ("sword", "axe", "pickaxe", "spear")]
tag("minecraft/tags/item/swords.json", [t for t in FINISHED if t.endswith("sword") or t.endswith("spear")])
tag("minecraft/tags/item/axes.json", [t for t in FINISHED if t.endswith("axe")])
tag("minecraft/tags/item/pickaxes.json", [t for t in FINISHED if t.endswith("pickaxe")])

# ---------------- advancements ----------------
def adv_display(icon, title, desc, background=None, frame="task"):
    d = {"icon": {"id": icon}, "title": {"translate": title}, "description": {"translate": desc},
         "frame": frame, "show_toast": True, "announce_to_chat": True, "hidden": False}
    if background:
        d["background"] = background
    return d

def has_items(*items):
    return {"trigger": "minecraft:inventory_changed",
            "conditions": {"items": [{"items": list(items)}]}}

w(D / M / "advancement/root.json", {
    "display": adv_display(tm("masterwork_hammer"), f"advancement.{M}.root", f"advancement.{M}.root.desc",
                           background="minecraft:textures/gui/advancements/backgrounds/stone.png"),
    "criteria": {"forge": has_items(tm("primitive_forge"))}})
w(D / M / "advancement/first_billet.json", {
    "parent": f"{M}:root",
    "display": adv_display(tm("iron_billet"), f"advancement.{M}.first_billet", f"advancement.{M}.first_billet.desc"),
    "criteria": {"billet": has_items(*[tm(f"{m}_billet") for m in ("copper", "iron", "steel", "hardened")])}})
w(D / M / "advancement/masterwork.json", {
    "parent": f"{M}:first_billet",
    "display": adv_display(tm("steel_sword"), f"advancement.{M}.masterwork", f"advancement.{M}.masterwork.desc",
                           frame="goal"),
    "criteria": {"forged": has_items(*FINISHED)}})
w(D / M / "advancement/legendary.json", {
    "parent": f"{M}:masterwork",
    "display": adv_display(tm("kings_edge_blueprint"), f"advancement.{M}.legendary", f"advancement.{M}.legendary.desc",
                           frame="challenge"),
    "criteria": {"blueprint": has_items(tm("kings_edge_blueprint")), "forged": has_items(*FINISHED)},
    "requirements": [["blueprint", "forged"]]})

# ---------------- worldgen ----------------
w(D / M / "worldgen/configured_feature/abandoned_forge.json",
  {"type": tm("abandoned_forge"), "config": {}})
w(D / M / "worldgen/placed_feature/abandoned_forge.json", {
    "feature": tm("abandoned_forge"),
    "placement": [
        {"type": "minecraft:rarity_filter", "chance": 48},
        {"type": "minecraft:in_square"},
        {"type": "minecraft:heightmap", "heightmap": "WORLD_SURFACE_WG"},
        {"type": "minecraft:biome"}]})
w(D / M / "neoforge/biome_modifier/add_abandoned_forge.json", {
    "type": "neoforge:add_features", "biomes": "#minecraft:is_overworld",
    "features": tm("abandoned_forge"), "step": "surface_structures"})

# ---------------- global loot ----------------
w(D / "neoforge/loot_modifiers/global_loot_modifiers.json",
  {"replace": False, "entries": [tm("workshop_cache")]})
w(D / M / "neoforge/loot_modifier/workshop_cache.json", {
    "type": tm("workshop_cache"),
    "conditions": [{"condition": "minecraft:any_of", "terms": [
        {"condition": "minecraft:loot_table_id", "loot_table_id": "minecraft:chests/village_weaponsmith"},
        {"condition": "minecraft:loot_table_id", "loot_table_id": "minecraft:chests/village_toolsmith"},
        {"condition": "minecraft:loot_table_id", "loot_table_id": "minecraft:chests/village_armorer"}]}]})

print("data part 2 done")

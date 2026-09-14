#!/usr/bin/env python3
"""Loot tables, tags, advancements, worldgen JSON."""
import json, os

M = "masterblacksmith"
D = f"src/main/resources/data/{M}"

def w(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(obj, f, indent=2)
        f.write("\n")

# ================= LOOT =================
SELF_BLOCKS = ["refractory_brick_block", "forge_hearth_primitive", "forge_hearth_iron",
    "forge_hearth_steel", "forge_hearth_master", "bellows", "anvil_basic", "anvil_iron",
    "anvil_steel", "anvil_master", "quenching_barrel", "grinding_wheel", "assembly_table",
    "tool_rack", "metal_shelf", "tin_block", "raw_tin_block", "bronze_block", "steel_block",
    "starfall_block"]
for b in SELF_BLOCKS:
    w(f"{D}/loot_tables/blocks/{b}.json", {"type": "minecraft:block",
        "pools": [{"rolls": 1, "bonus_rolls": 0,
                   "entries": [{"type": "minecraft:item", "name": f"{M}:{b}"}],
                   "conditions": [{"condition": "minecraft:survives_explosion"}]}]})

def ore_loot(ore, drop, lo, hi):
    w(f"{D}/loot_tables/blocks/{ore}.json", {"type": "minecraft:block",
        "pools": [{"rolls": 1, "bonus_rolls": 0,
                   "entries": [{"type": "minecraft:alternatives", "children": [
                       {"type": "minecraft:item", "name": f"{M}:{ore}",
                        "conditions": [{"condition": "minecraft:match_tool",
                                        "predicate": {"enchantments": [{"enchantment": "minecraft:silk_touch", "levels": 1}]}}]},
                       {"type": "minecraft:item", "name": f"{M}:{drop}",
                        "functions": [
                            {"function": "minecraft:set_count", "count": {"min": lo, "max": hi}},
                            {"function": "minecraft:apply_bonus", "enchantment": "minecraft:fortune",
                             "formula": "minecraft:ore_drops"}]}]}]}]})

ore_loot("tin_ore", "raw_tin", 1, 3)
ore_loot("deepslate_tin_ore", "raw_tin", 1, 3)
ore_loot("starfall_ore", "starfall_shard", 1, 2)

w(f"{D}/loot_tables/chests/ruined_smithy.json", {"type": "minecraft:chest",
    "pools": [
        {"rolls": {"min": 2, "max": 4}, "bonus_rolls": 0, "entries": [
            {"type": "minecraft:item", "name": f"{M}:coke", "weight": 10,
             "functions": [{"function": "minecraft:set_count", "count": {"min": 2, "max": 6}}]},
            {"type": "minecraft:item", "name": f"{M}:steel_ingot", "weight": 6,
             "functions": [{"function": "minecraft:set_count", "count": {"min": 1, "max": 3}}]},
            {"type": "minecraft:item", "name": f"{M}:refractory_brick", "weight": 8,
             "functions": [{"function": "minecraft:set_count", "count": {"min": 2, "max": 5}}]},
            {"type": "minecraft:item", "name": f"{M}:leather_strip", "weight": 8,
             "functions": [{"function": "minecraft:set_count", "count": {"min": 1, "max": 4}}]},
            {"type": "minecraft:item", "name": f"{M}:hammer_iron", "weight": 3},
            {"type": "minecraft:item", "name": f"{M}:oil_bucket", "weight": 3}]},
        {"rolls": 1, "bonus_rolls": 0, "entries": [
            {"type": "minecraft:empty", "weight": 7},
            {"type": "minecraft:item", "name": f"{M}:blueprint_oathkeeper", "weight": 1},
            {"type": "minecraft:item", "name": f"{M}:blueprint_stonesplitter", "weight": 1},
            {"type": "minecraft:item", "name": f"{M}:blueprint_aegis", "weight": 1}]},
    ]})

w(f"{D}/loot_tables/entities/travelling_blacksmith.json", {"type": "minecraft:entity",
    "pools": [{"rolls": 1, "bonus_rolls": 0,
               "entries": [{"type": "minecraft:item", "name": "minecraft:leather",
                            "functions": [{"function": "minecraft:set_count", "count": {"min": 0, "max": 2}}]},
                          {"type": "minecraft:item", "name": "minecraft:emerald",
                           "functions": [{"function": "minecraft:set_count", "count": {"min": 0, "max": 2}}]}]}]})

# ================= TAGS =================
def tag(path, values):
    w(f"src/main/resources/data/{path}", {"replace": False, "values": values})

tag("minecraft/tags/blocks/mineable/pickaxe.json", [
    f"{M}:refractory_brick_block", f"{M}:forge_hearth_primitive", f"{M}:forge_hearth_iron",
    f"{M}:forge_hearth_steel", f"{M}:forge_hearth_master", f"{M}:anvil_basic", f"{M}:anvil_iron",
    f"{M}:anvil_steel", f"{M}:anvil_master", f"{M}:grinding_wheel", f"{M}:metal_shelf",
    f"{M}:tin_ore", f"{M}:deepslate_tin_ore", f"{M}:starfall_ore", f"{M}:tin_block",
    f"{M}:raw_tin_block", f"{M}:bronze_block", f"{M}:steel_block", f"{M}:starfall_block"])
tag("minecraft/tags/blocks/mineable/axe.json",
    [f"{M}:bellows", f"{M}:quenching_barrel", f"{M}:assembly_table", f"{M}:tool_rack"])
tag("minecraft/tags/blocks/needs_iron_tool.json",
    [f"{M}:anvil_steel", f"{M}:anvil_master", f"{M}:starfall_ore", f"{M}:starfall_block",
     f"{M}:forge_hearth_steel", f"{M}:forge_hearth_master"])
tag("minecraft/tags/blocks/needs_stone_tool.json",
    [f"{M}:tin_ore", f"{M}:deepslate_tin_ore", f"{M}:anvil_iron", f"{M}:forge_hearth_iron",
     f"{M}:grinding_wheel", f"{M}:metal_shelf"])
tag("forge/tags/items/ingots/tin.json", [f"{M}:tin_ingot"])
tag("forge/tags/items/ingots/bronze.json", [f"{M}:bronze_ingot"])
tag("forge/tags/items/ingots/steel.json", [f"{M}:steel_ingot"])
tag("forge/tags/items/ores/tin.json", [f"{M}:tin_ore", f"{M}:deepslate_tin_ore", f"{M}:raw_tin"])
tag("forge/tags/items/storage_blocks/tin.json", [f"{M}:tin_block"])
tag("forge/tags/items/storage_blocks/bronze.json", [f"{M}:bronze_block"])
tag("forge/tags/items/storage_blocks/steel.json", [f"{M}:steel_block"])
tag("forge/tags/items/storage_blocks/raw_tin.json", [f"{M}:raw_tin_block"])

# ================= ADVANCEMENTS =================
def adv(name, parent, icon, title, desc, frame, trigger):
    obj = {"display": {"icon": {"item": icon}, "title": title, "description": desc,
                       "frame": frame, "show_toast": True, "announce_to_chat": True,
                       "hidden": False},
           "criteria": {"got": trigger}, "requirements": [["got"]]}
    if parent:
        obj["parent"] = parent
    else:
        obj["display"]["background"] = "minecraft:textures/block/deepslate_bricks.png"
    w(f"{D}/advancements/{name}.json", obj)

inv = lambda items, nbt=None: {"trigger": "minecraft:inventory_changed",
    "conditions": {"items": [{"items": items, **({"nbt": nbt} if nbt else {})}]}}
adv("root", None, f"{M}:tongs", {"translate": "adv.masterblacksmith.root"},
    {"translate": "adv.masterblacksmith.root.desc"}, "task", inv([f"{M}:tongs"]))
adv("first_bloom", f"{M}:root", f"{M}:bloom_iron",
    {"translate": "adv.masterblacksmith.first_bloom"}, {"translate": "adv.masterblacksmith.first_bloom.desc"},
    "task", inv([f"{M}:bloom_iron", f"{M}:bloom_copper", f"{M}:bloom_steel", f"{M}:bloom_tin"]))
adv("first_billet", f"{M}:first_bloom", f"{M}:billet_iron",
    {"translate": "adv.masterblacksmith.first_billet"}, {"translate": "adv.masterblacksmith.first_billet.desc"},
    "task", inv([f"{M}:billet_iron", f"{M}:billet_copper", f"{M}:billet_steel", f"{M}:billet_tin"]))
adv("quenched", f"{M}:first_billet", f"{M}:sword_blade",
    {"translate": "adv.masterblacksmith.quenched"}, {"translate": "adv.masterblacksmith.quenched.desc"},
    "goal", inv([f"{M}:sword_blade", f"{M}:axe_head", f"{M}:pickaxe_head", f"{M}:spear_head", f"{M}:armor_plate"],
                "{MBSForge:{Quenched:1b}}"))
adv("masterwork", f"{M}:quenched", f"{M}:forged_sword",
    {"translate": "adv.masterblacksmith.masterwork"}, {"translate": "adv.masterblacksmith.masterwork.desc"},
    "goal", inv([f"{M}:forged_sword", f"{M}:forged_axe", f"{M}:forged_pickaxe", f"{M}:forged_spear"],
                '{MBSIdentity:{Tier:"masterwork"}}'))
adv("legendary", f"{M}:masterwork", f"{M}:blueprint_kings_edge",
    {"translate": "adv.masterblacksmith.legendary"}, {"translate": "adv.masterblacksmith.legendary.desc"},
    "challenge", inv([f"{M}:forged_sword", f"{M}:forged_axe", f"{M}:forged_pickaxe", f"{M}:forged_spear",
                      f"{M}:forged_helmet", f"{M}:forged_chestplate", f"{M}:forged_leggings", f"{M}:forged_boots"],
                     '{MBSIdentity:{Tier:"legendary"}}'))

# advancement lang keys
import json as _j
adv_en = {
  "adv.masterblacksmith.root": "Soul of Steel",
  "adv.masterblacksmith.root.desc": "Take up the tongs. You are a blacksmith now.",
  "adv.masterblacksmith.first_bloom": "First Bloom",
  "adv.masterblacksmith.first_bloom.desc": "Smelt processed ore into a metal bloom.",
  "adv.masterblacksmith.first_billet": "True Billet",
  "adv.masterblacksmith.first_billet.desc": "Consolidate a hot bloom into a billet on the anvil.",
  "adv.masterblacksmith.quenched": "Hiss of Truth",
  "adv.masterblacksmith.quenched.desc": "Quench a finished, glowing component.",
  "adv.masterblacksmith.masterwork": "Masterwork",
  "adv.masterblacksmith.masterwork.desc": "Assemble a weapon of 70+ craftsmanship.",
  "adv.masterblacksmith.legendary": "The Best Blade You Ever Forged",
  "adv.masterblacksmith.legendary.desc": "Forge a one-of-a-kind LEGENDARY piece."
}
adv_vi = {
  "adv.masterblacksmith.root": "Hồn Của Thép",
  "adv.masterblacksmith.root.desc": "Cầm kẹp lên. Từ nay bạn là thợ rèn.",
  "adv.masterblacksmith.first_bloom": "Bông Kim Loại Đầu Tiên",
  "adv.masterblacksmith.first_bloom.desc": "Nung quặng đã xử lý thành bông kim loại.",
  "adv.masterblacksmith.first_billet": "Phôi Thật Sự",
  "adv.masterblacksmith.first_billet.desc": "Nén bông kim loại nóng thành phôi trên đe.",
  "adv.masterblacksmith.quenched": "Tiếng Xèo Của Sự Thật",
  "adv.masterblacksmith.quenched.desc": "Tôi một bộ phận đã rèn còn đỏ rực.",
  "adv.masterblacksmith.masterwork": "Kiệt Tác",
  "adv.masterblacksmith.masterwork.desc": "Lắp ráp vũ khí đạt tay nghề 70+.",
  "adv.masterblacksmith.legendary": "Lưỡi Kiếm Tuyệt Nhất Đời Bạn",
  "adv.masterblacksmith.legendary.desc": "Rèn một kiệt tác HUYỀN THOẠI độc nhất."
}
for path, extra in [("src/main/resources/assets/masterblacksmith/lang/en_us.json", adv_en),
                    ("src/main/resources/assets/masterblacksmith/lang/vi_vn.json", adv_vi)]:
    d = _j.load(open(path, encoding="utf-8"))
    d.update(extra)
    _j.dump(d, open(path, "w", encoding="utf-8"), ensure_ascii=False, indent=2)

# ================= WORLDGEN =================
w(f"{D}/worldgen/configured_feature/tin_ore.json", {"type": "minecraft:ore",
    "config": {"size": 9, "discard_chance_on_air_exposure": 0.0, "targets": [
        {"state": {"Name": f"{M}:tin_ore"},
         "target": {"predicate_type": "minecraft:tag_match", "tag": "minecraft:stone_ore_replaceables"}},
        {"state": {"Name": f"{M}:deepslate_tin_ore"},
         "target": {"predicate_type": "minecraft:tag_match", "tag": "minecraft:deepslate_ore_replaceables"}}]}})
w(f"{D}/worldgen/placed_feature/tin_ore.json", {"feature": f"{M}:tin_ore",
    "placement": [{"type": "minecraft:count", "count": 12}, {"type": "minecraft:in_square"},
        {"type": "minecraft:height_range", "height": {"type": "minecraft:uniform",
            "max_inclusive": {"absolute": 64}, "min_inclusive": {"absolute": -32}}},
        {"type": "minecraft:biome"}]})
w(f"{D}/worldgen/configured_feature/starfall_ore.json", {"type": "minecraft:ore",
    "config": {"size": 4, "discard_chance_on_air_exposure": 0.5, "targets": [
        {"state": {"Name": f"{M}:starfall_ore"},
         "target": {"predicate_type": "minecraft:tag_match", "tag": "minecraft:stone_ore_replaceables"}}]}})
w(f"{D}/worldgen/placed_feature/starfall_ore.json", {"feature": f"{M}:starfall_ore",
    "placement": [{"type": "minecraft:count", "count": 2}, {"type": "minecraft:in_square"},
        {"type": "minecraft:height_range", "height": {"type": "minecraft:trapezoid",
            "max_inclusive": {"absolute": 16}, "min_inclusive": {"absolute": -64}}},
        {"type": "minecraft:biome"}]})
w(f"{D}/worldgen/configured_feature/ruined_smithy.json",
  {"type": f"{M}:ruined_smithy", "config": {}})
w(f"{D}/worldgen/placed_feature/ruined_smithy.json", {"feature": f"{M}:ruined_smithy",
    "placement": [{"type": "minecraft:rarity_filter", "chance": 48}, {"type": "minecraft:in_square"},
        {"type": "minecraft:heightmap", "heightmap": "WORLD_SURFACE_WG"}, {"type": "minecraft:biome"}]})

w("src/main/resources/data/forge/biome_modifier/add_tin_ore.json",
  {"type": "forge:add_features", "biomes": "#minecraft:is_overworld",
   "features": f"{M}:tin_ore", "step": "underground_ores"})
w("src/main/resources/data/forge/biome_modifier/add_starfall_ore.json",
  {"type": "forge:add_features", "biomes": "#minecraft:is_overworld",
   "features": f"{M}:starfall_ore", "step": "underground_ores"})
w("src/main/resources/data/forge/biome_modifier/add_ruined_smithy.json",
  {"type": "forge:add_features", "biomes": "#minecraft:is_overworld",
   "features": f"{M}:ruined_smithy", "step": "surface_structures"})
w("src/main/resources/data/forge/biome_modifier/add_blacksmith_spawn.json",
  {"type": "forge:add_spawns", "biomes": "#minecraft:is_overworld",
   "spawners": {"type": f"{M}:travelling_blacksmith", "minCount": 1, "maxCount": 1, "weight": 4}})

print("data2 OK")

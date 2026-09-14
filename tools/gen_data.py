#!/usr/bin/env python3
"""Generate recipes, loot, tags, advancements and worldgen JSON."""
import json, os

M = "masterblacksmith"
D = f"src/main/resources/data/{M}"
METALS = ["copper", "tin", "bronze", "iron", "steel", "hardened_steel", "damascus_steel", "starfall_steel"]

def w(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(obj, f, indent=2)
        f.write("\n")

def shaped(name, result, count, pattern, keys, sub="crafting"):
    w(f"{D}/recipes/{name}.json", {"type": "minecraft:crafting_shaped",
        "pattern": pattern, "key": keys,
        "result": {"item": result, "count": count}})

def shapeless(name, result, count, ingredients):
    w(f"{D}/recipes/{name}.json", {"type": "minecraft:crafting_shapeless",
        "ingredients": ingredients,
        "result": {"item": result, "count": count}})

def smelt(name, kind, ingredient, result, xp, time):
    w(f"{D}/recipes/{name}.json", {"type": kind, "ingredient": ingredient,
        "result": result, "experience": xp, "cookingtime": time})

I = lambda x: {"item": x}
T = lambda x: {"tag": x}

# ================= WORKSHOP =================
shaped("refractory_brick_block", f"{M}:refractory_brick_block", 1,
       ["BB", "BB"], {"B": I(f"{M}:refractory_brick")})
shaped("forge_hearth_primitive", f"{M}:forge_hearth_primitive", 1,
       ["CBC", "BFB", "CBC"], {"C": I("minecraft:cobblestone"),
        "B": I(f"{M}:refractory_brick"), "F": I("minecraft:furnace")})
shaped("forge_hearth_iron", f"{M}:forge_hearth_iron", 1,
       ["IBI", "BFB", "IBI"], {"I": I("minecraft:iron_ingot"),
        "B": I(f"{M}:refractory_brick_block"), "F": I(f"{M}:forge_hearth_primitive")})
shaped("forge_hearth_steel", f"{M}:forge_hearth_steel", 1,
       ["IBI", "BFB", "IBI"], {"I": I(f"{M}:steel_ingot"),
        "B": I(f"{M}:refractory_brick_block"), "F": I(f"{M}:forge_hearth_iron")})
shaped("forge_hearth_master", f"{M}:forge_hearth_master", 1,
       ["SHS", "HFH", "SHS"],
       {"S": I(f"{M}:starfall_shard"), "H": I(f"{M}:hardened_steel_ingot"),
        "F": I(f"{M}:forge_hearth_steel")})
shaped("bellows", f"{M}:bellows", 1,
       ["PPP", "LLL", "PPP"], {"P": T("minecraft:planks"), "L": I("minecraft:leather")})
shaped("anvil_basic", f"{M}:anvil_basic", 1,
       ["BBB", " i ", "iii"], {"B": I("minecraft:iron_block"), "i": I("minecraft:iron_ingot")})
shaped("anvil_iron", f"{M}:anvil_iron", 1,
       ["BiB", "iAi", "BiB"], {"B": I("minecraft:iron_block"),
        "i": I("minecraft:iron_ingot"), "A": I(f"{M}:anvil_basic")})
shaped("anvil_steel", f"{M}:anvil_steel", 1,
       ["SiS", "iAi", "SiS"], {"S": I(f"{M}:steel_ingot"),
        "i": I("minecraft:iron_ingot"), "A": I(f"{M}:anvil_iron")})
shaped("anvil_master", f"{M}:anvil_master", 1,
       ["SHS", "HAH", "SHS"], {"S": I(f"{M}:starfall_shard"),
        "H": I(f"{M}:hardened_steel_ingot"), "A": I(f"{M}:anvil_steel")})
shaped("quenching_barrel", f"{M}:quenching_barrel", 1,
       ["PiP", "P P", "PPP"], {"P": T("minecraft:planks"), "i": I("minecraft:iron_ingot")})
shaped("grinding_wheel", f"{M}:grinding_wheel", 1,
       ["SSS", "SiS", "PPP"], {"S": I("minecraft:stone"),
        "i": I("minecraft:iron_ingot"), "P": T("minecraft:planks")})
shaped("assembly_table", f"{M}:assembly_table", 1,
       ["PPP", "ICI", "I I"], {"P": T("minecraft:planks"),
        "C": I("minecraft:crafting_table"), "I": I("minecraft:iron_ingot")})
shaped("tool_rack", f"{M}:tool_rack", 1,
       ["S S", "SSS", "S S"], {"S": I("minecraft:stick")})
shaped("metal_shelf", f"{M}:metal_shelf", 1,
       ["I I", "PPP", "I I"], {"I": I("minecraft:iron_ingot"), "P": T("minecraft:planks")})

# ================= HAMMERS & TONGS =================
def hammer(name, head):
    shaped(name, f"{M}:{name}", 1, ["HH ", "HSH", " S "],
           {"H": I(head), "S": I("minecraft:stick")})
hammer("hammer_copper", "minecraft:copper_ingot")
hammer("hammer_iron", "minecraft:iron_ingot")
hammer("hammer_steel", f"{M}:steel_ingot")
hammer("hammer_hardened", f"{M}:hardened_steel_ingot")
shaped("hammer_masterwork", f"{M}:hammer_masterwork", 1, ["DD ", "DBD", " B "],
       {"D": I(f"{M}:damascus_steel_ingot"), "B": I(f"{M}:handle_bone")})
shaped("tongs", f"{M}:tongs", 1, ["I I", " R ", "I I"],
       {"I": I("minecraft:iron_ingot"), "R": I(f"{M}:metal_rivet")})

# ================= STORAGE BLOCKS =================
for block, ing in [("tin_block", "tin_ingot"), ("bronze_block", "bronze_ingot"),
                   ("steel_block", "steel_ingot"), ("starfall_block", "starfall_steel_ingot")]:
    shaped(block, f"{M}:{block}", 1, ["III", "III", "III"], {"I": I(f"{M}:{ing}")})
    shapeless(f"{ing}_from_block", f"{M}:{ing}", 9, [I(f"{M}:{block}")])
shaped("raw_tin_block", f"{M}:raw_tin_block", 1, ["III", "III", "III"], {"I": I(f"{M}:raw_tin")})

# ================= PARTS =================
shaped("guard_iron", f"{M}:guard_iron", 1, ["III", " I "], {"I": I("minecraft:iron_ingot")})
shaped("guard_steel", f"{M}:guard_steel", 1, ["III", " I "], {"I": I(f"{M}:steel_ingot")})
shaped("guard_brass", f"{M}:guard_brass", 1, ["III", " I "], {"I": I("minecraft:copper_ingot")})
shaped("pommel_iron", f"{M}:pommel_iron", 1, [" I ", "IRI", " I "],
       {"I": I("minecraft:iron_ingot"), "R": I(f"{M}:metal_rivet")})
shaped("pommel_steel", f"{M}:pommel_steel", 1, [" I ", "IRI", " I "],
       {"I": I(f"{M}:steel_ingot"), "R": I(f"{M}:metal_rivet")})
shaped("pommel_brass", f"{M}:pommel_brass", 1, [" I ", "IRI", " I "],
       {"I": I("minecraft:copper_ingot"), "R": I(f"{M}:metal_rivet")})
shapeless("grip_leather", f"{M}:grip_leather", 1, [I("minecraft:leather"), I("minecraft:leather"), I("minecraft:stick")])
shapeless("grip_bone", f"{M}:grip_bone", 1, [I("minecraft:bone"), I("minecraft:bone"), I(f"{M}:leather_strip")])
shapeless("grip_exotic", f"{M}:grip_exotic", 1, [I(f"{M}:leather_strip"), I("minecraft:amethyst_shard"), I("minecraft:gold_ingot")])

# ================= HANDLES =================
shaped("handle_oak", f"{M}:handle_oak", 2, ["P", "P"], {"P": I("minecraft:oak_planks")})
shaped("handle_spruce", f"{M}:handle_spruce", 2, ["P", "P"], {"P": I("minecraft:spruce_planks")})
shaped("handle_birch", f"{M}:handle_birch", 2, ["P", "P"], {"P": I("minecraft:birch_planks")})
shaped("handle_dark_oak", f"{M}:handle_dark_oak", 2, ["P", "P"], {"P": I("minecraft:dark_oak_planks")})
shaped("handle_bamboo", f"{M}:handle_bamboo", 2, ["B", "B", "B"], {"B": I("minecraft:bamboo")})
shaped("handle_reinforced", f"{M}:handle_reinforced", 1, [" I ", "PSP", " I "],
       {"I": I("minecraft:iron_ingot"), "P": T("minecraft:planks"), "S": I("minecraft:stick")})
shapeless("handle_leather_wrapped", f"{M}:handle_leather_wrapped", 1,
          [I(f"{M}:handle_oak"), I(f"{M}:leather_strip"), I(f"{M}:leather_strip")])
shaped("handle_bone", f"{M}:handle_bone", 1, ["B", "B", "B"], {"B": I("minecraft:bone")})

# ================= SMALL GOODS =================
shapeless("metal_rivet", f"{M}:metal_rivet", 4, [I("minecraft:iron_ingot")])
shapeless("leather_strip", f"{M}:leather_strip", 4, [I("minecraft:leather")])
shaped("refractory_brick", f"{M}:refractory_brick", 2, ["CC", "SS"],
       {"C": I("minecraft:clay_ball"), "S": I("minecraft:sand")})
shapeless("refractory_brick_from_slag", f"{M}:refractory_brick", 1,
          [I(f"{M}:slag"), I("minecraft:sand")])
shapeless("blacksmith_journal", f"{M}:blacksmith_journal", 1,
          [I("minecraft:book"), I("minecraft:leather"), I("minecraft:iron_nugget")])

# ================= QUENCH MEDIA =================
shapeless("oil_bucket", f"{M}:oil_bucket", 1,
          [I("minecraft:bucket"), I("minecraft:kelp"), I("minecraft:kelp"), I("minecraft:kelp"), I("minecraft:kelp")])
shapeless("salt_water_bucket", f"{M}:salt_water_bucket", 1,
          [I("minecraft:water_bucket"), I("minecraft:sand"), I("minecraft:sand")])
shapeless("herbal_oil_bucket", f"{M}:herbal_oil_bucket", 1,
          [I(f"{M}:oil_bucket"), I("minecraft:poppy"), I("minecraft:dandelion"), I("minecraft:allium")])
shapeless("mineral_oil_bucket", f"{M}:mineral_oil_bucket", 1,
          [I(f"{M}:oil_bucket"), I("minecraft:coal_block"), I("minecraft:redstone")])
shapeless("alchemical_oil_bucket", f"{M}:alchemical_oil_bucket", 1,
          [I(f"{M}:herbal_oil_bucket"), I("minecraft:blaze_powder"), I("minecraft:ghast_tear")])
shapeless("blood_quench_vial", f"{M}:blood_quench_vial", 1,
          [I("minecraft:glass_bottle"), I("minecraft:rotten_flesh"), I("minecraft:rotten_flesh"), I("minecraft:redstone")])
shapeless("starfall_quench_vial", f"{M}:starfall_quench_vial", 1,
          [I("minecraft:glass_bottle"), I(f"{M}:starfall_shard"), I("minecraft:dragon_breath")])

# ================= ORE PROCESSING / ALLOYS =================
shapeless("processed_copper", f"{M}:processed_copper", 1, [I("minecraft:raw_copper"), I("minecraft:sand")])
shapeless("processed_tin", f"{M}:processed_tin", 1, [I(f"{M}:raw_tin"), I("minecraft:sand")])
shapeless("processed_iron", f"{M}:processed_iron", 1, [I("minecraft:raw_iron"), I("minecraft:sand")])
shapeless("processed_bronze", f"{M}:processed_bronze", 4,
          [I(f"{M}:processed_copper"), I(f"{M}:processed_copper"),
           I(f"{M}:processed_copper"), I(f"{M}:processed_tin")])
shapeless("processed_steel", f"{M}:processed_steel", 1, [I(f"{M}:processed_iron"), I(f"{M}:coke")])
shapeless("processed_hardened_steel", f"{M}:processed_hardened_steel", 2,
          [I(f"{M}:processed_steel"), I(f"{M}:processed_steel"), I(f"{M}:coke")])
shapeless("processed_damascus_steel", f"{M}:processed_damascus_steel", 2,
          [I(f"{M}:processed_steel"), I(f"{M}:processed_hardened_steel")])
shapeless("processed_starfall_steel", f"{M}:processed_starfall_steel", 1,
          [I(f"{M}:processed_steel"), I(f"{M}:starfall_shard"), I(f"{M}:starfall_shard")])

# ================= FURNACE WORK =================
for m in METALS:
    smelt(f"bloom_{m}", "minecraft:smelting", I(f"{M}:processed_{m}"), f"{M}:bloom_{m}", 0.7, 200)
BLOOM_TO_INGOT = {"copper": "minecraft:copper_ingot", "iron": "minecraft:iron_ingot",
    "tin": f"{M}:tin_ingot", "bronze": f"{M}:bronze_ingot", "steel": f"{M}:steel_ingot",
    "hardened_steel": f"{M}:hardened_steel_ingot", "damascus_steel": f"{M}:damascus_steel_ingot",
    "starfall_steel": f"{M}:starfall_steel_ingot"}
for m, ing in BLOOM_TO_INGOT.items():
    smelt(f"ingot_{m}", "minecraft:blasting", I(f"{M}:bloom_{m}"), ing, 1.0, 150)
smelt("tin_ingot", "minecraft:smelting", I(f"{M}:raw_tin"), f"{M}:tin_ingot", 0.7, 200)
smelt("tin_ingot_blast", "minecraft:blasting", I(f"{M}:raw_tin"), f"{M}:tin_ingot", 0.7, 100)
smelt("starfall_shard", "minecraft:smelting", I(f"{M}:starfall_ore"), f"{M}:starfall_shard", 1.0, 200)
smelt("starfall_shard_blast", "minecraft:blasting", I(f"{M}:starfall_ore"), f"{M}:starfall_shard", 1.0, 100)
smelt("coke", "minecraft:blasting", I("minecraft:coal_block"), f"{M}:coke", 0.5, 300)

print("recipes OK")

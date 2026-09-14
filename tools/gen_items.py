"""Generate item models + particle descriptors."""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
A = ROOT / "src/main/resources/assets/true_metallurgy"
M = "true_metallurgy"

def w(path, obj):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(obj, indent=2) + "\n")

GEN = "minecraft:item/generated"
HAND = "minecraft:item/handheld"

def flat(name):
    w(A / f"models/item/{name}.json", {"parent": GEN, "textures": {"layer0": f"{M}:item/{name}"}})

def handheld(name):
    w(A / f"models/item/{name}.json", {"parent": HAND, "textures": {"layer0": f"{M}:item/{name}"}})

def block_item(name, model):
    w(A / f"models/item/{name}.json", {"parent": f"{M}:block/{model}"})

# Block items
block_item("primitive_forge", "forge_primitive_0")
block_item("iron_forge", "forge_iron_0")
block_item("steel_forge", "forge_steel_0")
block_item("master_forge", "forge_master_0")
block_item("bellows", "bellows_0")
block_item("basic_anvil", "anvil_basic")
block_item("iron_anvil", "anvil_iron")
block_item("steel_anvil", "anvil_steel")
block_item("master_anvil", "anvil_master")
block_item("quenching_barrel", "quenching_barrel_none")
block_item("grinding_wheel", "grinding_wheel_0")
block_item("assembly_table", "assembly_table")
block_item("tool_rack", "tool_rack")
block_item("steel_block", "steel_block")

handheld("blacksmith_tongs")
for h in ("primitive", "copper", "iron", "steel", "hardened", "masterwork"):
    handheld(f"{h}_hammer")

for name in ("crushed_iron_ore", "crushed_copper_ore", "iron_bloom", "copper_bloom",
             "steel_bloom", "hardened_bloom", "iron_guard", "steel_guard", "iron_pommel",
             "steel_pommel", "oak_handle", "spruce_handle", "birch_handle", "dark_oak_handle",
             "bamboo_handle", "reinforced_handle", "leather_wrapped_handle", "bone_handle",
             "long_shaft", "binding", "coke", "quench_oil", "blacksmith_journal", "kings_edge_blueprint"):
    flat(name)

# Billets with heat overrides.
for mat in ("copper", "iron", "steel", "hardened"):
    name = f"{mat}_billet"
    overrides = []
    for i in range(1, 5):
        w(A / f"models/item/{name}_{i}.json",
          {"parent": GEN, "textures": {"layer0": f"{M}:item/{name}_{i}"}})
        overrides.append({"predicate": {f"{M}:heat": 0.25 * i}, "model": f"{M}:item/{name}_{i}"})
    w(A / f"models/item/{name}.json",
      {"parent": GEN, "textures": {"layer0": f"{M}:item/{name}_0"}, "overrides": overrides})

for comp in ("sword_blade", "axe_head", "pickaxe_head", "spear_head"):
    flat(comp)

for mat in ("copper", "iron", "steel", "hardened"):
    for tool in ("sword", "axe", "pickaxe", "spear"):
        handheld(f"{mat}_{tool}")

w(A / "models/item/blacksmith_spawn_egg.json", {"parent": "minecraft:item/template_spawn_egg"})

for p in ("spark", "ember", "steam", "metal_dust"):
    w(A / f"particles/{p}.json", {"textures": [f"{M}:{p}"]})

print("item models + particles done")

"""Generate sounds.json with two variants per event."""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
M = "true_metallurgy"

EVENTS = {
    "forge_crackle": "subtitle.true_metallurgy.forge_crackle",
    "forge_roar": "subtitle.true_metallurgy.forge_crackle",
    "bellows_whoosh": "subtitle.true_metallurgy.bellows_whoosh",
    "bellows_creak": "subtitle.true_metallurgy.bellows_whoosh",
    "hammer_iron": "subtitle.true_metallurgy.hammer",
    "hammer_steel": "subtitle.true_metallurgy.hammer",
    "hammer_master": "subtitle.true_metallurgy.hammer",
    "strike_perfect": "subtitle.true_metallurgy.hammer",
    "strike_bad": "subtitle.true_metallurgy.hammer",
    "quench_water": "subtitle.true_metallurgy.quench",
    "quench_oil": "subtitle.true_metallurgy.quench",
    "grind_loop": "subtitle.true_metallurgy.grind",
    "grind_scrape": "subtitle.true_metallurgy.grind",
    "assembly_wood": "subtitle.true_metallurgy.assembly",
    "assembly_metal": "subtitle.true_metallurgy.assembly",
    "masterwork_chime": "subtitle.true_metallurgy.chime",
    "journal_page": "subtitle.true_metallurgy.assembly",
    "tongs_clink": "subtitle.true_metallurgy.assembly",
}

out = {}
for name, subtitle in EVENTS.items():
    out[name] = {
        "subtitle": subtitle,
        "sounds": [{"name": f"{M}:{name}1"}, {"name": f"{M}:{name}2", "pitch": 0.95}],
    }

p = ROOT / "src/main/resources/assets/true_metallurgy/sounds.json"
p.write_text(json.dumps(out, indent=2) + "\n")
print(f"sounds.json: {len(out)} events")

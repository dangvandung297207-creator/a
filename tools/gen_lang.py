"""Parse TMLang.java add() calls into assets/.../lang/en_us.json (single source of truth)."""
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
src = (ROOT / "src/main/java/com/truemetallurgy/datagen/TMLang.java").read_text()
pairs = re.findall(r'add\("((?:[^"\\]|\\.)+)",\s*"((?:[^"\\]|\\.)*)"\)', src)
lang = {}
for key, val in pairs:
    lang[key] = val.replace('\\"', '"')
out = ROOT / "src/main/resources/assets/true_metallurgy/lang/en_us.json"
out.parent.mkdir(parents=True, exist_ok=True)
out.write_text(json.dumps(lang, indent=2, ensure_ascii=False) + "\n")
print(f"lang: {len(lang)} keys -> {out.relative_to(ROOT)}")

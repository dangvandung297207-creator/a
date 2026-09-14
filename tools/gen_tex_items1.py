"""Procedural item textures: tools, pipeline goods, billets."""
import random
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent))
from png import Canvas, lerp, shade

ROOT = Path(__file__).resolve().parent.parent
T = ROOT / "src/main/resources/assets/true_metallurgy/textures/item"

MAT = {
    "copper": (200, 112, 72, 255),
    "iron": (188, 188, 198, 255),
    "steel": (140, 166, 192, 255),
    "hardened": (96, 116, 138, 255),
}
WOOD = (110, 75, 45, 255)

def diag_tool(draw_head):
    c = Canvas(16, 16)
    # handle from bottom-left to center
    for i in range(8):
        x, y = 2 + i, 13 - i
        c.set(x, y, WOOD)
        c.set(x + 1, y, shade(WOOD, 0.8))
    c.set(2, 13, shade(WOOD, 0.7))
    draw_head(c)
    return c

# hammers: head block at top
HAMMER_HEAD = {
    "primitive": (110, 108, 104, 255),
    "copper": MAT["copper"],
    "iron": MAT["iron"],
    "steel": MAT["steel"],
    "hardened": MAT["hardened"],
    "masterwork": (210, 180, 95, 255),
}
for tier, col in HAMMER_HEAD.items():
    def head(c, col=col):
        c.rect(7, 1, 14, 6, col)
        c.rect(7, 1, 14, 2, shade(col, 1.2))
        c.rect(7, 6, 14, 6, shade(col, 0.65))
        c.rect(10, 1, 11, 6, shade(col, 0.8))  # socket band
        c.set(7, 1, shade(col, 1.35))
    diag_tool(head).save(T / f"{tier}_hammer.png")

# tongs: twin prongs
c = Canvas(16, 16)
for i in range(11):
    c.set(3 + i, 12 - i, (150, 150, 160, 255))
    c.set(4 + i, 13 - i, (105, 105, 115, 255))
c.rect(6, 6, 8, 8, (80, 80, 90, 255))  # hinge
c.set(7, 7, (180, 180, 190, 255))
c.set(12, 2, (150, 150, 160, 255))
c.set(13, 1, (105, 105, 115, 255))
c.save(T / "blacksmith_tongs.png")

# crushed ore
for name, fleck in (("crushed_iron_ore", (214, 175, 160, 255)), ("crushed_copper_ore", (230, 140, 90, 255))):
    c = Canvas(16, 16)
    rnd = random.Random(hash(name) % 999)
    for y in range(5, 14):
        wdt = 6 - abs(y - 9)
        for x in range(8 - wdt, 8 + wdt + 1):
            base = (110, 100, 95, 255) if rnd.random() < 0.7 else fleck
            c.set(x + rnd.randint(-1, 1), y, shade(base, 0.85 + rnd.random() * 0.3))
    c.save(T / f"{name}.png")

# blooms: spongy lumps
BLOOM = {"copper": (150, 90, 62, 255), "iron": (140, 130, 125, 255),
         "steel": (120, 135, 150, 255), "hardened": (95, 105, 120, 255)}
for mat, col in BLOOM.items():
    c = Canvas(16, 16)
    rnd = random.Random(hash(mat) % 999)
    widths = [0, 4, 6, 7, 7, 6, 5, 3]
    for i, wdt in enumerate(widths):
        y = 4 + i
        for x in range(8 - wdt, 8 + wdt + 1):
            if rnd.random() < 0.18:
                c.set(x, y, shade(col, 0.45))  # pore
            else:
                c.set(x, y, shade(col, 0.85 + rnd.random() * 0.3))
    for _ in range(6):
        c.set(rnd.randint(3, 12), rnd.randint(4, 10), shade(col, 1.3))
    c.save(T / f"{mat}_bloom.png")

# billets: bar with 5 heat stages
HEAT_TINT = [(0, 0, 0), (90, 20, 12), (225, 80, 20), (250, 165, 40), (255, 240, 190)]
HEAT_MIX = [0.0, 0.45, 0.7, 0.85, 0.95]
for mat, col in MAT.items():
    for stage in range(5):
        c = Canvas(16, 16)
        base = lerp(col, HEAT_TINT[stage] + (255,), HEAT_MIX[stage])
        c.rect(2, 6, 13, 9, base)
        c.rect(2, 6, 13, 6, shade(base, 1.25))
        c.rect(2, 9, 13, 9, shade(base, 0.7))
        c.rect(2, 6, 2, 9, shade(base, 0.75))
        c.rect(13, 6, 13, 9, shade(base, 0.75))
        rnd = random.Random(stage * 7 + 1)
        for _ in range(4):
            c.set(rnd.randint(3, 12), rnd.randint(7, 8), shade(base, 0.85))
        if stage >= 3:  # hot shimmer pixels
            for _ in range(5):
                c.set(rnd.randint(3, 12), rnd.randint(6, 9), (255, 250, 220, 255))
        c.save(T / f"{mat}_billet_{stage}.png")

print("item textures part 1 done")

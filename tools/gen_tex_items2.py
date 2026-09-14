"""Procedural item textures: components, fittings, handles, finished tools, goods."""
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
GRAY = (168, 168, 180, 255)  # components are tinted in code

# sword blade (vertical, grayscale)
c = Canvas(16, 16)
c.set(7, 1, GRAY); c.set(8, 1, GRAY)
c.set(7, 2, GRAY); c.set(8, 2, shade(GRAY, 1.15))
for y in range(3, 11):
    c.set(6, y, shade(GRAY, 1.15))
    c.set(7, y, shade(GRAY, 0.85))
    c.set(8, y, GRAY)
c.rect(4, 11, 10, 11, shade(GRAY, 0.7))
c.rect(7, 12, 8, 14, shade(GRAY, 0.55))
c.set(7, 15, shade(GRAY, 0.7)); c.set(8, 15, shade(GRAY, 0.7))
c.save(T / "sword_blade.png")

# axe head
c = Canvas(16, 16)
c.rect(5, 2, 12, 7, GRAY)
c.rect(5, 2, 5, 7, shade(GRAY, 1.2))  # edge
c.rect(9, 8, 11, 11, shade(GRAY, 0.8))  # beard/socket
c.rect(7, 3, 8, 4, shade(GRAY, 0.4))  # eye
c.rect(5, 7, 12, 7, shade(GRAY, 0.7))
c.save(T / "axe_head.png")

# pickaxe head
c = Canvas(16, 16)
c.rect(1, 5, 14, 6, GRAY)
c.set(0, 5, shade(GRAY, 1.15)); c.set(15, 5, shade(GRAY, 1.15))
c.rect(7, 4, 8, 8, shade(GRAY, 0.9))
c.rect(7, 5, 8, 6, shade(GRAY, 0.4))  # eye
c.rect(1, 6, 14, 6, shade(GRAY, 0.75))
c.save(T / "pickaxe_head.png")

# spear head (leaf + socket)
c = Canvas(16, 16)
widths = [1, 2, 3, 3, 2, 1]
for i, wdt in enumerate(widths):
    y = 2 + i
    c.rect(8 - wdt, y, 7 + wdt, y, GRAY)
c.set(7, 3, shade(GRAY, 1.2)); c.set(7, 4, shade(GRAY, 1.2))
c.rect(7, 8, 8, 12, shade(GRAY, 0.8))
c.rect(6, 12, 9, 13, shade(GRAY, 0.6))
c.save(T / "spear_head.png")

# guards + pommels
for name, col in (("iron_guard", MAT["iron"]), ("steel_guard", MAT["steel"])):
    c = Canvas(16, 16)
    c.rect(3, 7, 12, 8, col)
    c.rect(3, 7, 12, 7, shade(col, 1.2))
    c.rect(7, 6, 8, 9, shade(col, 0.7))
    c.save(T / f"{name}.png")
for name, col in (("iron_pommel", MAT["iron"]), ("steel_pommel", MAT["steel"])):
    c = Canvas(16, 16)
    for y in range(5, 11):
        for x in range(5, 11):
            if (x - 7.5) ** 2 + (y - 7.5) ** 2 <= 9:
                c.set(x, y, col)
    c.set(6, 6, shade(col, 1.3)); c.set(7, 6, shade(col, 1.3))
    c.set(9, 9, shade(col, 0.6))
    c.save(T / f"{name}.png")

# handles
HANDLES = {
    "oak": (154, 106, 58, 255), "spruce": (110, 74, 44, 255), "birch": (196, 178, 132, 255),
    "dark_oak": (74, 52, 32, 255), "bamboo": (178, 160, 80, 255),
    "reinforced": (154, 106, 58, 255), "leather_wrapped": (122, 78, 46, 255), "bone": (224, 220, 204, 255),
}
for name, col in HANDLES.items():
    c = Canvas(16, 16)
    c.rect(6, 2, 9, 13, col)
    c.rect(6, 2, 6, 13, shade(col, 0.75))
    c.rect(9, 2, 9, 13, shade(col, 1.15))
    if name == "bamboo":
        for y in (4, 8, 11):
            c.rect(6, y, 9, y, shade(col, 0.7))
    elif name == "reinforced":
        for y in (4, 5, 10, 11):
            c.rect(6, y, 9, y, (140, 140, 150, 255))
    elif name == "leather_wrapped":
        for i in range(6):
            c.set(6 + (i % 4), 3 + i * 2, shade(col, 0.6))
            c.set(7 + (i % 4), 3 + i * 2, shade(col, 0.6))
    elif name == "bone":
        c.rect(6, 2, 9, 3, shade(col, 0.85))
        c.rect(6, 12, 9, 13, shade(col, 0.85))
    c.save(T / f"{name}_handle.png")

# shaft + binding
c = Canvas(16, 16)
c.rect(7, 0, 8, 15, (110, 75, 45, 255))
c.rect(7, 0, 7, 15, (85, 58, 34, 255))
c.save(T / "long_shaft.png")
c = Canvas(16, 16)
for a in range(16):
    import math
    x, y = int(7.5 + 4 * math.cos(a / 16 * 6.283)), int(7.5 + 4 * math.sin(a / 16 * 6.283))
    c.set(x, y, (122, 78, 46, 255))
    c.set(x, y + 1, (96, 60, 34, 255))
c.rect(4, 7, 11, 8, (196, 178, 150, 255))  # string
c.save(T / "binding.png")

# finished tools (diagonal, per material)
def sword(mat):
    c = Canvas(16, 16)
    col = MAT[mat]
    for i in range(9):  # blade
        x, y = 5 + i, 10 - i
        c.set(x, y, col)
        c.set(x + 1, y, shade(col, 1.2))
    c.set(13, 2, shade(col, 1.25))
    for i in range(4):  # guard
        c.set(3 + i, 11 - i, (70, 70, 80, 255))
    for i in range(3):  # grip
        c.set(1 + i, 14 - i, (96, 64, 38, 255))
    c.set(1, 14, (140, 140, 150, 255))  # pommel
    return c

def axe(mat):
    c = Canvas(16, 16)
    col = MAT[mat]
    for i in range(10):
        c.set(2 + i, 13 - i, (110, 75, 45, 255))
    c.rect(8, 1, 13, 5, col)
    c.rect(12, 1, 13, 5, shade(col, 1.2))  # edge
    c.rect(8, 5, 13, 5, shade(col, 0.7))
    c.rect(9, 2, 10, 3, shade(col, 0.5))  # eye
    return c

def pickaxe(mat):
    c = Canvas(16, 16)
    col = MAT[mat]
    for i in range(10):
        c.set(2 + i, 13 - i, (110, 75, 45, 255))
    c.rect(4, 3, 13, 4, col)
    c.set(3, 3, shade(col, 1.15)); c.set(14, 3, shade(col, 1.15))
    c.rect(8, 2, 9, 5, shade(col, 0.8))
    return c

def spear(mat):
    c = Canvas(16, 16)
    col = MAT[mat]
    for i in range(12):
        c.set(1 + i, 14 - i, (110, 75, 45, 255))
    for i in range(4):  # leaf head
        x, y = 12 + i, 3 - i
        c.set(x, y, col)
        if i < 2:
            c.set(x, y + 1, shade(col, 0.8))
    c.set(11, 4, (122, 78, 46, 255))  # binding
    return c

for mat in MAT:
    sword(mat).save(T / f"{mat}_sword.png")
    axe(mat).save(T / f"{mat}_axe.png")
    pickaxe(mat).save(T / f"{mat}_pickaxe.png")
    spear(mat).save(T / f"{mat}_spear.png")

# goods
c = Canvas(16, 16)
rnd = random.Random(21)
blob = [(x, y) for y in range(5, 12) for x in range(4, 12) if rnd.random() < 0.85]
for x, y in blob:
    c.set(x, y, shade((36, 36, 42, 255), 0.9 + rnd.random() * 0.3))
c.set(6, 6, (120, 120, 130, 255)); c.set(9, 8, (110, 110, 120, 255))
c.save(T / "coke.png")

c = Canvas(16, 16)
c.rect(5, 5, 10, 13, (186, 132, 52, 255))  # oil
c.rect(5, 5, 10, 5, (120, 220, 235, 255))  # glass rim
c.rect(4, 6, 4, 13, (200, 225, 235, 255))
c.rect(11, 6, 11, 13, (200, 225, 235, 255))
c.rect(4, 13, 11, 13, (200, 225, 235, 255))
c.rect(6, 2, 9, 4, (150, 190, 200, 255))  # neck
c.rect(6, 1, 9, 2, (120, 84, 50, 255))  # cork
c.set(6, 7, (235, 200, 140, 255))  # gloss
c.save(T / "quench_oil.png")

c = Canvas(16, 16)
c.rect(3, 2, 12, 13, (110, 70, 40, 255))  # cover
c.rect(3, 2, 4, 13, (80, 50, 28, 255))  # spine
c.rect(11, 3, 12, 12, (216, 198, 160, 255))  # pages
c.rect(5, 6, 9, 6, (150, 110, 70, 255))
c.rect(5, 8, 9, 8, (150, 110, 70, 255))
c.rect(5, 10, 7, 10, (150, 110, 70, 255))
c.set(10, 7, (210, 180, 100, 255))  # clasp
c.save(T / "blacksmith_journal.png")

c = Canvas(16, 16)
c.rect(2, 3, 13, 12, (150, 172, 192, 255))  # sheet
c.rect(2, 3, 13, 3, (110, 132, 155, 255))
c.rect(2, 3, 2, 12, (110, 132, 155, 255))
for i in range(6):  # sword sketch
    c.set(6 + i, 10 - i, (40, 55, 75, 255))
c.set(5, 10, (40, 55, 75, 255)); c.set(4, 9, (40, 55, 75, 255))
c.rect(11, 4, 12, 5, (200, 60, 50, 255))  # seal
c.save(T / "kings_edge_blueprint.png")

print("item textures part 2 done")

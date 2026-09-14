"""Procedural block textures."""
import random
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent))
from png import Canvas, lerp, shade

ROOT = Path(__file__).resolve().parent.parent
T = ROOT / "src/main/resources/assets/true_metallurgy/textures/block"

def forge_side(tier, base, mortar, band):
    c = Canvas(16, 16)
    c.bricks(base, mortar, seed=hash(tier) % 1000)
    if band:
        c.rect(0, 6, 15, 7, band)
        c.rect(0, 6, 15, 6, shade(band, 1.25))
        for x in (2, 7, 12):
            c.set(x, 7, shade(band, 0.6))
    c.save(T / f"forge_{tier}_side.png")

def forge_top(tier, base):
    c = Canvas(16, 16, shade(base, 0.8))
    c.noise(10, seed=5)
    c.rect(5, 5, 10, 10, (12, 10, 10, 255))
    c.rect(5, 5, 10, 5, shade(base, 1.3))
    c.border(shade(base, 0.6))
    c.save(T / f"forge_{tier}_top.png")

HEATS = [(15, 12, 12), (110, 25, 12), (200, 70, 15), (235, 125, 20), (250, 200, 60), (255, 246, 205)]

def forge_front(heat):
    c = Canvas(16, 16)
    c.bricks((88, 76, 70, 255), (55, 47, 44, 255), seed=9)
    rnd = random.Random(100 + heat)
    # arched opening
    opening = [(x, y) for y in range(7, 15) for x in range(5, 11)]
    opening += [(x, 6) for x in range(6, 10)] + [(x, 5) for x in range(7, 9)]
    glow = HEATS[heat]
    for x, y in opening:
        depth = (y - 5) / 10.0
        col = lerp((10, 8, 8, 255), glow + (255,), 0.35 + 0.65 * depth) if heat else (12, 10, 10, 255)
        if heat and rnd.random() < 0.3:
            col = lerp(col, (255, 240, 180, 255), 0.5)
        c.set(x, y, col)
    # stone rim
    for x, y in opening:
        for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            nx, ny = x + dx, y + dy
            if (nx, ny) not in opening and c.get(nx, ny)[0] > 40:
                c.set(nx, ny, shade(c.get(nx, ny), 0.75))
    # warm spill on surrounding bricks at high heat
    if heat >= 3:
        for y in range(16):
            for x in range(16):
                if (x, y) in opening:
                    continue
                d = min(abs(x - 7.5) + abs(y - 10), 12)
                if d < 7:
                    c.set(x, y, lerp(c.get(x, y), glow + (255,), (7 - d) * 0.03 * heat))
    c.save(T / f"forge_front_{heat}.png")

forge_side("primitive", (112, 96, 86, 255), (70, 60, 54, 255), None)
forge_side("iron", (122, 122, 130, 255), (75, 75, 84, 255), (150, 150, 160, 255))
forge_side("steel", (92, 98, 110, 255), (58, 62, 72, 255), (140, 165, 195, 255))
forge_side("master", (62, 62, 74, 255), (38, 38, 48, 255), (205, 172, 82, 255))
for tier, base in (("primitive", (96, 82, 74, 255)), ("iron", (105, 105, 112, 255)),
                   ("steel", (80, 86, 96, 255)), ("master", (55, 55, 66, 255))):
    forge_top(tier, base)
for heat in range(6):
    forge_front(heat)

# bellows
c = Canvas(16, 16)
c.planks((122, 84, 50, 255), vertical=True, seed=3)
c.save(T / "bellows_wood.png")
c = Canvas(16, 16, (118, 74, 44, 255))
c.noise(9, seed=4)
c.rect(0, 0, 15, 0, (88, 54, 32, 255))
c.rect(0, 15, 15, 15, (88, 54, 32, 255))
for x in range(1, 15, 2):
    c.set(x, 1, (150, 100, 60, 255))
    c.set(x, 14, (150, 100, 60, 255))
c.save(T / "bellows_leather.png")

# anvils
for tier, base in (("basic", (108, 102, 96, 255)), ("iron", (138, 138, 148, 255)),
                   ("steel", (148, 168, 192, 255)), ("master", (118, 128, 148, 255))):
    c = Canvas(16, 16, base)
    c.noise(9, seed=hash(tier) % 500)
    rnd = random.Random(hash(tier) % 999)
    for _ in range(7):  # scratches + dents
        x, y = rnd.randint(1, 12), rnd.randint(1, 14)
        for dx in range(rnd.randint(2, 5)):
            c.set(x + dx, y, shade(base, 0.72))
    for _ in range(4):
        c.set(rnd.randint(0, 15), rnd.randint(0, 15), shade(base, 0.6))
    if tier == "master":
        for x in range(16):
            c.set(x, 0, (205, 172, 82, 255))
    c.border(shade(base, 0.62))
    c.save(T / f"anvil_{tier}.png")

# barrel
c = Canvas(16, 16)
c.planks((110, 76, 46, 255), vertical=True, seed=7)
for band_y in (2, 12):
    c.rect(0, band_y, 15, band_y + 1, (70, 70, 78, 255))
    c.rect(0, band_y, 15, band_y, (105, 105, 115, 255))
    for x in (3, 8, 12):
        c.set(x, band_y + 1, (40, 40, 46, 255))
c.save(T / "barrel_side.png")
for liq, inner in (("none", (14, 10, 8, 255)), ("water", (42, 108, 198, 255)), ("oil", (122, 80, 24, 255))):
    c = Canvas(16, 16, (110, 76, 46, 255))
    c.noise(8, seed=8)
    c.rect(2, 2, 13, 13, inner)
    rnd = random.Random(hash(liq) % 77)
    for _ in range(10 if liq == "none" else 16):
        x, y = rnd.randint(3, 12), rnd.randint(3, 12)
        c.set(x, y, lerp(inner, (255, 255, 255, 255), 0.35 if liq != "none" else 0.05))
    if liq == "oil":
        c.rect(3, 4, 10, 4, (200, 150, 80, 255))
    c.save(T / f"barrel_top_{liq}.png")

# grinding wheel
c = Canvas(16, 16)
c.planks((96, 70, 46, 255), vertical=False, seed=11)
c.save(T / "wheel_wood.png")
c = Canvas(16, 16, (128, 128, 134, 255))
c.noise(10, seed=12)
for r in range(7, 0, -2):  # concentric grooves
    for x in range(8 - r, 8 + r + 1):
        c.set(x, 8 - r, shade((128, 128, 134, 255), 0.8))
        c.set(x, 8 + r, shade((128, 128, 134, 255), 0.8))
    for y in range(8 - r, 8 + r + 1):
        c.set(8 - r, y, shade((128, 128, 134, 255), 0.8))
        c.set(8 + r, y, shade((128, 128, 134, 255), 0.8))
c.set(8, 8, (70, 70, 76, 255))
c.save(T / "wheel_stone.png")

# assembly + rack + steel
c = Canvas(16, 16)
c.planks((128, 92, 58, 255), vertical=False, seed=13)
rnd = random.Random(13)
for _ in range(5):  # stains + scratches
    x, y = rnd.randint(0, 13), rnd.randint(0, 14)
    c.rect(x, y, x + 2, y + 1, (96, 68, 42, 255))
c.save(T / "assembly_top.png")
c = Canvas(16, 16)
c.planks((96, 68, 42, 255), vertical=True, seed=14)
c.save(T / "assembly_side.png")
c = Canvas(16, 16)
c.planks((84, 60, 38, 255), vertical=True, seed=15)
c.save(T / "rack_wood.png")
c = Canvas(16, 16, (150, 175, 200, 255))
c.noise(7, seed=16)
for i in range(16):
    c.set(i, i, lerp((150, 175, 200, 255), (255, 255, 255, 255), 0.25))
c.border((96, 116, 138, 255))
for x, y in ((2, 2), (13, 2), (2, 13), (13, 13)):
    c.set(x, y, (210, 225, 240, 255))
c.save(T / "steel_block.png")

print("block textures done")

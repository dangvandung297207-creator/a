"""Particles, GUI screens, entity texture."""
import math
import random
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent))
from png import Canvas, lerp, shade

ROOT = Path(__file__).resolve().parent.parent
TX = ROOT / "src/main/resources/assets/true_metallurgy/textures"

# ---------------- particles ----------------
P = TX / "particle"
c = Canvas(16, 16)
c.rect(6, 6, 9, 9, (255, 240, 200, 255))
c.rect(5, 5, 10, 10, (255, 170, 60, 255))
c.rect(6, 6, 9, 9, (255, 245, 215, 255))
c.save(P / "spark.png")
c = Canvas(16, 16)
c.rect(6, 6, 9, 9, (230, 90, 25, 255))
c.set(7, 7, (255, 180, 90, 255)); c.set(8, 8, (255, 180, 90, 255))
c.save(P / "ember.png")
c = Canvas(16, 16)
for y in range(16):
    for x in range(16):
        d = math.hypot(x - 7.5, y - 7.5) / 7.5
        if d < 1:
            a = int(140 * (1 - d))
            c.set(x, y, (210, 215, 220, a))
c.save(P / "steam.png")
c = Canvas(16, 16)
rnd = random.Random(3)
for _ in range(14):
    c.set(rnd.randint(5, 10), rnd.randint(5, 10), (160, 160, 170, 255))
c.save(P / "metal_dust.png")

# ---------------- GUI ----------------
G = TX / "gui"

def base_panel(wdt=176, hgt=186):
    c = Canvas(256, 256)
    c.fill((0, 0, 0, 0))
    c.rect(0, 0, wdt - 1, hgt - 1, (40, 32, 26, 255))  # leather panel
    c.noise(7, seed=42, area=(0, 0, wdt - 1, hgt - 1))
    # wood frame
    for i in range(4):
        col = (96, 64, 38, 255) if i % 2 == 0 else (78, 52, 30, 255)
        c.rect(i, i, wdt - 1 - i, i, col)
        c.rect(i, hgt - 1 - i, wdt - 1 - i, hgt - 1 - i, col)
        c.rect(i, i, i, hgt - 1 - i, col)
        c.rect(wdt - 1 - i, i, wdt - 1 - i, hgt - 1 - i, col)
    # iron corners
    for cx, cy in ((0, 0), (wdt - 8, 0), (0, hgt - 8), (wdt - 8, hgt - 8)):
        c.rect(cx, cy, cx + 7, cy + 7, (110, 110, 120, 255))
        c.rect(cx + 1, cy + 1, cx + 6, cy + 6, (80, 80, 90, 255))
        c.set(cx + 3, cy + 3, (150, 150, 160, 255))
    return c

def slot(c, x, y):
    c.rect(x - 1, y - 1, x + 16, y + 16, (20, 14, 10, 255))
    c.rect(x, y, x + 15, y + 15, (52, 44, 36, 255))
    c.rect(x, y, x + 15, y, (18, 12, 8, 255))
    c.rect(x, y, x, y + 15, (18, 12, 8, 255))
    c.rect(x, y + 15, x + 15, y + 15, (84, 72, 58, 255))
    c.rect(x + 15, y, x + 15, y + 15, (84, 72, 58, 255))

def player_slots(c):
    for r in range(3):
        for col in range(9):
            slot(c, 8 + col * 18, 104 + r * 18)
    for col in range(9):
        slot(c, 8 + col * 18, 162)

# forge screen (matches ForgeMenu slots)
c = base_panel()
player_slots(c)
for x, y in ((44, 53), (80, 53), (116, 35), (116, 57), (143, 46)):
    slot(c, x, y)
# flame gauge frame at (116,35)? keep simple: decorative rivets row
for x in range(12, 164, 12):
    c.set(x, 92, (150, 150, 160, 255))
c.save(G / "forge.png")

# forging screen
c = base_panel()
player_slots(c)
slot(c, 80, 35)
# anvil pictogram area frame
c.rect(60, 30, 116, 60, (20, 14, 10, 255))
c.save(G / "forging.png")

# quench screen
c = base_panel()
player_slots(c)
slot(c, 80, 35)
c.save(G / "quench.png")

# grind screen
c = base_panel()
player_slots(c)
slot(c, 80, 35)
c.save(G / "grind.png")

# assembly screen
c = base_panel()
player_slots(c)
for x, y in ((26, 35), (48, 35), (26, 57), (48, 57), (74, 46), (128, 46)):
    slot(c, x, y)
# arrow between input and output
for i in range(30):
    c.set(94 + i, 54, (150, 150, 160, 255))
for i in range(5):
    c.set(124 - i, 52 + i, (150, 150, 160, 255))
    c.set(124 - i, 56 - i, (150, 150, 160, 255))
c.save(G / "assembly.png")

# journal (240x180 parchment)
c = Canvas(256, 256)
c.fill((0, 0, 0, 0))
c.rect(0, 0, 239, 179, (214, 188, 148, 255))
c.noise(9, seed=99, area=(0, 0, 239, 179))
for i in range(6):  # leather border
    col = (96, 60, 34, 255) if i % 2 == 0 else (74, 46, 26, 255)
    c.rect(i, i, 239 - i, i, col)
    c.rect(i, 179 - i, 239 - i, 179 - i, col)
    c.rect(i, i, i, 179 - i, col)
    c.rect(239 - i, i, 239 - i, 179 - i, col)
for x in range(0, 240, 8):  # stitching
    c.set(x, 8, (150, 110, 70, 255))
    c.set(x, 171, (150, 110, 70, 255))
c.save(G / "journal.png")

# ---------------- entity: blacksmith villager-ish ----------------
E = TX / "entity"
c = Canvas(64, 64, (0, 0, 0, 0))
SKIN = (198, 150, 106, 255)
HAT = (112, 72, 40, 255)
ROBE = (96, 60, 36, 255)
APRON = (64, 48, 36, 255)
# head block (0,0,32,16): skin with face on front (8,8,8,8)
c.rect(0, 0, 31, 15, SKIN)
c.rect(8, 8, 15, 15, SKIN)
c.set(9, 10, (40, 60, 40, 255)); c.set(10, 10, (40, 60, 40, 255))  # eyes
c.set(13, 10, (40, 60, 40, 255)); c.set(14, 10, (40, 60, 40, 255))
c.rect(11, 11, 12, 13, shade(SKIN, 0.85))  # nose shadow
c.rect(8, 8, 15, 9, shade(SKIN, 0.92))  # brow
# hat (32,0,32,16)
c.rect(32, 0, 63, 15, HAT)
c.rect(32, 12, 63, 15, shade(HAT, 0.7))  # brim
c.rect(40, 2, 55, 4, shade(HAT, 1.2))  # band highlight
# nose (24,0)? villager nose overlaps head UV; paint separately at (24,0,4,4)
c.rect(24, 0, 27, 3, shade(SKIN, 0.88))
# body (0,16,32,16) robe with apron
c.rect(0, 16, 31, 31, ROBE)
c.rect(12, 16, 19, 31, APRON)  # apron front
c.rect(12, 22, 19, 23, (50, 36, 26, 255))  # belt
c.set(15, 22, (180, 160, 110, 255))  # buckle
# arms (32,16...) sleeves
c.rect(32, 16, 63, 23, shade(ROBE, 0.85))
c.rect(32, 24, 63, 31, SKIN)  # hands
# legs (0,32,32,16) boots
c.rect(0, 32, 31, 39, (60, 48, 40, 255))
c.rect(0, 40, 31, 47, (44, 34, 28, 255))
c.save(E / "blacksmith.png")

print("particles + gui + entity done")

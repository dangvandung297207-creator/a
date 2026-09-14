#!/usr/bin/env python3
"""Procedural pixel-art textures for The Master Blacksmith (Minecraft-compatible 16px)."""
from PIL import Image, ImageDraw
import random, os

A = "src/main/resources/assets/masterblacksmith/textures"
BLK = f"{A}/block"
ITM = f"{A}/item"
os.makedirs(BLK, exist_ok=True)
os.makedirs(ITM, exist_ok=True)
os.makedirs(f"{ITM}/hot", exist_ok=True)

def C(h, a=255):
    h = h.lstrip("#")
    return (int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16), a)

def new(size=16):
    return Image.new("RGBA", (size, size), (0, 0, 0, 0))

def save(img, path):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img.save(path)

def shade(color, f):
    return (min(255, int(color[0] * f)), min(255, int(color[1] * f)),
            min(255, int(color[2] * f)), color[3])

def jitter(color, rnd, amt=12):
    return (max(0, min(255, color[0] + rnd.randint(-amt, amt))),
            max(0, min(255, color[1] + rnd.randint(-amt, amt))),
            max(0, min(255, color[2] + rnd.randint(-amt, amt))), color[3])

def bricks(draw, rnd, x0, y0, w, h, brick_cols, mortar, bh=4, bw=8):
    for yy in range(y0, y0 + h, bh):
        row = ((yy - y0) // bh) % 2
        off = (bw // 2) if row else 0
        for xx in range(x0 - off, x0 + w, bw):
            for py in range(yy, min(yy + bh, y0 + h)):
                for px in range(max(xx, x0), min(xx + bw, x0 + w)):
                    if py == yy or px == xx:
                        draw.point((px, py), fill=jitter(mortar, rnd, 6))
                    else:
                        draw.point((px, py), fill=jitter(rnd.choice(brick_cols), rnd, 8))

def stone_noise(draw, rnd, base, x0=0, y0=0, w=16, h=16, amt=10):
    for yy in range(y0, y0 + h):
        for xx in range(x0, x0 + w):
            draw.point((xx, yy), fill=jitter(base, rnd, amt))

def speckle(draw, rnd, colors, n, x0=0, y0=0, w=16, h=16):
    for _ in range(n):
        draw.point((rnd.randrange(x0, x0 + w), rnd.randrange(y0, y0 + h)),
                   fill=rnd.choice(colors))

def wood_planks(draw, rnd, base, dark, x0=0, y0=0, w=16, h=16, pw=4):
    for xx in range(x0, x0 + w):
        plank = (xx - x0) // pw
        for yy in range(y0, y0 + h):
            if xx % pw == 0:
                draw.point((xx, yy), fill=jitter(dark, rnd, 6))
            else:
                c = jitter(base, rnd, 7)
                if (yy + plank * 3) % 7 == 0:
                    c = shade(c, 0.9)
                draw.point((xx, yy), fill=c)
    # knots
    for _ in range(2):
        kx, ky = rnd.randrange(x0 + 1, x0 + w - 1), rnd.randrange(y0 + 1, y0 + h - 1)
        draw.point((kx, ky), fill=shade(dark, 0.8))

def brushed_metal(draw, rnd, base, dark, light, x0=0, y0=0, w=16, h=16, scratches=6):
    for yy in range(y0, y0 + h):
        band = 0.94 + 0.06 * ((yy * 7) % 3) / 2
        for xx in range(x0, x0 + w):
            draw.point((xx, yy), fill=jitter(shade(base, band), rnd, 5))
    for _ in range(scratches):
        x, y = rnd.randrange(x0, x0 + w), rnd.randrange(y0, y0 + h)
        ln = rnd.randint(2, 5)
        dx = rnd.choice([-1, 0, 1])
        for i in range(ln):
            px, py = x + dx * i, y + i
            if x0 <= px < x0 + w and y0 <= py < y0 + h:
                draw.point((px, py), fill=rnd.choice([light, dark]))
    speckle(draw, rnd, [dark], scratches // 2, x0, y0, w, h)

def ore_blobs(draw, rnd, cols, n=5, size=(2, 3)):
    for _ in range(n):
        bw, bh = rnd.randint(*size), rnd.randint(*size)
        bx, by = rnd.randrange(0, 17 - bw), rnd.randrange(0, 17 - bh)
        for yy in range(by, by + bh):
            for xx in range(bx, bx + bw):
                if rnd.random() < 0.85:
                    edge = xx in (bx, bx + bw - 1) or yy in (by, by + bh - 1)
                    draw.point((xx, yy), fill=rnd.choice(cols[1:] if edge else cols[:2]))

# ================= BLOCK TEXTURES =================
def block_refractory():
    img = new(); d = ImageDraw.Draw(img); rnd = random.Random(11)
    bricks(d, rnd, 0, 0, 16, 16, [C("#7A3B28"), C("#8A4430"), C("#6B3323")], C("#4A3630"))
    speckle(d, rnd, [C("#2A1E1A")], 14)
    save(img, f"{BLK}/refractory_brick_block.png")

FORGE_TRIM = {
    "primitive": ([C("#6B6B6B"), C("#7A7A7A"), C("#5A5A5A")], C("#3A3230"), None),
    "iron": ([C("#7A3B28"), C("#8A4430"), C("#6B3323")], C("#4A3630"), C("#8A8D91")),
    "steel": ([C("#5E2F22"), C("#6B3628"), C("#522920")], C("#3A2A26"), C("#6E7681")),
    "master": ([C("#4A2620"), C("#552C24"), C("#40221C")], C("#2E2020"), C("#C8A44A")),
}

def block_forge_sides():
    for tier, (bcols, mortar, trim) in FORGE_TRIM.items():
        img = new(); d = ImageDraw.Draw(img); rnd = random.Random(hash(tier) % 9999)
        if tier == "primitive":
            stone_noise(d, rnd, C("#6E6E6E"))
            bricks(d, rnd, 2, 3, 12, 10, [C("#7A3B28"), C("#6B3323")], C("#4A3630"), bh=3, bw=6)
        else:
            bricks(d, rnd, 0, 0, 16, 16, bcols, mortar)
            speckle(d, rnd, [C("#241A16")], 10)
        if trim:
            for x in range(16):
                d.point((x, 0), fill=jitter(trim, rnd, 5))
                d.point((x, 15), fill=jitter(trim, rnd, 5))
            for y in range(16):
                d.point((0, y), fill=jitter(trim, rnd, 5))
                d.point((15, y), fill=jitter(trim, rnd, 5))
        if tier == "master":
            for x in range(3, 13, 3):  # brass rivets
                d.point((x, 2), fill=C("#E8C86A"))
                d.point((x, 13), fill=C("#E8C86A"))
        save(img, f"{BLK}/forge_side_{tier}.png")

def block_forge_tops():
    for tier, (bcols, mortar, trim) in FORGE_TRIM.items():
        img = new(); d = ImageDraw.Draw(img); rnd = random.Random(hash("top" + tier) % 9999)
        bricks(d, rnd, 0, 0, 16, 16, bcols, mortar)
        for yy in range(5, 11):
            for xx in range(5, 11):  # chimney mouth
                edge = xx in (5, 10) or yy in (5, 10)
                d.point((xx, yy), fill=C("#1A1210") if not edge else C("#0A0808"))
        if trim:
            for x in range(4, 12):
                d.point((x, 4), fill=trim); d.point((x, 11), fill=trim)
            for y in range(4, 12):
                d.point((4, y), fill=trim); d.point((11, y), fill=trim)
        save(img, f"{BLK}/forge_top_{tier}.png")

HEAT_INTERIOR = {
    0: ([C("#2E2A28"), C("#3A3433")], []),
    1: ([C("#3A2A26"), C("#4A2E24")], [C("#8A2A12")]),
    2: ([C("#5A2A1A"), C("#7A3418")], [C("#E8541A"), C("#8A2A12")]),
    3: ([C("#B8541A"), C("#E87A1A")], [C("#FFAA2A"), C("#FFE27A")]),
    4: ([C("#FFAA2A"), C("#E87A1A")], [C("#FFE27A"), C("#FFF6D8")]),
    5: ([C("#FFE27A"), C("#FFF6D8")], [C("#FFFFFF"), C("#FFF6D8")]),
}

def block_forge_fronts():
    for heat in range(6):
        img = new(); d = ImageDraw.Draw(img); rnd = random.Random(100 + heat)
        bricks(d, rnd, 0, 0, 16, 16, [C("#7A3B28"), C("#6B3323"), C("#8A4430")], C("#4A3630"))
        coals, flames = HEAT_INTERIOR[heat]
        for yy in range(5, 13):
            for xx in range(3, 13):
                frame = xx in (3, 12) or yy in (5, 12)
                if frame:
                    d.point((xx, yy), fill=C("#2A201C"))
                else:
                    d.point((xx, yy), fill=jitter(rnd.choice(coals), rnd, 10))
        if flames:
            for _ in range(4 + heat * 3):
                fx = rnd.randrange(4, 12)
                fh = rnd.randint(1, 2 + heat // 2)
                for i in range(fh):
                    fy = 11 - i - rnd.randint(0, 1)
                    if 6 <= fy <= 11:
                        d.point((fx, fy), fill=rnd.choice(flames))
        if heat >= 2:  # glow spill on the brick frame
            spill = [C("#8A3A12"), C("#B85A1A"), C("#E87A1A"), C("#FFAA2A")][min(3, heat - 2)]
            for yy in range(4, 14):
                for xx in ([2, 13] if yy in range(5, 13) else list(range(2, 14))):
                    if rnd.random() < 0.25 + heat * 0.1:
                        d.point((xx, yy), fill=jitter(spill, rnd, 14))
        save(img, f"{BLK}/forge_front_{heat}.png")

def block_bellows():
    img = new(); d = ImageDraw.Draw(img); rnd = random.Random(21)
    wood_planks(d, rnd, C("#7A5A34"), C("#4A3620"))
    save(img, f"{BLK}/bellows_wood.png")
    img = new(); d = ImageDraw.Draw(img); rnd = random.Random(22)
    stone_noise(d, rnd, C("#6B4A2E"), amt=8)
    for x in range(0, 16, 4):  # stitched ribs
        for y in range(16):
            d.point((x, y), fill=C("#4A2E1C"))
            if y % 2 == 0:
                d.point((x + 1, y), fill=C("#8A6A44"))
    save(img, f"{BLK}/bellows_leather.png")

ANVIL_METALS = {
    "basic": (C("#6E6E72"), C("#4A4A4E"), C("#9A9AA0")),
    "iron": (C("#8A8D91"), C("#5A5D61"), C("#B8BBC0")),
    "steel": (C("#6E7681"), C("#484E56"), C("#A8B2BE")),
    "master": (C("#525A66"), C("#34383F"), C("#C8A44A")),
}

def block_anvils():
    for tier, (base, dark, light) in ANVIL_METALS.items():
        rnd = random.Random(hash("as" + tier) % 9999)
        img = new(); d = ImageDraw.Draw(img)
        brushed_metal(d, rnd, base, dark, light, scratches=8)
        save(img, f"{BLK}/anvil_side_{tier}.png")
        rnd = random.Random(hash("at" + tier) % 9999)
        img = new(); d = ImageDraw.Draw(img)
        brushed_metal(d, rnd, shade(base, 1.08), dark, light, scratches=12)
        for yy in range(4, 12):  # polished work face
            for xx in range(4, 12):
                d.point((xx, yy), fill=jitter(light, rnd, 10))
        if tier == "master":  # engraved border
            for x in range(2, 14):
                d.point((x, 2), fill=dark); d.point((x, 13), fill=dark)
            for y in range(2, 14):
                d.point((2, y), fill=dark); d.point((13, y), fill=dark)
        save(img, f"{BLK}/anvil_top_{tier}.png")

def block_barrel():
    img = new(); d = ImageDraw.Draw(img); rnd = random.Random(31)
    wood_planks(d, rnd, C("#6E5230"), C("#41301C"), pw=3)
    for y in (1, 7, 13):  # iron hoops
        for x in range(16):
            d.point((x, y), fill=jitter(C("#5A5D61"), rnd, 6))
    save(img, f"{BLK}/barrel_side.png")
    img = new(); d = ImageDraw.Draw(img); rnd = random.Random(32)
    for yy in range(16):
        for xx in range(16):
            edge = xx in (0, 15) or yy in (0, 15) or xx in (1, 14) or yy in (1, 14)
            d.point((xx, yy), fill=jitter(C("#6E5230") if edge else C("#2E2114"), rnd, 6))
    save(img, f"{BLK}/barrel_top_empty.png")
    for name, col in [("water", C("#2E5AC8")), ("oil", C("#5E3E16")), ("special", C("#5E2E8A"))]:
        img = new(); d = ImageDraw.Draw(img); rnd = random.Random(hash("lq" + name) % 9999)
        for yy in range(16):
            for xx in range(16):
                edge = xx in (0, 15) or yy in (0, 15) or xx in (1, 14) or yy in (1, 14)
                if edge:
                    d.point((xx, yy), fill=jitter(C("#6E5230"), rnd, 6))
                else:
                    c = jitter(col, rnd, 8)
                    if (xx + yy * 2) % 9 == 0:
                        c = shade(C("#FFFFFF"), 0.85)
                    d.point((xx, yy), fill=c)
        save(img, f"{BLK}/liquid_{name}.png")

def block_wheel_table_rack_shelf():
    img = new(); d = ImageDraw.Draw(img); rnd = random.Random(41)
    wood_planks(d, rnd, C("#5E472A"), C("#38281A"))
    save(img, f"{BLK}/wheel_frame.png")
    img = new(); d = ImageDraw.Draw(img); rnd = random.Random(42)
    stone_noise(d, rnd, C("#7E7E7E"), amt=8)
    cx = cy = 7.5
    for yy in range(16):
        for xx in range(16):
            dist = ((xx - cx) ** 2 + (yy - cy) ** 2) ** 0.5
            if dist > 7.4:
                d.point((xx, yy), fill=(0, 0, 0, 0))
            elif abs(dist - 5.5) < 0.6 or abs(dist - 2.5) < 0.6:
                d.point((xx, yy), fill=C("#5E5E5E"))
            elif dist < 1.2:
                d.point((xx, yy), fill=C("#4A4A4A"))
    save(img, f"{BLK}/wheel_stone.png")
    img = new(); d = ImageDraw.Draw(img); rnd = random.Random(43)
    wood_planks(d, rnd, C("#7A5F38"), C("#4A3A22"), pw=8)
    speckle(d, rnd, [C("#3A2E1E")], 8)  # scars and stains
    d.line([(2, 12), (9, 3)], fill=C("#4A3A22"))
    save(img, f"{BLK}/table_top.png")
    img = new(); d = ImageDraw.Draw(img); rnd = random.Random(44)
    wood_planks(d, rnd, C("#6B5232"), C("#40301C"))
    save(img, f"{BLK}/table_side.png")
    img = new(); d = ImageDraw.Draw(img); rnd = random.Random(45)
    wood_planks(d, rnd, C("#5E4A2C"), C("#382A1A"), pw=8)
    save(img, f"{BLK}/rack_wood.png")
    img = new(); d = ImageDraw.Draw(img); rnd = random.Random(46)
    brushed_metal(d, rnd, C("#5A5D61"), C("#3A3D40"), C("#8A8D91"), scratches=5)
    save(img, f"{BLK}/shelf_metal.png")
    img = new(); d = ImageDraw.Draw(img); rnd = random.Random(47)
    wood_planks(d, rnd, C("#6B5232"), C("#40301C"))
    save(img, f"{BLK}/shelf_wood.png")

def block_ores():
    img = new(); d = ImageDraw.Draw(img); rnd = random.Random(51)
    stone_noise(d, rnd, C("#7E7E7E"))
    ore_blobs(d, rnd, [C("#C9D2D6"), C("#9AA4AB"), C("#7E8891")])
    save(img, f"{BLK}/tin_ore.png")
    img = new(); d = ImageDraw.Draw(img); rnd = random.Random(52)
    stone_noise(d, rnd, C("#4A4A52"))
    ore_blobs(d, rnd, [C("#C9D2D6"), C("#9AA4AB"), C("#7E8891")])
    save(img, f"{BLK}/deepslate_tin_ore.png")
    img = new(); d = ImageDraw.Draw(img); rnd = random.Random(53)
    stone_noise(d, rnd, C("#3A3A44"))
    ore_blobs(d, rnd, [C("#8A9AFF"), C("#5E6BFF"), C("#3E46C8")], n=4)
    speckle(d, rnd, [C("#D8E0FF")], 6)
    save(img, f"{BLK}/starfall_ore.png")
    for name, base, dark, light in [
            ("tin_block", C("#B8C2C9"), C("#8A949C"), C("#E2E8ED")),
            ("raw_tin_block", C("#9AA4AB"), C("#6E787F"), C("#C4CDD3")),
            ("bronze_block", C("#A8842E"), C("#7A5F20"), C("#D8B45E")),
            ("steel_block", C("#6E7681"), C("#484E56"), C("#A8B2BE")),
            ("starfall_block", C("#5E6BFF"), C("#3E46C8"), C("#A8B2FF"))]:
        img = new(); d = ImageDraw.Draw(img); rnd = random.Random(hash("mb" + name) % 9999)
        brushed_metal(d, rnd, base, dark, light, scratches=4)
        for x in range(16):  # block seams
            d.point((x, 0), fill=dark); d.point((x, 15), fill=dark)
            d.point((0, x), fill=dark); d.point((15, x), fill=dark)
        save(img, f"{BLK}/{name}.png")


# ================= ITEM MASKS (16x16 ASCII) =================
# K outline, B base, L light, D dark, E edge, W wood/secondary, X accent, T tang, G guard, P pommel
MASKS = {
"billet": [
"................",
"................",
"................",
"................",
"................",
"...KKKKKKKKKK...",
"..KBBBBBBBBBBK..",
"..KBLBBBBBBBDK..",
"..KBLBBBBBBBDK..",
"..KBBBBBBBBBDK..",
"..KDDDDDDDDDDK..",
"...KKKKKKKKKK...",
"................",
"................",
"................",
"................"],
"bloom": [
"................",
"................",
".....KKKK.......",
"....KBBBBK......",
"...KBBBBBBK.....",
"..KBLBBBDDBK....",
"..KBBBBBBBBK....",
"..KBLBDDBBBK....",
"..KBBBBBBBBK....",
"...KBDBBLBK.....",
"....KBBBBK......",
".....KKKK.......",
"................",
"................",
"................",
"................"],
"processed": [
"................",
"................",
"................",
".....KKK........",
"....KBBBK.......",
"...KBBBBBK......",
"...KBLBBDK......",
"..KBBBBBBBK.....",
"..KBLBBBDDBK....",
"..KBBBBBBBBK....",
"..KBBBBLBBBK....",
"...KBBBBBBK.....",
"....KDDDDK......",
".....KKKK.......",
"................",
"................"],
"ingot": [
"................",
"................",
"................",
"................",
"................",
"................",
"...KKKKKKKKK....",
"..KBBBBBBBBBK...",
"..KBLBBBBBBDK...",
"..KBLBBBBBBDK...",
".KBBBBBBBBBBBK..",
".KBLLBBBBBBBDK..",
".KBBBBBBBBBBDK..",
".KDDDDDDDDDDDDK.",
"..KKKKKKKKKKKK..",
"................"],
"hammer": [
"....KKKKK.......",
"...KHHHHHK......",
"...KHLHHHK......",
"...KHHHHHK......",
"....KHHHK.......",
".....KKK........",
"....KWWK........",
"...KWWK.........",
"...KWWK.........",
"..KWWK..........",
"..KWWK..........",
".KWWK...........",
".KWWK...........",
"KWKK............",
"KK..............",
"................"],
"tongs": [
"....KK..KK......",
"....KWK.KWK.....",
"....KWK.KWK.....",
"....KWK.KWK.....",
"....KWK.KWK.....",
"....KWKKKWK.....",
".....KWKWK......",
".....KWKWK......",
".....KWKWK......",
".....KWKWK......",
".....KWKWK......",
".....KWKWK......",
".....KWKWK......",
"......KWK.......",
"......KKK.......",
"................"],
"sword_blade": [
".............KKK",
"............KBEK",
"...........KBEK.",
"..........KBEK..",
".........KBEK...",
"........KBEK....",
".......KBEK.....",
"......KBEK......",
".....KBEK.......",
"....KBEK........",
"...KBEK.........",
"...KTK..........",
"...KTK..........",
"....K...........",
"................",
"................"],
"forged_sword": [
".............KKK",
"............KBEK",
"...........KBEK.",
"..........KBEK..",
".........KBEK...",
"........KBEK....",
".......KBEK.....",
"......KBEK......",
".....KBEK.......",
"....KBEK........",
"..KGGGGGK.......",
"....KWK.........",
"....KWK.........",
"....KWK.........",
"....KPK.........",
".....K.........."],
"axe_head": [
"................",
"................",
".....KKKK.......",
"....KBBBBK......",
"...KBBEBBK......",
"...KBBEBBK......",
"..KBBBEBBBK.....",
"..KBBBEBBBK.....",
"..KBBBEBBBK.....",
"...KBBEBBK......",
"...KBBEBBK......",
"....KBBBK.......",
"....KBBBK.......",
".....KKK........",
"................",
"................"],
"forged_axe": [
"....KKKK........",
"...KBBBBK.......",
"...KBBEBK.......",
"...KBBEBK.......",
"....KBBBK.......",
".....KBK........",
".....KWK........",
"....KWWK........",
"....KWWK........",
"...KWWK.........",
"...KWWK.........",
"..KWWK..........",
"..KWWK..........",
".KKK............",
"................",
"................"],
"pickaxe_head": [
"................",
"................",
"................",
".KK........KK...",
".KBBK....KBBK...",
"..KBBK..KBBK....",
"..KBBKKKBBK.....",
"...KBBBBBK......",
"....KBBBK.......",
"....KBBBK.......",
"....KBBBK.......",
".....KKK........",
"................",
"................",
"................",
"................"],
"forged_pickaxe": [
"................",
".KK........KK...",
".KBBK....KBBK...",
"..KBBK..KBBK....",
"..KBBKKKBBK.....",
"...KBBBBBK......",
"....KBBBK.......",
"....KWWK........",
"....KWWK........",
"...KWWK.........",
"...KWWK.........",
"...KWWK.........",
"...KWWK.........",
"....KK..........",
"................",
"................"],
"spear_head": [
".......K........",
"......KBK.......",
"......KBK.......",
".....KBBBK......",
".....KBBBK......",
".....KBBBK......",
"......KBK.......",
"......KBK.......",
"......KXK.......",
"......KXK.......",
"......KXK.......",
".......K........",
"................",
"................",
"................",
"................"],
"forged_spear": [
".............KK.",
"............KBBK",
"...........KBBK.",
"..........KBBK..",
".........KXK....",
".........KWK....",
"........KWWK....",
".......KWWK.....",
"......KWWK......",
".....KWWK.......",
"....KWWK........",
"...KWWK.........",
"...KWWK.........",
"...KKK..........",
"................",
"................"],
"armor_plate": [
"................",
"................",
"....KKKKKKK.....",
"...KBBBBBBBK....",
"..KBBLLBBBBBK...",
"..KBLLBBBBBBK...",
"..KBBBBBBBBBK...",
"..KBBBBBDDBBK...",
"..KBBBBBBBBBK...",
"..KBBBDDBBBBK...",
"..KBBBBBBBBBK...",
"...KBBBBBBBK....",
"....KKKKKKK.....",
"................",
"................",
"................"],
"guard": [
"................",
"................",
"................",
"................",
"................",
"................",
"................",
"..KKKKKKKKKKK...",
".KBBBBBBBBBBBK..",
".KBLBBBBBBBBDK..",
".KBBBBBBBBBBBK..",
"..KKKKKKKKKKK...",
"................",
"................",
"................",
"................"],
"grip": [
"................",
"................",
"................",
"................",
".........KK.....",
"........KWWK....",
".......KWWK.....",
"......KWWK......",
".....KWWK.......",
"....KWWK........",
"....KWWK........",
"...KWWK.........",
"...KKK..........",
"................",
"................",
"................"],
"pommel": [
"................",
"................",
"................",
"................",
"................",
"......KKK.......",
".....KBBBK......",
"....KBLBBK......",
"....KBBBBK......",
"....KBBDBK......",
".....KBBK.......",
"......KK........",
"................",
"................",
"................",
"................"],
"handle": [
"............KK..",
"...........KWWK.",
"..........KWWK..",
".........KWWK...",
"........KWWK....",
".......KWWK.....",
"......KWWK......",
".....KWWK.......",
"....KWWK........",
"...KWWK.........",
"...KWWK.........",
"..KWWK..........",
"..KKK...........",
"................",
"................",
"................"],
"helmet": [
"................",
"................",
".....KKKKK......",
"....KBBBBBK.....",
"...KBBBBBBBK....",
"...KBLBBBDBK....",
"...KBBBBBBBK....",
"...KBBBBBBBK....",
"...KBBKKBBBK....",
"...KBBBBBBBK....",
"....KKKKKKK.....",
"................",
"................",
"................",
"................",
"................"],
"chestplate": [
"....KK...KK.....",
"...KBBK.KBBK....",
"...KBBKKKBBK....",
"...KBBBBBBBK....",
"...KBLBBBDBK....",
"...KBBBBBBBK....",
"...KBBBBBBBK....",
"...KBBBBBBBK....",
"....KBBBBBK.....",
"....KBBBBBK.....",
"....KBBBBBK.....",
"....KKKKKKK.....",
"................",
"................",
"................",
"................"],
"leggings": [
"...KKKKKKKKK....",
"...KBBBBBBBK....",
"...KBBBBBBBK....",
"...KBLBBBDBK....",
"....KBBBBBK.....",
"....KBBBK.......",
"....KBBBK.......",
"....KBBK.KBBK...",
"....KBBK.KBBK...",
"....KBBK.KBBK...",
"....KBBK.KBBK...",
"....KBBK.KBBK...",
".....KK...KK....",
"................",
"................",
"................"],
"boots": [
"................",
"................",
"................",
"....KKK..KKK....",
"...KBBK.KBBK....",
"...KBBK.KBBK....",
"...KBBK.KBBK....",
"...KBBK.KBBK....",
"...KBBBKKBBBK...",
"...KBBBBBBBBK...",
"...KBBBBBBBBK...",
"....KKKKKKKK....",
"................",
"................",
"................",
"................"],
"oil_bucket": [
"................",
"................",
".....KKKKK......",
"....KBBBBBK.....",
"....KBBBBBK.....",
"....KLLLLLK.....",
"....KLLLLLK.....",
"....KBBBBBK.....",
"....KBBBBBK.....",
"....KBDDBBK.....",
"....KBBBBBK.....",
"....KBBBBBK.....",
".....KBBBK......",
".....KKKKK......",
"................",
"................"],
"vial": [
"................",
"................",
"......KK........",
"......KWK.......",
"......KWK.......",
".....KWWWK......",
"....KWWWWWK.....",
"....KWLLLWK.....",
"....KWLLLWK.....",
"....KWLLLWK.....",
"....KWLLLWK.....",
".....KLLLK......",
".....KLLLK......",
"......KKK.......",
"................",
"................"],
"journal": [
"................",
"................",
"....KKKKKKK.....",
"...KBBBBBBBK....",
"...KBPBBBBLK....",
"...KBPBBBBLK....",
"...KBPBBBCLK....",
"...KBPBBBBLK....",
"...KBPBBBBLK....",
"...KBPBBBBLK....",
"...KBPBBBBLK....",
"...KBBBBBBBK....",
"....KKKKKKK.....",
"................",
"................",
"................"],
"blueprint": [
"................",
"................",
"....KKKKKKKKK...",
"...KPPPPPPPPPK..",
"...KPKKKKKKPPK..",
"...KPKXKKXKPPK..",
"...KPKKKKKKPPK..",
"...KPPPPPPPPPK..",
"...KPPPPPPPPPK..",
"....KKKKKKKKK...",
"................",
"................",
"................",
"................",
"................",
"................"],
"shard": [
"................",
".......K........",
"......KBK.......",
"......KBK.......",
".....KBLBK......",
".....KBLBK......",
"....KBLBLBK.....",
"....KBBBBBK.....",
"....KBBBBBK.....",
".....KBBBK......",
".....KBBBK......",
"......KBK.......",
"......KBK.......",
".......K........",
"................",
"................"],
"coke": [
"................",
"................",
"................",
".....KKK..KK....",
"....KBBBK.KBK...",
"....KBBBKBBBK...",
"...KBBBBBBBBK...",
"...KBBLBBBDBK...",
"...KBBBBBBBBK...",
"....KBBBBBBK....",
"....KBBDBBK.....",
".....KBBBK......",
"......KKK.......",
"................",
"................",
"................"],
"rivet": [
"................",
"................",
"................",
"................",
"................",
"................",
"......KK........",
".....KBBK.......",
".....KBLK.......",
".....KBBK.......",
"......KK........",
"................",
"................",
"................",
"................",
"................"],
"strip": [
"................",
"............KK..",
"...........KLLK.",
"..........KLLK..",
".........KLLK...",
"........KLLK....",
".......KLLK.....",
"......KLLK......",
".....KLLK.......",
"....KLLK........",
"....KLLK........",
"...KLLK.........",
"...KKK..........",
"................",
"................",
"................"],
}

METAL_PAL = {
    "copper": ("#B87333", "#E8A86E", "#6E4420"),
    "tin": ("#B8C2C9", "#E8EEF2", "#767F87"),
    "bronze": ("#A8842E", "#D8B45E", "#6B5220"),
    "iron": ("#8A8D91", "#C4C7CB", "#56595D"),
    "steel": ("#6E7681", "#A8B2BE", "#43484F"),
    "hardened_steel": ("#525A66", "#8A94A2", "#2E333A"),
    "damascus_steel": ("#7A8699", "#B8C4D4", "#4A5461"),
    "starfall_steel": ("#6E7BFF", "#B8C0FF", "#3E46C8"),
}
HANDLE_PAL = {
    "oak": "#8A6A3E", "spruce": "#5E4630", "birch": "#B8A87E", "dark_oak": "#4A3423",
    "bamboo": "#A8B45E", "reinforced": "#7A6A52", "leather_wrapped": "#6E4A2C", "bone": "#D8D4C4",
}
LIQUID_PAL = {
    "oil_bucket": "#6B4A1F", "salt_water_bucket": "#5FA8D3", "herbal_oil_bucket": "#5F7A3A",
    "mineral_oil_bucket": "#4A4A55", "alchemical_oil_bucket": "#7A3FA0",
}
VIAL_PAL = {"blood_quench_vial": "#8A1A1A", "starfall_quench_vial": "#4A5FFF"}
SEAL_PAL = {"blueprint_kings_edge": "#E8C86A", "blueprint_oathkeeper": "#C83A3A",
            "blueprint_stonesplitter": "#6E8A5E", "blueprint_skypiercer": "#5FA8D3",
            "blueprint_aegis": "#9A6ED8"}

def pal_for(base, light, dark, extra=None):
    p = {"K": C("#1A1210"), "B": C(base), "L": C(light), "D": C(dark), "E": C(light)}
    if extra:
        p.update(extra)
    return p

def render_mask(mask, palette, seed=1, amt=6):
    for row in mask:
        assert len(row) == 16, f"bad row: {row!r}"
    img = new(); d = ImageDraw.Draw(img); rnd = random.Random(seed)
    for y, row in enumerate(mask):
        for x, ch in enumerate(row):
            if ch == ".":
                continue
            col = palette.get(ch, palette["K"])
            d.point((x, y), fill=jitter(col, rnd, amt))
    return img

def damascus_pass(img, dark):
    d = ImageDraw.Draw(img)
    import math
    for x in range(16):
        y = 8 + int(2.2 * math.sin(x * 0.9))
        for dy in (-1, 0, 1):
            yy = y + dy
            if 0 <= yy < 16 and img.getpixel((x, yy))[3] > 0:
                px = img.getpixel((x, yy))
                if px[:3] != C("#1A1210")[:3] and dy == 0:
                    d.point((x, yy), fill=dark)

OAK = ("#8A6A3E", "#B8905A", "#5E4426")

def item_metals():
    for m, (b, l, d) in METAL_PAL.items():
        save(render_mask(MASKS["billet"], pal_for(b, l, d), seed=hash("b" + m) % 9999), f"{ITM}/billet_{m}.png")
        save(render_mask(MASKS["bloom"], pal_for(b, l, d), seed=hash("l" + m) % 9999), f"{ITM}/bloom_{m}.png")
        p = pal_for(b, l, "#4A4A4E")
        p["D"] = C("#4A4A4E"); p["B"] = shade(C(b), 0.85)
        save(render_mask(MASKS["processed"], p, seed=hash("p" + m) % 9999), f"{ITM}/processed_{m}.png")
    ingots = {"tin_ingot": "tin", "bronze_ingot": "bronze", "steel_ingot": "steel",
              "hardened_steel_ingot": "hardened_steel", "damascus_steel_ingot": "damascus_steel",
              "starfall_steel_ingot": "starfall_steel"}
    for item, m in ingots.items():
        b, l, d = METAL_PAL[m]
        img = render_mask(MASKS["ingot"], pal_for(b, l, d), seed=hash(item) % 9999)
        if m == "damascus_steel":
            damascus_pass(img, C(d))
        save(img, f"{ITM}/{item}.png")
    save(render_mask(MASKS["processed"], pal_for("#9AA4AB", "#C4CDD3", "#5A646B"),
                     seed=71), f"{ITM}/raw_tin.png")
    save(render_mask(MASKS["shard"], pal_for("#6E7BFF", "#D8E0FF", "#3E46C8"), seed=72), f"{ITM}/starfall_shard.png")

def item_tools():
    heads = {"hammer_copper": "copper", "hammer_iron": "iron", "hammer_steel": "steel",
             "hammer_hardened": "hardened_steel", "hammer_masterwork": "damascus_steel"}
    for item, m in heads.items():
        b, l, d = METAL_PAL[m]
        wcol = "#D8D4C4" if item == "hammer_masterwork" else OAK[0]
        img = render_mask(MASKS["hammer"], pal_for(b, l, d, {"H": C(b), "W": C(wcol)}),
                          seed=hash(item) % 9999)
        if m == "damascus_steel":
            damascus_pass(img, C(d))
        save(img, f"{ITM}/{item}.png")
    save(render_mask(MASKS["tongs"], pal_for("#8A8D91", "#C4C7CB", "#56595D", {"W": C("#6E6E72")}),
                     seed=81), f"{ITM}/tongs.png")

def item_components():
    rough = pal_for("#7E7E84", "#9A9AA2", "#4A4A50")
    fine = pal_for("#8A929C", "#D8DEE6", "#4E565F", {"E": C("#F2F6FA")})
    pairs = [("blade_blank", "sword_blade", "sword_blade"),
             ("axe_head_blank", "axe_head", "axe_head"),
             ("pick_head_blank", "pickaxe_head", "pickaxe_head"),
             ("spear_head_blank", "spear_head", "spear_head"),
             ("armor_plate_blank", "armor_plate", "armor_plate")]
    for blank, finished, mask in pairs:
        save(render_mask(MASKS[mask], rough, seed=hash(blank) % 9999, amt=9), f"{ITM}/{blank}.png")
        save(render_mask(MASKS[mask], fine, seed=hash(finished) % 9999, amt=4), f"{ITM}/{finished}.png")

def item_parts_handles():
    save(render_mask(MASKS["guard"], pal_for("#8A8D91", "#C4C7CB", "#56595D"), seed=91), f"{ITM}/guard_iron.png")
    save(render_mask(MASKS["guard"], pal_for("#6E7681", "#A8B2BE", "#43484F"), seed=92), f"{ITM}/guard_steel.png")
    save(render_mask(MASKS["guard"], pal_for("#C8A44A", "#E8C86A", "#7A6228"), seed=93), f"{ITM}/guard_brass.png")
    save(render_mask(MASKS["pommel"], pal_for("#8A8D91", "#C4C7CB", "#56595D"), seed=94), f"{ITM}/pommel_iron.png")
    save(render_mask(MASKS["pommel"], pal_for("#6E7681", "#A8B2BE", "#43484F"), seed=95), f"{ITM}/pommel_steel.png")
    save(render_mask(MASKS["pommel"], pal_for("#C8A44A", "#E8C86A", "#7A6228"), seed=96), f"{ITM}/pommel_brass.png")
    save(render_mask(MASKS["grip"], {"K": C("#1A1210"), "W": C("#8A5A30")}, seed=97), f"{ITM}/grip_leather.png")
    save(render_mask(MASKS["grip"], {"K": C("#1A1210"), "W": C("#D8D4C4")}, seed=98), f"{ITM}/grip_bone.png")
    save(render_mask(MASKS["grip"], {"K": C("#1A1210"), "W": C("#7A3FA0")}, seed=99), f"{ITM}/grip_exotic.png")
    for h, col in HANDLE_PAL.items():
        img = render_mask(MASKS["handle"], {"K": C("#1A1210"), "W": C(col)}, seed=hash("h" + h) % 9999)
        d = ImageDraw.Draw(img)
        if h == "reinforced":  # iron bands
            for y in (4, 9):
                for x in range(16):
                    if img.getpixel((x, y))[:3] == C(col)[:3]:
                        pass
            for xy in [(10, 3), (9, 4), (7, 6), (6, 7), (4, 10), (3, 11)]:
                if img.getpixel(xy)[3] > 0:
                    d.point(xy, fill=C("#5A5D61"))
        if h == "leather_wrapped":  # stitching
            for xy in [(11, 2), (8, 5), (5, 8)]:
                for dx, dy in [(0, 0), (1, 0)]:
                    px, py = xy[0] + dx, xy[1] + dy
                    if img.getpixel((px, py))[3] > 0:
                        d.point((px, py), fill=C("#D8B878"))
        save(img, f"{ITM}/handle_{h}.png")

def item_weapons_armor():
    steel = METAL_PAL["steel"]
    blade = pal_for(steel[0], steel[1], steel[2], {"E": C("#F2F6FA"), "W": C(OAK[0]),
                                                 "G": C("#8A8D91"), "P": C("#6E7681"), "X": C("#43484F")})
    save(render_mask(MASKS["forged_sword"], blade, seed=101), f"{ITM}/forged_sword.png")
    save(render_mask(MASKS["forged_axe"], blade, seed=102), f"{ITM}/forged_axe.png")
    save(render_mask(MASKS["forged_pickaxe"], blade, seed=103), f"{ITM}/forged_pickaxe.png")
    save(render_mask(MASKS["forged_spear"], blade, seed=104), f"{ITM}/forged_spear.png")
    armor = pal_for("#7E8894", "#C4CDD6", "#4E565F")
    save(render_mask(MASKS["helmet"], armor, seed=105), f"{ITM}/forged_helmet.png")
    save(render_mask(MASKS["chestplate"], armor, seed=106), f"{ITM}/forged_chestplate.png")
    save(render_mask(MASKS["leggings"], armor, seed=107), f"{ITM}/forged_leggings.png")
    save(render_mask(MASKS["boots"], armor, seed=108), f"{ITM}/forged_boots.png")

def item_goods():
    bucket_base = {"K": C("#1A1210"), "B": C("#8A8D91"), "D": C("#56595D")}
    for item, liq in LIQUID_PAL.items():
        p = dict(bucket_base); p["L"] = C(liq)
        save(render_mask(MASKS["oil_bucket"], p, seed=hash(item) % 9999), f"{ITM}/{item}.png")
    for item, liq in VIAL_PAL.items():
        p = {"K": C("#1A1210"), "W": C("#B8D0D8"), "L": C(liq)}
        save(render_mask(MASKS["vial"], p, seed=hash(item) % 9999), f"{ITM}/{item}.png")
    save(render_mask(MASKS["journal"], {"K": C("#1A1210"), "B": C("#6B4A2E"),
            "P": C("#D8C89A"), "L": C("#B8A87E"), "C": C("#C8A44A")}, seed=109), f"{ITM}/blacksmith_journal.png")
    for item, seal in SEAL_PAL.items():
        save(render_mask(MASKS["blueprint"], {"K": C("#1A1210"), "P": C("#D8C89A"), "X": C(seal)},
                         seed=hash(item) % 9999), f"{ITM}/{item}.png")
    save(render_mask(MASKS["ingot"], pal_for("#8A4430", "#A85A3E", "#5E2F22"), seed=110), f"{ITM}/refractory_brick.png")
    save(render_mask(MASKS["coke"], pal_for("#2E2E33", "#4A4A52", "#141416"), seed=111), f"{ITM}/coke.png")
    save(render_mask(MASKS["coke"], pal_for("#5A5A60", "#7E7E86", "#343438"), seed=112), f"{ITM}/slag.png")
    save(render_mask(MASKS["strip"], pal_for("#8A5A30", "#B87E4A", "#5E3A1E"), seed=113), f"{ITM}/leather_strip.png")
    save(render_mask(MASKS["rivet"], pal_for("#8A8D91", "#C4C7CB", "#56595D"), seed=114), f"{ITM}/metal_rivet.png")

HOT_LEVELS = [
    {"K": "#3A1408", "D": "#5A1408", "B": "#A8230A", "L": "#E8541A", "E": "#FF7A1A"},
    {"K": "#4A1408", "D": "#A8230A", "B": "#E8541A", "L": "#FFAA2A", "E": "#FFE27A"},
    {"K": "#5A1A08", "D": "#E8541A", "B": "#FFAA2A", "L": "#FFE27A", "E": "#FFFFFF"},
]

def item_hot_variants():
    shapes = {"billet": "billet", "bloom": "bloom", "blade_blank": "sword_blade",
              "axe_head_blank": "axe_head", "pick_head_blank": "pickaxe_head",
              "spear_head_blank": "spear_head", "armor_plate_blank": "armor_plate",
              "sword_blade": "sword_blade", "axe_head": "axe_head",
              "pickaxe_head": "pickaxe_head", "spear_head": "spear_head",
              "armor_plate": "armor_plate"}
    for shape, mask in shapes.items():
        for lvl, cols in enumerate(HOT_LEVELS):
            p = {"K": C(cols["K"]), "D": C(cols["D"]), "B": C(cols["B"]),
                 "L": C(cols["L"]), "E": C(cols["E"]), "H": C(cols["B"]),
                 "W": C(cols["D"]), "X": C(cols["L"]), "T": C(cols["B"]),
                 "G": C(cols["D"]), "P": C(cols["D"])}
            save(render_mask(MASKS[mask], p, seed=200 + lvl, amt=8), f"{ITM}/hot/{shape}_{lvl}.png")


# ================= GUI =================
def gui_panel(w, h):
    img = Image.new("RGBA", (256, 256), (0, 0, 0, 0))
    d = ImageDraw.Draw(img); rnd = random.Random(301)
    for yy in range(h):
        for xx in range(w):
            edge = xx < 3 or yy < 3 or xx >= w - 3 or yy >= h - 3
            if edge:
                c = C("#3A3D40") if (xx + yy) % 2 == 0 else C("#2A2D30")
            else:
                c = jitter(C("#4A3826") if (xx // 8 + yy // 8) % 2 == 0 else C("#443322"), rnd, 5)
            d.point((xx, yy), fill=c)
    for x in range(3, w - 3):  # brass inner lines
        d.point((x, 3), fill=C("#7A6228")); d.point((x, h - 4), fill=C("#7A6228"))
    for y in range(3, h - 3):
        d.point((3, y), fill=C("#7A6228")); d.point((w - 4, y), fill=C("#7A6228"))
    for cx, cy in [(6, 6), (w - 7, 6), (6, h - 7), (w - 7, h - 7)]:  # rivets
        d.point((cx, cy), fill=C("#C8A44A"))
    return img

def gui_textures():
    save(gui_panel(176, 166), f"{A}/gui/forge_hearth.png")
    save(gui_panel(200, 196), f"{A}/gui/anvil_forging.png")
    save(gui_panel(176, 186), f"{A}/gui/assembly.png")
    save(gui_panel(176, 186), f"{A}/gui/grinding.png")
    # journal page
    img = Image.new("RGBA", (256, 256), (0, 0, 0, 0))
    d = ImageDraw.Draw(img); rnd = random.Random(302)
    for yy in range(200):
        for xx in range(220):
            edge = xx < 6 or yy < 6 or xx >= 214 or yy >= 194
            if edge:
                c = C("#5E4028") if (xx + yy) % 3 else C("#4A3220")
            elif xx in (6, 213) or yy in (6, 193):
                c = C("#2E2114")
            else:
                c = jitter(C("#D8C89A"), rnd, 6)
                if xx > 200 or yy > 184:  # burned edges
                    c = shade(c, 0.82)
            d.point((xx, yy), fill=c)
    for x in range(100, 120):  # spine
        for y in range(6, 194):
            d.point((x, y), fill=shade(C("#D8C89A"), 0.9))
    speckle(d, rnd, [C("#8A7A5E")], 40, 8, 8, 204, 184)
    save(img, f"{A}/gui/journal.png")

# ================= ENTITY =================
def entity_textures():
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    d = ImageDraw.Draw(img); rnd = random.Random(401)
    skin, hair, beard = C("#C8966E"), C("#3A2A1C"), C("#5A4230")
    shirt, apron, pants, boots = C("#4A5A6A"), C("#6B4A2E"), C("#3A3A44"), C("#2E2114")
    def region(x0, y0, w, h, col, amt=6):
        for yy in range(y0, y0 + h):
            for xx in range(x0, x0 + w):
                d.point((xx, yy), fill=jitter(col, rnd, amt))
    # head (front 8,8 8x8 + sides)
    region(0, 8, 32, 8, skin)
    region(0, 0, 32, 8, hair)          # top/bottom hair
    region(8, 12, 8, 4, beard)         # beard on front
    region(0, 12, 8, 4, beard); region(16, 12, 8, 4, beard); region(24, 12, 8, 4, beard)
    d.point((10, 10), fill=C("#1A1210")); d.point((13, 10), fill=C("#1A1210"))  # eyes
    # torso
    region(16, 16, 24, 16, shirt)
    region(20, 16, 16, 16, apron)      # apron front/back
    region(20, 16, 16, 2, C("#4A2E1C"))  # apron bib top
    for x in (22, 33):                 # straps
        for y in range(16, 22):
            d.point((x, y), fill=C("#3A2616"))
    # arms
    region(40, 16, 16, 16, shirt)
    region(40, 26, 16, 6, skin)        # forearms
    # legs
    region(0, 16, 16, 16, pants)
    region(0, 28, 16, 4, boots)
    # hat layer (second layer head -> hood)
    region(32, 8, 32, 8, C("#2E2114"))
    save(img, f"{A}/entity/travelling_blacksmith.png")

# ================= PARTICLES =================
def particle_textures():
    def dot(name, size, col, seed):
        img = new(); d = ImageDraw.Draw(img); rnd = random.Random(seed)
        c = 8
        for yy in range(16):
            for xx in range(16):
                dist = ((xx - c + 0.5) ** 2 + (yy - c + 0.5) ** 2) ** 0.5
                if dist < size:
                    a = 255 if dist < size * 0.6 else 140
                    d.point((xx, yy), fill=jitter(col, rnd, 20)[:3] + (a,))
        save(img, f"{A}/particle/{name}.png")
    dot("ember_0", 3.2, C("#FF7A1A"), 501)
    dot("ember_1", 2.4, C("#E8541A"), 502)
    dot("ember_2", 1.6, C("#A8230A"), 503)
    for i, ln in enumerate([6, 4]):
        img = new(); d = ImageDraw.Draw(img)
        for j in range(ln):
            d.point((8 - ln // 2 + j, 8), fill=C("#FFF6D8"))
            d.point((8 - ln // 2 + j, 9), fill=C("#FFE27A"))
        save(img, f"{A}/particle/spark_{i}.png")
    dot("steam_0", 6.5, C("#D8D8D8"), 504)
    dot("steam_1", 5.0, C("#C4C4C4"), 505)
    dot("steam_2", 3.5, C("#A8A8A8"), 506)
    dot("dust_0", 2.6, C("#8A7A5E"), 507)
    dot("dust_1", 1.8, C("#6E6250"), 508)

# ================= ARMOR LAYERS =================
def armor_textures():
    for layer, base in [(1, C("#7E8894")), (2, C("#6E7681"))]:
        img = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
        d = ImageDraw.Draw(img); rnd = random.Random(600 + layer)
        for yy in range(32):
            for xx in range(64):
                c = jitter(base, rnd, 6)
                if yy >= 20:
                    c = shade(c, 0.92)
                d.point((xx, yy), fill=c)
        if layer == 2:  # belt
            for x in range(16, 40):
                d.point((x, 20), fill=C("#C8A44A"))
                d.point((x, 21), fill=C("#7A6228"))
        save(img, f"{A}/models/armor/forged_layer_{layer}.png")

def main():
    block_refractory(); block_forge_sides(); block_forge_tops(); block_forge_fronts()
    block_bellows(); block_anvils(); block_barrel(); block_wheel_table_rack_shelf(); block_ores()
    item_metals(); item_tools(); item_components(); item_parts_handles()
    item_weapons_armor(); item_goods(); item_hot_variants()
    gui_textures(); entity_textures(); particle_textures(); armor_textures()
    print("textures OK")

if __name__ == "__main__":
    main()

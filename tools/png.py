"""Minimal stdlib PNG writer + canvas helpers for procedural pixel art."""
import random
import struct
import zlib
from pathlib import Path


def write_png(path, w, h, pixels):
    def chunk(typ, data):
        out = struct.pack(">I", len(data)) + typ + data
        out += struct.pack(">I", zlib.crc32(typ + data) & 0xFFFFFFFF)
        return out

    raw = bytearray()
    for row in pixels:
        raw.append(0)
        for r, g, b, a in row:
            raw += struct.pack("4B", r & 255, g & 255, b & 255, a & 255)
    data = (b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", struct.pack(">IIBBBBB", w, h, 8, 6, 0, 0, 0))
            + chunk(b"IDAT", zlib.compress(bytes(raw), 9)) + chunk(b"IEND", b""))
    path = Path(path)
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(data)


def lerp(c1, c2, t):
    return tuple(int(a + (b - a) * t) for a, b in zip(c1[:3], c2[:3])) + (255,)


def shade(c, f):
    return (min(255, int(c[0] * f)), min(255, int(c[1] * f)), min(255, int(c[2] * f)), 255)


class Canvas:
    def __init__(self, w, h, bg=(0, 0, 0, 0)):
        self.w, self.h = w, h
        self.px = [[bg for _ in range(w)] for _ in range(h)]

    def set(self, x, y, c):
        if 0 <= x < self.w and 0 <= y < self.h:
            self.px[int(y)][int(x)] = c

    def get(self, x, y):
        if 0 <= x < self.w and 0 <= y < self.h:
            return self.px[int(y)][int(x)]
        return (0, 0, 0, 0)

    def fill(self, c):
        for y in range(self.h):
            for x in range(self.w):
                self.px[y][x] = c

    def rect(self, x0, y0, x1, y1, c):
        for y in range(max(0, y0), min(self.h, y1 + 1)):
            for x in range(max(0, x0), min(self.w, x1 + 1)):
                self.px[y][x] = c

    def border(self, c, t=1):
        for i in range(t):
            self.rect(i, i, self.w - 1 - i, i, c)
            self.rect(i, self.h - 1 - i, self.w - 1 - i, self.h - 1 - i, c)
            self.rect(i, i, i, self.h - 1 - i, c)
            self.rect(self.w - 1 - i, i, self.w - 1 - i, self.h - 1 - i, c)

    def noise(self, amt, seed=1, area=None):
        rnd = random.Random(seed)
        x0, y0, x1, y1 = area or (0, 0, self.w - 1, self.h - 1)
        for y in range(max(0, y0), min(self.h, y1 + 1)):
            for x in range(max(0, x0), min(self.w, x1 + 1)):
                r, g, b, a = self.px[y][x]
                if a == 0:
                    continue
                d = rnd.randint(-amt, amt)
                self.px[y][x] = (max(0, min(255, r + d)), max(0, min(255, g + d)),
                                 max(0, min(255, b + d)), a)

    def bricks(self, base, mortar, bh=4, seed=1):
        rnd = random.Random(seed)
        self.fill(base)
        for y in range(0, self.h, bh):
            self.rect(0, y, self.w - 1, y, mortar)
            off = (y // bh % 2) * 4
            for x in range(off, self.w, 8):
                for yy in range(y, min(y + bh, self.h)):
                    self.set(x, yy, mortar)
        self.noise(10, seed)

    def planks(self, base, vertical=True, pw=4, seed=1):
        rnd = random.Random(seed)
        self.fill(base)
        n = self.w // pw if vertical else self.h // pw
        for i in range(n):
            f = 0.92 + rnd.random() * 0.16
            c = shade(base, f)
            if vertical:
                self.rect(i * pw, 0, i * pw + pw - 1, self.h - 1, c)
                self.rect(i * pw, 0, i * pw, self.h - 1, shade(base, 0.7))
            else:
                self.rect(0, i * pw, self.w - 1, i * pw + pw - 1, c)
                self.rect(0, i * pw, self.w - 1, i * pw, shade(base, 0.7))
        self.noise(8, seed)

    def save(self, path):
        write_png(path, self.w, self.h, self.px)

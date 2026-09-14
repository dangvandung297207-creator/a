# Đoản Kiếm Hư Không — Ender Blade

**NeoForge 1.21.1 · Java 21** legendary dimensional assassin weapon.

## Features

| System | Details |
|--------|---------|
| **Stats** | Netherite tier · 8.0 dmg · 1.6 speed · 2031 durability · **4-block reach** · Fireproof · Epic |
| **Model** | Custom voxel 3D (Blade / Edge / Void Cracks / Guard / Handle / Pommel / Ender Core / Runes / Emissive) |
| **Passive** | *Chém Xuyên Không* — 20% spatial displace (~3 blk), momentum interrupt, Void Mark |
| **Ender Echo** | RMB launch piercing void orb · RMB again ≤2s teleport (no pearl damage) · 12s CD |
| **Void Anchor** | Shift+RMB place / recall floating End core |
| **Void Slash** | Key **R** — dimensional cut rift ahead · damage + mark |
| **Paradox Step** | Key **V** (or auto on hit during window) — afterimage counter, blink behind, crit |
| **Void Mark ×3** | Rotating rune · at 3 stacks **Rift Collapse** |
| **Ultimate** | Key **G** or Sprint+Shift+RMB — 5s End Dimension domain → mass collapse |
| **VFX** | Mesh rifts, rings, afterimages, emissive cracks — not vanilla particle spam |
| **Audio** | Distorted End/void sound events for every ability |

## Craft

**Shapeless:** Netherite Sword + Dragon Breath + Eye of Ender → Ender Blade

## Controls

| Input | Ability |
|-------|---------|
| LMB | 3-hit combo + passive |
| RMB | Ender Echo / teleport |
| Shift + RMB | Void Anchor |
| **R** | Void Slash |
| **V** | Paradox Step |
| **G** | End Dimension ultimate |
| Sprint + Shift + RMB | Ultimate (alt) |

## Build

```bash
./gradlew build
# JAR → build/libs/enderblade-1.0.0.jar
```

Requires **JDK 21** and NeoForge `21.1.x`.

## Preview

Open `index.html` for the Three.js weapon showcase (source of visual truth).

## Package

`com.enderblade` — item, entities, abilities, network, client renderers/VFX, data components, attachments.

# Đoản Kiếm Hư Vô — Ender Blade

**NeoForge 1.21.1 · Java 21** legendary dimensional assassin weapon.

> An ancient Netherite blade containing a fractured piece of the End dimension.

## Features

| System | Details |
|--------|---------|
| **Stats** | Netherite tier · 8.0 dmg · 1.6 speed · 2031 durability · **4-block reach** · Fireproof · Epic |
| **Model** | Custom 3D parts: Blade / BladeEdge / VoidCracks / Guard / Handle / Pommel / EnderCore / Runes / Emissive / Fragments |
| **Materials** | Separate Netherite, dark metal, flowing void cracks, controlled emissives (cracks / runes / core only) |
| **Passive** | *Chém Xuyên Không* — 20% spatial displace (~3 blk), momentum interrupt, Void Mark |
| **Ender Echo** | RMB piercing void orb (rings + trail) · RMB ≤2s teleport · 12s CD |
| **Void Anchor** | Shift+RMB place / recall floating End core |
| **Void Slash** | Key **R** — literal cut-in-space rift · damage + mark |
| **Paradox Step** | Key **V** — afterimage counter, blink behind, crit slash |
| **Void Mark ×3** | Progressive dimensional rune · at 3 stacks **Rift Collapse** (space compresses inward) |
| **Ultimate** | Key **G** — 5s End Dimension domain → pull marked → mass collapse |
| **VFX** | Mesh rifts, ribbons, rings, afterimages, emissive cracks — not vanilla particle spam |
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

## Showcase (Three.js)

Open `index.html` (or serve the repo root) for the polished weapon inspection view:

- **Combat View** — mannequin + full ability animations
- **Inspect** — weapon-only orbit / zoom
- Toggle **VFX**, **Wireframe**, **Model Only**
- Play each animation individually · Reset · Playback speed
- Click structure parts to isolate components

```bash
python3 -m http.server 8080
# → http://localhost:8080
```

## Build

```bash
./gradlew build
# JAR → build/libs/enderblade-1.0.0.jar
# CI also publishes release/enderblade-1.0.0.jar
```

Requires **JDK 21** and NeoForge `21.1.x`.

## Package

`com.enderblade` — item, entities, abilities, network, client renderers/VFX, data components, attachments.

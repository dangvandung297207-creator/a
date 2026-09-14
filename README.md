# Ender Blade (Đoản Kiếm Hư Không)

NeoForge **1.21.1** (Java 21) mod that adds the **Ender Blade** — a netherite-tier void sword.

## Features

| Feature | Details |
|--------|---------|
| **Stats** | 8.0 attack damage · 1.6 attack speed · 2031 durability · Fire-resistant · Epic rarity |
| **Reach** | `+1.0` `ENTITY_INTERACTION_RANGE` on mainhand (3 → 4 blocks) |
| **Void Slash** | 20% on-hit chance to chaotic-teleport the target within 3 blocks, reset velocity, portal FX |
| **Ender Phantom** | Shift + RMB launches a piercing no-gravity phantom (2 s). Shift + RMB again swaps you to it (no pearl damage). 12 s cooldown. |

## Crafting

**Shapeless** (Crafting Table):

- `minecraft:netherite_sword`
- `minecraft:dragon_breath`
- `minecraft:eye_of_ender`

→ `enderblade:ender_blade`

A shaped alternate recipe (with echo shards) is also provided.

## Project layout

```
src/main/java/com/enderblade/
├── EnderBladeMod.java              # @Mod entrypoint
├── component/PhantomLink.java      # Data component (projectile UUID + expiry)
├── item/EnderBladeItem.java        # Sword logic (passive + active)
├── entity/EnderPhantomProjectile.java
├── client/EnderBladeClient.java    # ThrownItemRenderer registration
└── registry/
    ├── ModItems.java
    ├── ModEntities.java
    ├── ModDataComponents.java
    └── ModCreativeTabs.java
```

## Build / run

```bash
./gradlew build          # produce JAR in build/libs
./gradlew runClient      # launch Minecraft client with the mod
```

Requires **JDK 21**. NeoForge `21.1.250` is pinned in `gradle.properties`.

## ID

- Mod ID: `enderblade`
- Item: `enderblade:ender_blade`
- Entity: `enderblade:ender_phantom`
- Data component: `enderblade:phantom_link`

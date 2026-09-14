package com.enderblade.ability;

import com.enderblade.effect.VoidMarkEffect;
import com.enderblade.network.ModNetworking;
import com.enderblade.network.PlayAnimationPayload;
import com.enderblade.network.SpawnVfxPayload;
import com.enderblade.registry.ModParticles;
import com.enderblade.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.Optional;

/**
 * Server-authoritative spatial combat helpers for the Ender Blade.
 */
public final class AbilityHelper {

    public static final double VOID_SLASH_RADIUS = 3.0D;
    public static final float VOID_SLASH_CHANCE = 0.20f;
    public static final int PHANTOM_LIFETIME = 40;
    public static final int ECHO_COOLDOWN = 240;
    public static final int ANCHOR_COOLDOWN = 200;
    public static final int VOID_SLASH_COOLDOWN = 80;
    public static final int PARADOX_COOLDOWN = 160;
    public static final int ULTIMATE_COOLDOWN = 600;
    public static final int ULTIMATE_DURATION = 100; // 5s
    public static final float ECHO_DAMAGE = 5.0f;
    public static final float VOID_SLASH_DAMAGE = 8.0f;
    public static final float RIFT_COLLAPSE_DAMAGE = 10.0f;
    public static final float ULTIMATE_PULL_DAMAGE = 14.0f;

    private AbilityHelper() {
    }

    // ------------------------------------------------------------------ FX
    public static void broadcastAnim(ServerPlayer player, String animId) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,
                new PlayAnimationPayload(player.getId(), animId));
    }

    public static void broadcastVfx(ServerLevel level, Vec3 pos, String type, float scale) {
        PacketDistributor.sendToPlayersInDimension(level,
                new SpawnVfxPayload(type, pos.x, pos.y, pos.z, scale));
    }

    public static void spatialBurst(ServerLevel level, Vec3 pos) {
        // Supporting particles only — main look is mesh VFX
        level.sendParticles(ModParticles.RIFT_DUST.get(), pos.x, pos.y + 0.5, pos.z, 10, 0.3, 0.4, 0.3, 0.015);
        level.sendParticles(ModParticles.VOID_SPARK.get(), pos.x, pos.y + 0.4, pos.z, 6, 0.2, 0.3, 0.2, 0.03);
        broadcastVfx(level, pos, "spatial_burst", 1.0f);
    }

    public static void afterimageBurst(ServerLevel level, Vec3 pos) {
        level.sendParticles(ModParticles.AFTERIMAGE_MIST.get(), pos.x, pos.y + 1.0, pos.z, 8, 0.25, 0.5, 0.25, 0.01);
        broadcastVfx(level, pos, "afterimage", 1.0f);
    }

    // --------------------------------------------------------- Safe teleport
    public static boolean safeTeleport(LivingEntity entity, double x, double y, double z) {
        BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos(Mth.floor(x), Mth.floor(y), Mth.floor(z));
        // Snap down/up to free space
        if (!isSafe(entity, mut)) {
            // try slightly above
            for (int dy = 1; dy <= 3; dy++) {
                mut.setY(Mth.floor(y) + dy);
                if (isSafe(entity, mut)) {
                    y = mut.getY();
                    break;
                }
            }
        }
        if (!isSafe(entity, BlockPos.containing(x, y, z))) {
            return false;
        }
        entity.teleportTo(x, y, z);
        entity.setDeltaMovement(Vec3.ZERO);
        entity.hurtMarked = true;
        entity.fallDistance = 0.0f;
        entity.resetFallDistance();
        return true;
    }

    private static boolean isSafe(LivingEntity entity, BlockPos pos) {
        AABB box = entity.getBoundingBox().move(
                pos.getX() + 0.5 - entity.getX(),
                pos.getY() - entity.getY(),
                pos.getZ() + 0.5 - entity.getZ()
        );
        return entity.level().noCollision(entity, box) && !entity.level().containsAnyLiquid(box);
    }

    public static Optional<Vec3> findSafeNearby(ServerLevel level, LivingEntity entity,
                                                Vec3 origin, double radius, RandomSource random, int attempts) {
        float w = entity.getBbWidth();
        float h = entity.getBbHeight();
        for (int i = 0; i < attempts; i++) {
            double dx = (random.nextDouble() * 2 - 1) * radius;
            double dy = (random.nextDouble() * 2 - 1) * radius * 0.55;
            double dz = (random.nextDouble() * 2 - 1) * radius;
            double x = origin.x + dx;
            double y = Mth.clamp(origin.y + dy, level.getMinBuildHeight() + 1, level.getMaxBuildHeight() - 2);
            double z = origin.z + dz;

            BlockPos feet = BlockPos.containing(x, y, z);
            if (!level.getBlockState(feet).getCollisionShape(level, feet).isEmpty()) {
                y = feet.getY() + 1.0;
            }
            AABB box = new AABB(x - w / 2, y, z - w / 2, x + w / 2, y + h, z + w / 2);
            if (level.noCollision(entity, box) && !level.containsAnyLiquid(box)) {
                return Optional.of(new Vec3(x, y, z));
            }
        }
        return Optional.empty();
    }

    // ---------------------------------------------------- Passive displacement
    public static void tryVoidDisplace(ServerLevel level, LivingEntity target, LivingEntity attacker) {
        if (level.getRandom().nextFloat() >= VOID_SLASH_CHANCE) {
            return;
        }
        Vec3 origin = target.position();
        Optional<Vec3> dest = findSafeNearby(level, target, origin, VOID_SLASH_RADIUS, level.getRandom(), 18);
        if (dest.isEmpty()) {
            return;
        }
        // Ghost stretch VFX at origin
        spatialBurst(level, origin.add(0, target.getBbHeight() * 0.5, 0));
        level.playSound(null, origin.x, origin.y, origin.z,
                ModSounds.TELEPORT.get(), SoundSource.PLAYERS, 0.8f, 0.7f);

        safeTeleport(target, dest.get().x, dest.get().y, dest.get().z);
        // Kill momentum — interrupt combos
        target.setDeltaMovement(Vec3.ZERO);
        target.hurtMarked = true;

        spatialBurst(level, dest.get().add(0, target.getBbHeight() * 0.5, 0));
        level.playSound(null, dest.get().x, dest.get().y, dest.get().z,
                ModSounds.TELEPORT.get(), SoundSource.PLAYERS, 0.9f, 1.3f);

        VoidMarkEffect.applyOrStack(target, attacker);
    }

    // ----------------------------------------------------------- Rift Collapse
    public static void riftCollapse(ServerLevel level, LivingEntity center, LivingEntity attacker) {
        Vec3 c = center.position().add(0, center.getBbHeight() * 0.5, 0);
        broadcastVfx(level, c, "rift_collapse", 1.5f);
        level.playSound(null, c.x, c.y, c.z, ModSounds.RIFT_COLLAPSE.get(), SoundSource.PLAYERS, 1.2f, 0.85f);

        // Pull nearby living entities inward
        AABB area = center.getBoundingBox().inflate(4.0);
        List<LivingEntity> victims = level.getEntitiesOfClass(LivingEntity.class, area,
                e -> e.isAlive() && e != attacker && e != center);

        DamageSource src = attacker != null
                ? level.damageSources().indirectMagic(attacker, attacker)
                : level.damageSources().magic();

        // Damage center
        center.hurt(src, RIFT_COLLAPSE_DAMAGE);
        center.setDeltaMovement(Vec3.ZERO);

        for (LivingEntity v : victims) {
            Vec3 pull = c.subtract(v.position().add(0, v.getBbHeight() * 0.5, 0)).normalize().scale(0.55);
            v.setDeltaMovement(v.getDeltaMovement().add(pull));
            v.hurtMarked = true;
            v.hurt(src, RIFT_COLLAPSE_DAMAGE * 0.6f);
        }

        // Supporting particles only — collapse look is geometry-driven
        level.sendParticles(ModParticles.VOID_SPARK.get(), c.x, c.y, c.z, 16, 0.4, 0.4, 0.4, 0.05);
        level.sendParticles(ModParticles.RIFT_DUST.get(), c.x, c.y, c.z, 12, 0.35, 0.35, 0.35, 0.03);

        // Slight displace of center
        findSafeNearby(level, center, center.position(), 1.5, level.getRandom(), 8)
                .ifPresent(p -> safeTeleport(center, p.x, p.y, p.z));
    }

    // ------------------------------------------------------ Player data access
    public static PlayerBladeData data(Player player) {
        return player.getData(com.enderblade.registry.ModAttachments.BLADE_DATA);
    }

    public static void teleportPlayerNoDamage(ServerPlayer player, Vec3 dest) {
        Vec3 from = player.position();
        // Departure: vertical rift + afterimages (not Enderman particles)
        afterimageBurst(player.serverLevel(), from.add(0, 1, 0));
        broadcastVfx(player.serverLevel(), from.add(0, 1, 0), "teleport_depart", 1.0f);
        levelSoftSparks(player.serverLevel(), from.add(0, 1, 0));

        player.teleportTo(dest.x, dest.y, dest.z);
        player.setDeltaMovement(Vec3.ZERO);
        player.hurtMarked = true;
        player.fallDistance = 0;
        player.resetFallDistance();

        // Arrival: rift open + short shockwave ring
        afterimageBurst(player.serverLevel(), dest.add(0, 1, 0));
        broadcastVfx(player.serverLevel(), dest.add(0, 1, 0), "teleport_arrival", 1.25f);
        levelSoftSparks(player.serverLevel(), dest.add(0, 1, 0));
        player.serverLevel().playSound(null, dest.x, dest.y, dest.z,
                ModSounds.TELEPORT.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        broadcastAnim(player, "teleport");
    }

    private static void levelSoftSparks(ServerLevel level, Vec3 pos) {
        level.sendParticles(ModParticles.VOID_SPARK.get(), pos.x, pos.y, pos.z, 5, 0.15, 0.25, 0.15, 0.02);
        level.sendParticles(ModParticles.RIFT_DUST.get(), pos.x, pos.y, pos.z, 4, 0.12, 0.2, 0.12, 0.01);
    }

    /** Find a safe spot behind a target for Paradox Step. */
    public static Optional<Vec3> behindTarget(LivingEntity target, LivingEntity mover) {
        Vec3 back = target.getLookAngle().scale(-1.5);
        Vec3 dest = target.position().add(back.x, 0, back.z);
        if (mover.level() instanceof ServerLevel sl) {
            return findSafeNearby(sl, mover, dest, 0.8, sl.getRandom(), 10)
                    .or(() -> Optional.of(dest));
        }
        return Optional.of(dest);
    }
}

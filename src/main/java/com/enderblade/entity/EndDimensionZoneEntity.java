package com.enderblade.entity;

import com.enderblade.ability.AbilityHelper;
import com.enderblade.effect.VoidMarkEffect;
import com.enderblade.registry.ModEntities;
import com.enderblade.registry.ModParticles;
import com.enderblade.registry.ModSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;

/**
 * Ultimate zone — End Dimension domain for ~5 seconds, then mass Rift Collapse.
 */
public class EndDimensionZoneEntity extends Entity {

    private static final EntityDataAccessor<Integer> AGE =
            SynchedEntityData.defineId(EndDimensionZoneEntity.class, EntityDataSerializers.INT);

    public static final double RADIUS = 8.0;

    private UUID ownerUuid;
    private boolean collapsed;

    public EndDimensionZoneEntity(EntityType<? extends EndDimensionZoneEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
        setNoGravity(true);
    }

    public EndDimensionZoneEntity(Level level, Vec3 pos, UUID owner) {
        this(ModEntities.END_DIMENSION_ZONE.get(), level);
        setPos(pos.x, pos.y, pos.z);
        this.ownerUuid = owner;
    }

    public int getZoneAge() {
        return entityData.get(AGE);
    }

    public float getProgress() {
        return getZoneAge() / (float) AbilityHelper.ULTIMATE_DURATION;
    }

    @Override
    public void tick() {
        super.tick();
        int age = getZoneAge() + 1;
        entityData.set(AGE, age);

        if (level().isClientSide) {
            clientFx(age);
            return;
        }

        if (!(level() instanceof ServerLevel sl)) return;

        // Soft pull + mark tick every second
        if (age % 20 == 0 && age < AbilityHelper.ULTIMATE_DURATION) {
            LivingEntity owner = resolveOwner(sl);
            AABB area = getBoundingBox().inflate(RADIUS);
            List<LivingEntity> list = sl.getEntitiesOfClass(LivingEntity.class, area,
                    e -> e.isAlive() && (owner == null || !e.is(owner)));
            for (LivingEntity t : list) {
                Vec3 pull = position().subtract(t.position()).normalize().scale(0.08);
                t.setDeltaMovement(t.getDeltaMovement().add(pull));
                t.hurtMarked = true;
                if (owner != null && sl.getRandom().nextFloat() < 0.35f) {
                    VoidMarkEffect.applyOrStack(t, owner);
                }
            }
        }

        if (!collapsed && age >= AbilityHelper.ULTIMATE_DURATION) {
            collapsed = true;
            finishCollapse(sl);
            discard();
        }
    }

    private void clientFx(int age) {
        // Sparse supporting sparks only — mesh renderer owns the domain look
        if (age % 5 == 0) {
            double ang = age * 0.2;
            double r = RADIUS * 0.65;
            level().addParticle(ModParticles.VOID_SPARK.get(),
                    getX() + Math.cos(ang) * r,
                    getY() + 0.6 + Math.sin(age * 0.08) * 0.25,
                    getZ() + Math.sin(ang) * r,
                    0, 0.015, 0);
        }
    }

    private void finishCollapse(ServerLevel sl) {
        LivingEntity owner = resolveOwner(sl);
        Vec3 c = position().add(0, 1, 0);
        AbilityHelper.broadcastVfx(sl, c, "ultimate_collapse", 2.5f);
        sl.playSound(null, c.x, c.y, c.z, ModSounds.RIFT_COLLAPSE.get(), SoundSource.PLAYERS, 1.5f, 0.7f);
        sl.playSound(null, c.x, c.y, c.z, ModSounds.ULTIMATE.get(), SoundSource.PLAYERS, 0.8f, 0.5f);

        AABB area = getBoundingBox().inflate(RADIUS);
        List<LivingEntity> list = sl.getEntitiesOfClass(LivingEntity.class, area,
                e -> e.isAlive() && (owner == null || !e.is(owner)));

        DamageSource src = owner != null
                ? damageSources().indirectMagic(owner, owner)
                : damageSources().magic();

        for (LivingEntity t : list) {
            // Pull hard to center
            Vec3 dest = position();
            AbilityHelper.safeTeleport(t, dest.x + (sl.getRandom().nextDouble() - 0.5),
                    dest.y, dest.z + (sl.getRandom().nextDouble() - 0.5));
            t.hurt(src, AbilityHelper.ULTIMATE_PULL_DAMAGE);
            t.removeEffect(com.enderblade.registry.ModEffects.VOID_MARK);
            AbilityHelper.spatialBurst(sl, t.position().add(0, 1, 0));
        }

        // Geometry collapse VFX already broadcast — light particle support only
        sl.sendParticles(ModParticles.RIFT_DUST.get(), c.x, c.y, c.z, 24, 1.0, 0.7, 1.0, 0.04);
        sl.sendParticles(ModParticles.VOID_SPARK.get(), c.x, c.y, c.z, 18, 0.8, 0.6, 0.8, 0.06);
    }

    private LivingEntity resolveOwner(ServerLevel sl) {
        if (ownerUuid == null) return null;
        Entity e = sl.getEntity(ownerUuid);
        return e instanceof LivingEntity le ? le : null;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(AGE, 0);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("Owner")) ownerUuid = tag.getUUID("Owner");
        entityData.set(AGE, tag.getInt("Age"));
        collapsed = tag.getBoolean("Collapsed");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (ownerUuid != null) tag.putUUID("Owner", ownerUuid);
        tag.putInt("Age", getZoneAge());
        tag.putBoolean("Collapsed", collapsed);
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }
}

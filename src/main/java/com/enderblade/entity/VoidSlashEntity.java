package com.enderblade.entity;

import com.enderblade.ability.AbilityHelper;
import com.enderblade.effect.VoidMarkEffect;
import com.enderblade.registry.ModEntities;
import com.enderblade.registry.ModParticles;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Dimensional cut rift several blocks in front of the player.
 * Damages + marks entities intersecting its plane, then snaps shut.
 */
public class VoidSlashEntity extends Entity {

    private static final EntityDataAccessor<Float> YAW =
            SynchedEntityData.defineId(VoidSlashEntity.class, EntityDataSerializers.FLOAT);

    public static final int LIFE = 12; // ticks visible
    private static final double REACH = 4.5;
    private static final double WIDTH = 2.2;
    private static final double HEIGHT = 2.0;

    private UUID ownerUuid;
    private int age;
    private final Set<Integer> hit = new HashSet<>();
    private boolean damaged;

    public VoidSlashEntity(EntityType<? extends VoidSlashEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
        setNoGravity(true);
    }

    public VoidSlashEntity(Level level, LivingEntity owner) {
        this(ModEntities.VOID_SLASH.get(), level);
        this.ownerUuid = owner.getUUID();
        Vec3 look = owner.getLookAngle();
        Vec3 origin = owner.getEyePosition().add(look.scale(2.2));
        setPos(origin.x, origin.y - 0.5, origin.z);
        setYRot(owner.getYRot());
        entityData.set(YAW, owner.getYRot());
    }

    public float getSlashYaw() {
        return entityData.get(YAW);
    }

    public int getAge() {
        return age;
    }

    public float getLifeProgress() {
        return age / (float) LIFE;
    }

    @Override
    public void tick() {
        super.tick();
        age++;

        if (!level().isClientSide && !damaged && age == 2) {
            doDamage();
            damaged = true;
        }

        if (level().isClientSide && age % 2 == 0) {
            float yaw = getSlashYaw() * MthDeg();
            for (int i = 0; i < 4; i++) {
                double ox = (random.nextDouble() - 0.5) * WIDTH;
                double oy = (random.nextDouble() - 0.5) * HEIGHT;
                double lx = -Math.sin(yaw) * 0.1;
                double lz = Math.cos(yaw) * 0.1;
                level().addParticle(ModParticles.VOID_SPARK.get(),
                        getX() + ox * Math.cos(yaw), getY() + oy, getZ() + ox * Math.sin(yaw),
                        lx, 0, lz);
            }
        }

        if (!level().isClientSide && age >= LIFE) {
            discard();
        }
    }

    private float MthDeg() {
        return (float) Math.toRadians(getSlashYaw());
    }

    private void doDamage() {
        if (!(level() instanceof ServerLevel sl)) return;

        float yawRad = (float) Math.toRadians(getSlashYaw());
        Vec3 forward = new Vec3(-Math.sin(yawRad), 0, Math.cos(yawRad));
        Vec3 right = new Vec3(forward.z, 0, -forward.x);

        // AABB covering the slash plane volume
        Vec3 center = position().add(0, HEIGHT * 0.5, 0);
        AABB box = new AABB(center, center).inflate(WIDTH * 0.6, HEIGHT * 0.6, REACH * 0.35);

        final LivingEntity owner;
        if (ownerUuid != null) {
            Entity e = sl.getEntity(ownerUuid);
            owner = e instanceof LivingEntity le ? le : null;
        } else {
            owner = null;
        }

        List<LivingEntity> targets = sl.getEntitiesOfClass(LivingEntity.class, box,
                e -> e.isAlive() && (owner == null || e != owner) && !hit.contains(e.getId()));

        DamageSource src = owner != null
                ? damageSources().indirectMagic(this, owner)
                : damageSources().magic();

        for (LivingEntity t : targets) {
            // Project onto slash plane — must be near the plane
            Vec3 rel = t.position().add(0, t.getBbHeight() * 0.5, 0).subtract(center);
            double alongRight = rel.dot(right);
            double alongFwd = rel.dot(forward);
            if (Math.abs(alongRight) > WIDTH * 0.55) continue;
            if (alongFwd < -0.5 || alongFwd > REACH * 0.5) continue;

            hit.add(t.getId());
            t.hurt(src, AbilityHelper.VOID_SLASH_DAMAGE);
            if (owner != null) {
                VoidMarkEffect.applyOrStack(t, owner);
            }
            AbilityHelper.spatialBurst(sl, t.position().add(0, t.getBbHeight() * 0.5, 0));
        }

        AbilityHelper.broadcastVfx(sl, center, "void_slash", 1.4f);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(YAW, 0f);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("Owner")) ownerUuid = tag.getUUID("Owner");
        age = tag.getInt("Age");
        if (tag.contains("Yaw")) entityData.set(YAW, tag.getFloat("Yaw"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (ownerUuid != null) tag.putUUID("Owner", ownerUuid);
        tag.putInt("Age", age);
        tag.putFloat("Yaw", getSlashYaw());
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

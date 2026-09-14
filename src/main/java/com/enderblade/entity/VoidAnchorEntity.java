package com.enderblade.entity;

import com.enderblade.ability.AbilityHelper;
import com.enderblade.registry.ModEntities;
import com.enderblade.registry.ModParticles;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

/**
 * Floating Void Anchor — Eye of Ender core + rotating ring.
 * Player recalls here via Shift+RMB.
 */
public class VoidAnchorEntity extends Entity {

    public static final int MAX_LIFE = 600; // 30s max

    private UUID ownerUuid;
    private int age;

    public VoidAnchorEntity(EntityType<? extends VoidAnchorEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
        setNoGravity(true);
    }

    public VoidAnchorEntity(Level level, Vec3 pos, UUID owner) {
        this(ModEntities.VOID_ANCHOR.get(), level);
        setPos(pos.x, pos.y, pos.z);
        this.ownerUuid = owner;
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public void setOwnerUuid(UUID uuid) {
        this.ownerUuid = uuid;
    }

    @Override
    public void tick() {
        super.tick();
        age++;

        // Gentle hover bob
        setPos(getX(), getY() + Math.sin(age * 0.08) * 0.005, getZ());

        if (level().isClientSide && age % 6 == 0) {
            // Sparse custom spark only — renderer owns the look
            level().addParticle(ModParticles.VOID_SPARK.get(),
                    getX() + (random.nextDouble() - 0.5) * 0.3,
                    getY() + 0.4 + random.nextDouble() * 0.2,
                    getZ() + (random.nextDouble() - 0.5) * 0.3,
                    0, 0.015, 0);
        }

        if (!level().isClientSide && age >= MAX_LIFE) {
            if (level() instanceof ServerLevel sl) {
                AbilityHelper.spatialBurst(sl, position().add(0, 0.5, 0));
            }
            discard();
        }
    }

    /** Called when player recalls to this anchor. */
    public void activateAndConsume() {
        if (level() instanceof ServerLevel sl) {
            AbilityHelper.broadcastVfx(sl, position().add(0, 0.5, 0), "anchor_recall", 1.3f);
            AbilityHelper.spatialBurst(sl, position().add(0, 0.5, 0));
        }
        discard();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("Owner")) ownerUuid = tag.getUUID("Owner");
        age = tag.getInt("Age");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (ownerUuid != null) tag.putUUID("Owner", ownerUuid);
        tag.putInt("Age", age);
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

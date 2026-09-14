package com.enderblade.entity;

import com.enderblade.ability.AbilityHelper;
import com.enderblade.effect.VoidMarkEffect;
import com.enderblade.item.EnderBladeItem;
import com.enderblade.registry.ModEntities;
import com.enderblade.registry.ModItems;
import com.enderblade.registry.ModParticles;
import com.enderblade.registry.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.HashSet;
import java.util.Set;

/**
 * Ender Echo — piercing void orb. Custom 3D renderer on client.
 * Passes through living entities dealing void damage + Void Mark.
 */
public class EnderEchoProjectile extends ThrowableItemProjectile {

    private int lifetime = AbilityHelper.PHANTOM_LIFETIME;
    private int age;
    private final Set<Integer> hitIds = new HashSet<>();

    public EnderEchoProjectile(EntityType<? extends EnderEchoProjectile> type, Level level) {
        super(type, level);
        setNoGravity(true);
        setItem(new ItemStack(Items.ENDER_EYE));
    }

    public EnderEchoProjectile(Level level, LivingEntity owner) {
        super(ModEntities.ENDER_ECHO.get(), owner, level);
        setNoGravity(true);
        setOwner(owner);
        setItem(new ItemStack(Items.ENDER_EYE));
    }

    public void setLifetime(int ticks) {
        this.lifetime = ticks;
    }

    public int getAge() {
        return age;
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.ENDER_BLADE.get();
    }

    @Override
    public void tick() {
        super.tick();
        age++;

        Level level = level();
        if (level.isClientSide) {
            level.addParticle(ModParticles.VOID_SPARK.get(), getX(), getY(), getZ(), 0, 0, 0);
            if (age % 2 == 0) {
                level.addParticle(ParticleTypes.REVERSE_PORTAL, getX(), getY(), getZ(),
                        (random.nextDouble() - 0.5) * 0.08,
                        (random.nextDouble() - 0.5) * 0.08,
                        (random.nextDouble() - 0.5) * 0.08);
            }
        } else if (level instanceof ServerLevel sl && age % 2 == 0) {
            sl.sendParticles(ModParticles.RIFT_DUST.get(), getX(), getY(), getZ(), 1, 0.05, 0.05, 0.05, 0);
        }

        if (!level.isClientSide && age >= lifetime) {
            expire();
        }
    }

    private void expire() {
        if (level() instanceof ServerLevel sl) {
            Entity owner = getOwner();
            if (owner != null) {
                EnderBladeItem.onEchoExpired(sl, owner.getUUID(), getUUID());
            }
            sl.sendParticles(ParticleTypes.PORTAL, getX(), getY(), getZ(), 16, 0.25, 0.25, 0.25, 0.08);
            sl.playSound(null, getX(), getY(), getZ(), ModSounds.ECHO_LAUNCH.get(), SoundSource.NEUTRAL, 0.4f, 1.6f);
        }
        discard();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity hit = result.getEntity();
        if (level().isClientSide) return;

        Entity owner = getOwner();
        if (owner != null && hit.is(owner)) return;

        if (hit instanceof LivingEntity living && hitIds.add(hit.getId())) {
            DamageSource magic = owner instanceof LivingEntity lo
                    ? damageSources().indirectMagic(this, lo)
                    : damageSources().magic();
            living.hurt(magic, AbilityHelper.ECHO_DAMAGE);

            if (owner instanceof LivingEntity lo) {
                VoidMarkEffect.applyOrStack(living, lo);
            }

            if (level() instanceof ServerLevel sl) {
                AbilityHelper.broadcastVfx(sl, living.position().add(0, living.getBbHeight() * 0.5, 0),
                        "spatial_cut", 0.7f);
                sl.sendParticles(ModParticles.VOID_SPARK.get(),
                        living.getX(), living.getY() + living.getBbHeight() * 0.5, living.getZ(),
                        10, 0.2, 0.3, 0.2, 0.04);
                sl.playSound(null, living.getX(), living.getY(), living.getZ(),
                        ModSounds.SLASH.get(), SoundSource.NEUTRAL, 0.5f, 1.4f);
            }
        }
        // pierce — do not discard
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!level().isClientSide) {
            if (level() instanceof ServerLevel sl) {
                Entity owner = getOwner();
                if (owner != null) {
                    EnderBladeItem.onEchoExpired(sl, owner.getUUID(), getUUID());
                }
                AbilityHelper.spatialBurst(sl, position());
            }
            discard();
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        if (hitIds.contains(target.getId())) return false;
        Entity owner = getOwner();
        if (owner != null && target.is(owner)) return false;
        return super.canHitEntity(target);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Lifetime", lifetime);
        tag.putInt("Age", age);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Lifetime")) lifetime = tag.getInt("Lifetime");
        if (tag.contains("Age")) age = tag.getInt("Age");
        setNoGravity(true);
    }

    @Override
    protected double getDefaultGravity() {
        return 0.0;
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

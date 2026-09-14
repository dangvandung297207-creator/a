package com.enderblade.entity;

import com.enderblade.item.EnderBladeItem;
import com.enderblade.registry.ModEntities;
import com.enderblade.registry.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
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
 * Ender Phantom — a gravity-free piercing projectile launched by the Ender Blade.
 *
 * <ul>
 *   <li>Leaves a trail of {@link ParticleTypes#DRAGON_BREATH} / {@link ParticleTypes#REVERSE_PORTAL}.</li>
 *   <li>Pierces living entities, dealing magic damage, instead of breaking on first contact.</li>
 *   <li>Expires after {@link EnderBladeItem#PHANTOM_LIFETIME_TICKS} ticks, triggering item cooldown.</li>
 *   <li>Can be recalled (player teleports to it) via a second Shift+RMB on the blade.</li>
 * </ul>
 */
public class EnderPhantomProjectile extends ThrowableItemProjectile {

    /** Magic damage dealt when piercing a living entity. */
    public static final float PIERCE_DAMAGE = 4.0f;

    private int lifetimeTicks = EnderBladeItem.PHANTOM_LIFETIME_TICKS;
    private int age;
    /** Entity ids already damaged by this phantom (one hit each). */
    private final Set<Integer> hitEntityIds = new HashSet<>();

    // ---- Constructors -------------------------------------------------------

    public EnderPhantomProjectile(EntityType<? extends EnderPhantomProjectile> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.setItem(new ItemStack(Items.ENDER_EYE));
    }

    public EnderPhantomProjectile(EntityType<? extends EnderPhantomProjectile> type, Level level, LivingEntity owner) {
        super(type, owner, level);
        this.setNoGravity(true);
        this.setOwner(owner);
        this.setItem(new ItemStack(Items.ENDER_EYE));
    }

    public EnderPhantomProjectile(Level level, LivingEntity owner) {
        this(ModEntities.ENDER_PHANTOM.get(), level, owner);
    }

    // ---- Configuration ------------------------------------------------------

    public void setLifetimeTicks(int ticks) {
        this.lifetimeTicks = ticks;
    }

    public int getLifetimeTicks() {
        return this.lifetimeTicks;
    }

    public int getAge() {
        return this.age;
    }

    // ---- Item appearance (ThrownItemRenderer) -------------------------------

    @Override
    protected Item getDefaultItem() {
        // Identity fallback; visual stack is an Ender Eye set via setItem().
        return ModItems.ENDER_BLADE.get();
    }

    // ---- Tick ---------------------------------------------------------------

    @Override
    public void tick() {
        super.tick();
        this.age++;

        Level level = this.level();

        if (level.isClientSide) {
            spawnTrailParticles();
        } else if (level instanceof ServerLevel serverLevel && this.age % 2 == 0) {
            serverLevel.sendParticles(ParticleTypes.DRAGON_BREATH,
                    this.getX(), this.getY(), this.getZ(),
                    1, 0.05, 0.05, 0.05, 0.0);
        }

        if (!level.isClientSide && this.age >= this.lifetimeTicks) {
            expireAndNotifyOwner();
        }
    }

    private void spawnTrailParticles() {
        Level level = this.level();
        level.addParticle(ParticleTypes.DRAGON_BREATH,
                this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
        if (this.age % 2 == 0) {
            level.addParticle(ParticleTypes.REVERSE_PORTAL,
                    this.getX(), this.getY(), this.getZ(),
                    (this.random.nextDouble() - 0.5) * 0.1,
                    (this.random.nextDouble() - 0.5) * 0.1,
                    (this.random.nextDouble() - 0.5) * 0.1);
        }
    }

    /** Timeout: discard and start the blade's 12 s cooldown for the owner. */
    private void expireAndNotifyOwner() {
        if (this.level() instanceof ServerLevel serverLevel) {
            Entity owner = this.getOwner();
            if (owner != null) {
                EnderBladeItem.onPhantomExpired(serverLevel, owner.getUUID(), this.getUUID());
            }
            serverLevel.sendParticles(ParticleTypes.PORTAL,
                    this.getX(), this.getY(), this.getZ(),
                    20, 0.3, 0.3, 0.3, 0.1);
            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.ENDER_EYE_DEATH, SoundSource.NEUTRAL, 0.7f, 1.2f);
        }
        this.discard();
    }

    // ---- Collision ----------------------------------------------------------

    /**
     * Pierces living entities: deal magic damage once per target, keep flying.
     * Does not discard the projectile.
     */
    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity hit = result.getEntity();
        if (this.level().isClientSide) {
            return;
        }

        Entity owner = this.getOwner();
        if (owner != null && hit.is(owner)) {
            return;
        }

        if (hit instanceof LivingEntity living && this.hitEntityIds.add(hit.getId())) {
            DamageSource magic = this.damageSources().magic();
            if (owner instanceof LivingEntity livingOwner) {
                magic = this.damageSources().indirectMagic(this, livingOwner);
            }
            living.hurt(magic, PIERCE_DAMAGE);

            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.REVERSE_PORTAL,
                        hit.getX(), hit.getY() + hit.getBbHeight() * 0.5, hit.getZ(),
                        12, 0.25, 0.35, 0.25, 0.05);
                serverLevel.playSound(null, hit.getX(), hit.getY(), hit.getZ(),
                        SoundEvents.ENDERMAN_HURT, SoundSource.NEUTRAL, 0.4f, 1.5f);
            }
        }
    }

    /**
     * Solid block contact ends the phantom early and starts the item cooldown.
     */
    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!this.level().isClientSide) {
            if (this.level() instanceof ServerLevel serverLevel) {
                Entity owner = this.getOwner();
                if (owner != null) {
                    EnderBladeItem.onPhantomExpired(serverLevel, owner.getUUID(), this.getUUID());
                }
                serverLevel.sendParticles(ParticleTypes.PORTAL,
                        this.getX(), this.getY(), this.getZ(),
                        16, 0.25, 0.25, 0.25, 0.08);
            }
            this.discard();
        }
    }

    @Override
    protected void onHit(HitResult result) {
        // Typed handlers only — do not discard here so entity piercing works.
        super.onHit(result);
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        if (this.hitEntityIds.contains(target.getId())) {
            return false;
        }
        Entity owner = this.getOwner();
        if (owner != null && target.is(owner)) {
            return false;
        }
        return super.canHitEntity(target);
    }

    // ---- Persistence --------------------------------------------------------

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Lifetime", this.lifetimeTicks);
        tag.putInt("Age", this.age);
        tag.putIntArray("HitEntities", this.hitEntityIds.stream().mapToInt(Integer::intValue).toArray());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Lifetime")) {
            this.lifetimeTicks = tag.getInt("Lifetime");
        }
        if (tag.contains("Age")) {
            this.age = tag.getInt("Age");
        }
        if (tag.contains("HitEntities")) {
            this.hitEntityIds.clear();
            for (int id : tag.getIntArray("HitEntities")) {
                this.hitEntityIds.add(id);
            }
        }
        this.setNoGravity(true);
    }

    // ---- Misc ---------------------------------------------------------------

    @Override
    protected double getDefaultGravity() {
        return 0.0D;
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

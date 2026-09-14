package com.enderblade.item;

import com.enderblade.component.PhantomLink;
import com.enderblade.entity.EnderPhantomProjectile;
import com.enderblade.registry.ModDataComponents;
import com.enderblade.registry.ModEntities;
import com.enderblade.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Đoản Kiếm Hư Không (Ender Blade).
 *
 * <h2>Passive — Void Slash</h2>
 * 20% chance on hit to randomly teleport the target within a 3-block radius,
 * reset their velocity, and play enderman teleport FX.
 *
 * <h2>Active — Ender Phantom (Shift + Right Click)</h2>
 * <ul>
 *   <li>First press: launch a piercing {@link EnderPhantomProjectile} (40 tick lifetime).</li>
 *   <li>Second press (while alive): teleport the player to the phantom without pearl damage.</li>
 *   <li>Timeout / after swap: 240 tick (12 s) item cooldown.</li>
 * </ul>
 */
public class EnderBladeItem extends SwordItem {

    /** Chance to trigger chaotic teleport on hit. */
    public static final float VOID_SLASH_CHANCE = 0.20f;
    /** Radius (blocks) for chaotic target teleport. */
    public static final double VOID_SLASH_RADIUS = 3.0D;
    /** Phantom projectile lifetime in ticks (2 seconds). */
    public static final int PHANTOM_LIFETIME_TICKS = 40;
    /** Item cooldown after ability resolves (12 seconds). */
    public static final int ABILITY_COOLDOWN_TICKS = 240;
    /** Launch speed of the phantom projectile. */
    public static final float PHANTOM_LAUNCH_SPEED = 1.75f;

    public EnderBladeItem(Tier tier, Item.Properties properties) {
        super(tier, properties);
    }

    // -------------------------------------------------------------------------
    // Passive: Void Slash
    // -------------------------------------------------------------------------

    /**
     * Called when this weapon successfully damages a living entity.
     * Implements the 20% chaotic teleport passive.
     */
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean result = super.hurtEnemy(stack, target, attacker);

        Level level = target.level();
        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            RandomSource random = level.getRandom();
            if (random.nextFloat() < VOID_SLASH_CHANCE) {
                applyVoidSlash(serverLevel, target, random);
            }
        }

        return result;
    }

    /**
     * Teleports {@code target} to a safe random position within {@link #VOID_SLASH_RADIUS},
     * zeroes their delta movement, and plays portal particles / enderman teleport sound
     * at both the old and new locations.
     */
    public static void applyVoidSlash(ServerLevel level, LivingEntity target, RandomSource random) {
        Vec3 origin = target.position();

        Optional<Vec3> destination = findSafeTeleportPos(level, target, origin, VOID_SLASH_RADIUS, random, 16);
        if (destination.isEmpty()) {
            return;
        }

        Vec3 dest = destination.get();

        // FX at old position
        spawnPortalBurst(level, origin);
        level.playSound(null, origin.x, origin.y, origin.z,
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.0f);

        // Perform teleport and kill momentum
        target.teleportTo(dest.x, dest.y, dest.z);
        target.setDeltaMovement(Vec3.ZERO);
        target.hurtMarked = true;
        target.fallDistance = 0.0f;

        // FX at new position
        spawnPortalBurst(level, dest);
        level.playSound(null, dest.x, dest.y, dest.z,
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.15f);
    }

    // -------------------------------------------------------------------------
    // Active: Ender Phantom launch / swap
    // -------------------------------------------------------------------------

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Ability requires sneaking (Shift)
        if (!player.isShiftKeyDown()) {
            return InteractionResultHolder.pass(stack);
        }

        // Respect item cooldown
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        if (level.isClientSide) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResultHolder.pass(stack);
        }

        PhantomLink link = stack.getOrDefault(ModDataComponents.PHANTOM_LINK.get(), PhantomLink.EMPTY);
        long gameTime = serverLevel.getGameTime();

        // --- Second press: swap to existing phantom ---
        if (link.isActive(gameTime)) {
            Entity phantom = serverLevel.getEntity(link.projectileId().orElseThrow());
            if (phantom instanceof EnderPhantomProjectile livingPhantom && livingPhantom.isAlive()) {
                performPhantomSwap(serverLevel, player, livingPhantom, stack);
                return InteractionResultHolder.sidedSuccess(stack, false);
            }
            // Phantom already gone — clear stale link and fall through to launch
            stack.set(ModDataComponents.PHANTOM_LINK.get(), PhantomLink.EMPTY);
        }

        // --- First press: launch phantom ---
        launchPhantom(serverLevel, player, stack);
        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    /**
     * Spawns an {@link EnderPhantomProjectile}, records its UUID on the item via
     * {@link PhantomLink}, and plays a soft launch cue.
     */
    private void launchPhantom(ServerLevel level, Player player, ItemStack stack) {
        EnderPhantomProjectile phantom = new EnderPhantomProjectile(
                ModEntities.ENDER_PHANTOM.get(), level, player);

        Vec3 eye = player.getEyePosition();
        phantom.setPos(eye.x, eye.y, eye.z);
        phantom.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0f,
                PHANTOM_LAUNCH_SPEED, 0.0f);
        phantom.setNoGravity(true);
        phantom.setLifetimeTicks(PHANTOM_LIFETIME_TICKS);

        level.addFreshEntity(phantom);

        long expireAt = level.getGameTime() + PHANTOM_LIFETIME_TICKS;
        stack.set(ModDataComponents.PHANTOM_LINK.get(), PhantomLink.of(phantom.getUUID(), expireAt));

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENDER_EYE_LAUNCH, SoundSource.PLAYERS, 0.8f, 1.4f);

        // Small dragon-breath puff at the player's eyes
        level.sendParticles(ParticleTypes.DRAGON_BREATH,
                eye.x, eye.y, eye.z, 8, 0.15, 0.15, 0.15, 0.01);
    }

    /**
     * Teleports the player to the phantom's current position without pearl fall damage,
     * discards the projectile, applies the 12 s cooldown, and plays chorus-fruit FX.
     */
    private void performPhantomSwap(ServerLevel level, Player player,
                                    EnderPhantomProjectile phantom, ItemStack stack) {
        Vec3 dest = phantom.position();
        Vec3 origin = player.position();

        // Discard phantom first so its tick won't race us
        phantom.discard();
        stack.set(ModDataComponents.PHANTOM_LINK.get(), PhantomLink.EMPTY);

        // Safe teleport — no ender-pearl damage
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.teleportTo(dest.x, dest.y, dest.z);
        } else {
            player.teleportTo(dest.x, dest.y, dest.z);
        }
        player.setDeltaMovement(Vec3.ZERO);
        player.hurtMarked = true;
        player.fallDistance = 0.0f;
        player.resetFallDistance();

        // Cooldown
        player.getCooldowns().addCooldown(this, ABILITY_COOLDOWN_TICKS);

        // Wide portal burst + chorus fruit sound at landing
        spawnWidePortalBurst(level, dest);
        level.playSound(null, dest.x, dest.y, dest.z,
                SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.0f);

        // Also a small burst at the departure point
        spawnPortalBurst(level, origin);
    }

    /**
     * Called by the phantom when it times out so the owning stack can start cooldown
     * and clear its data component.
     */
    public static void onPhantomExpired(ServerLevel level, UUID ownerUuid, UUID phantomUuid) {
        Player player = level.getPlayerByUUID(ownerUuid);
        if (player == null) {
            return;
        }

        // Search mainhand / offhand for a blade still linked to this phantom
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (!stack.is(ModItems.ENDER_BLADE.get())) {
                continue;
            }
            PhantomLink link = stack.getOrDefault(ModDataComponents.PHANTOM_LINK.get(), PhantomLink.EMPTY);
            if (link.projectileId().isPresent() && link.projectileId().get().equals(phantomUuid)) {
                stack.set(ModDataComponents.PHANTOM_LINK.get(), PhantomLink.EMPTY);
                player.getCooldowns().addCooldown(ModItems.ENDER_BLADE.get(), ABILITY_COOLDOWN_TICKS);
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.5f, 1.6f);
                return;
            }
        }
    }

    // -------------------------------------------------------------------------
    // Tooltip
    // -------------------------------------------------------------------------

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.enderblade.ender_blade.desc.passive")
                .withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.add(Component.translatable("item.enderblade.ender_blade.desc.active")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("item.enderblade.ender_blade.desc.reach")
                .withStyle(ChatFormatting.GRAY));
    }

    // -------------------------------------------------------------------------
    // Utility helpers
    // -------------------------------------------------------------------------

    /**
     * Attempts to find a collision-safe landing spot near {@code origin} within {@code radius}.
     * The target's bounding-box dimensions are respected so large mobs don't clip into walls.
     */
    public static Optional<Vec3> findSafeTeleportPos(ServerLevel level, LivingEntity entity,
                                                     Vec3 origin, double radius,
                                                     RandomSource random, int attempts) {
        float width = entity.getBbWidth();
        float height = entity.getBbHeight();

        for (int i = 0; i < attempts; i++) {
            double dx = (random.nextDouble() * 2.0D - 1.0D) * radius;
            double dy = (random.nextDouble() * 2.0D - 1.0D) * radius * 0.6D;
            double dz = (random.nextDouble() * 2.0D - 1.0D) * radius;

            double x = origin.x + dx;
            double y = Mth.clamp(origin.y + dy, level.getMinBuildHeight() + 1, level.getMaxBuildHeight() - 2);
            double z = origin.z + dz;

            // Snap slightly upward if standing inside a solid block
            BlockPos feet = BlockPos.containing(x, y, z);
            BlockState feetState = level.getBlockState(feet);
            if (!feetState.getCollisionShape(level, feet).isEmpty()) {
                y = feet.getY() + 1.0D;
            }

            AABB box = new AABB(
                    x - width / 2.0D, y, z - width / 2.0D,
                    x + width / 2.0D, y + height, z + width / 2.0D
            );

            if (level.noCollision(entity, box) && !level.containsAnyLiquid(box)) {
                return Optional.of(new Vec3(x, y, z));
            }
        }
        return Optional.empty();
    }

    public static void spawnPortalBurst(ServerLevel level, Vec3 pos) {
        level.sendParticles(ParticleTypes.PORTAL,
                pos.x, pos.y + 0.5D, pos.z,
                32, 0.35, 0.6, 0.35, 0.15);
    }

    public static void spawnWidePortalBurst(ServerLevel level, Vec3 pos) {
        level.sendParticles(ParticleTypes.PORTAL,
                pos.x, pos.y + 0.8D, pos.z,
                80, 0.8, 1.0, 0.8, 0.25);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL,
                pos.x, pos.y + 0.5D, pos.z,
                24, 0.4, 0.5, 0.4, 0.05);
    }
}

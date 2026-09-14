package com.enderblade.item;

import com.enderblade.ability.AbilityHelper;
import com.enderblade.ability.PlayerBladeData;
import com.enderblade.component.AnchorLink;
import com.enderblade.component.PhantomLink;
import com.enderblade.entity.EndDimensionZoneEntity;
import com.enderblade.entity.EnderEchoProjectile;
import com.enderblade.entity.VoidAnchorEntity;
import com.enderblade.entity.VoidSlashEntity;
import com.enderblade.registry.ModDataComponents;
import com.enderblade.registry.ModItems;
import com.enderblade.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
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
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Đoản Kiếm Hư Không — full ability suite.
 *
 * <ul>
 *   <li>LMB combo 1-2-3 + passive Chém Xuyên Không</li>
 *   <li>RMB — Ender Echo / teleport recall</li>
 *   <li>Shift+RMB — Void Anchor place / recall</li>
 *   <li>Shift+LMB (hurtEnemy path) heavy handled via combo</li>
 * </ul>
 *
 * Hotkeys for advanced abilities (Void Slash / Paradox / Ultimate) are
 * handled via {@link com.enderblade.event.ModEvents} key packet + sneak combos:
 * <ul>
 *   <li>Sneak + Sprint + RMB = Ultimate</li>
 *   <li>Sneak + Attack key binding via secondary use — Void Slash on cooldown item use while sprinting</li>
 * </ul>
 */
public class EnderBladeItem extends SwordItem {

    public EnderBladeItem(Tier tier, Item.Properties properties) {
        super(tier, properties);
    }

    // =====================================================================
    // Passive
    // =====================================================================

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean result = super.hurtEnemy(stack, target, attacker);

        if (!attacker.level().isClientSide && attacker.level() instanceof ServerLevel server) {
            // Combo tracking + anim
            if (attacker instanceof ServerPlayer sp) {
                PlayerBladeData data = AbilityHelper.data(sp);
                int hit = data.nextCombo(server.getGameTime());
                String anim = switch (hit) {
                    case 0 -> "attack1";
                    case 1 -> "attack2";
                    default -> "attack3";
                };
                AbilityHelper.broadcastAnim(sp, anim);
                server.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(),
                        hit == 2 ? ModSounds.HEAVY_SLASH.get() : ModSounds.SLASH.get(),
                        SoundSource.PLAYERS, 0.9f, 0.95f + hit * 0.08f);

                // Open paradox window briefly after a successful hit
                data.paradoxWindowUntil = server.getGameTime() + 30;
            }

            AbilityHelper.tryVoidDisplace(server, target, attacker);
        }
        return result;
    }

    // =====================================================================
    // Use — Echo / Anchor / Ultimate / Void Slash routing
    // =====================================================================

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }
        if (!(level instanceof ServerLevel server) || !(player instanceof ServerPlayer sp)) {
            return InteractionResultHolder.pass(stack);
        }

        long now = server.getGameTime();
        PlayerBladeData data = AbilityHelper.data(sp);

        // ---- Ultimate: Sneak + Sprint + RMB ----
        if (player.isShiftKeyDown() && player.isSprinting()) {
            return tryUltimate(server, sp, stack, data, now);
        }

        // ---- Void Slash: Sneak only while on ground looking down-ish? Use secondary: sneak + not sprint ----
        // Spec: Shift+RMB = Void Anchor. Plain RMB = Echo.
        // Void Slash / Paradox activated via dedicated methods (also called from network/key).
        if (player.isShiftKeyDown()) {
            return tryVoidAnchor(server, sp, stack, data, now);
        }

        return tryEnderEcho(server, sp, stack, data, now);
    }

    // =====================================================================
    // Ender Echo
    // =====================================================================

    private InteractionResultHolder<ItemStack> tryEnderEcho(ServerLevel level, ServerPlayer player,
                                                            ItemStack stack, PlayerBladeData data, long now) {
        PhantomLink link = stack.getOrDefault(ModDataComponents.PHANTOM_LINK.get(), PhantomLink.EMPTY);

        // Second press — teleport to living echo
        if (link.isActive(now)) {
            Entity echo = level.getEntity(link.projectileId().orElseThrow());
            if (echo instanceof EnderEchoProjectile proj && proj.isAlive()) {
                Vec3 dest = proj.position();
                proj.discard();
                stack.set(ModDataComponents.PHANTOM_LINK.get(), PhantomLink.EMPTY);
                AbilityHelper.teleportPlayerNoDamage(player, dest);
                data.echoCooldownUntil = now + AbilityHelper.ECHO_COOLDOWN;
                player.getCooldowns().addCooldown(this, AbilityHelper.ECHO_COOLDOWN);
                return InteractionResultHolder.sidedSuccess(stack, false);
            }
            stack.set(ModDataComponents.PHANTOM_LINK.get(), PhantomLink.EMPTY);
        }

        if (data.isOnCooldown(now, data.echoCooldownUntil) || player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        // Launch
        EnderEchoProjectile echo = new EnderEchoProjectile(level, player);
        Vec3 eye = player.getEyePosition();
        echo.setPos(eye.x, eye.y, eye.z);
        echo.shootFromRotation(player, player.getXRot(), player.getYRot(), 0f, 1.75f, 0f);
        echo.setLifetime(AbilityHelper.PHANTOM_LIFETIME);
        level.addFreshEntity(echo);

        stack.set(ModDataComponents.PHANTOM_LINK.get(),
                PhantomLink.of(echo.getUUID(), now + AbilityHelper.PHANTOM_LIFETIME));

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                ModSounds.ECHO_LAUNCH.get(), SoundSource.PLAYERS, 0.9f, 1.2f);
        AbilityHelper.broadcastAnim(player, "ender_echo");
        AbilityHelper.broadcastVfx(level, eye, "echo_launch", 0.8f);

        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    public static void onEchoExpired(ServerLevel level, UUID ownerUuid, UUID echoUuid) {
        Player player = level.getPlayerByUUID(ownerUuid);
        if (player == null) return;

        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (!stack.is(ModItems.ENDER_BLADE.get())) continue;
            PhantomLink link = stack.getOrDefault(ModDataComponents.PHANTOM_LINK.get(), PhantomLink.EMPTY);
            if (link.projectileId().isPresent() && link.projectileId().get().equals(echoUuid)) {
                stack.set(ModDataComponents.PHANTOM_LINK.get(), PhantomLink.EMPTY);
                PlayerBladeData data = AbilityHelper.data(player);
                long now = level.getGameTime();
                data.echoCooldownUntil = now + AbilityHelper.ECHO_COOLDOWN;
                player.getCooldowns().addCooldown(ModItems.ENDER_BLADE.get(), AbilityHelper.ECHO_COOLDOWN);
                return;
            }
        }
    }

    // =====================================================================
    // Void Anchor
    // =====================================================================

    private InteractionResultHolder<ItemStack> tryVoidAnchor(ServerLevel level, ServerPlayer player,
                                                             ItemStack stack, PlayerBladeData data, long now) {
        AnchorLink link = stack.getOrDefault(ModDataComponents.ANCHOR_LINK.get(), AnchorLink.EMPTY);

        if (link.isPresent()) {
            Entity e = level.getEntity(link.anchorId().orElseThrow());
            if (e instanceof VoidAnchorEntity anchor && anchor.isAlive()) {
                Vec3 dest = anchor.position();
                // Validate safe
                if (!AbilityHelper.safeTeleport(player, dest.x, dest.y, dest.z)) {
                    // force near
                    player.teleportTo(dest.x, dest.y, dest.z);
                    player.fallDistance = 0;
                }
                AbilityHelper.afterimageBurst(level, player.position().add(0, 1, 0));
                anchor.activateAndConsume();
                stack.set(ModDataComponents.ANCHOR_LINK.get(), AnchorLink.EMPTY);
                data.anchorCooldownUntil = now + AbilityHelper.ANCHOR_COOLDOWN;
                AbilityHelper.broadcastAnim(player, "void_anchor");
                AbilityHelper.teleportPlayerNoDamage(player, dest);
                return InteractionResultHolder.sidedSuccess(stack, false);
            }
            stack.set(ModDataComponents.ANCHOR_LINK.get(), AnchorLink.EMPTY);
        }

        if (data.isOnCooldown(now, data.anchorCooldownUntil)) {
            return InteractionResultHolder.fail(stack);
        }

        // Place
        VoidAnchorEntity anchor = new VoidAnchorEntity(level, player.position().add(0, 0.2, 0), player.getUUID());
        level.addFreshEntity(anchor);
        stack.set(ModDataComponents.ANCHOR_LINK.get(), AnchorLink.of(anchor.getUUID()));
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                ModSounds.ANCHOR.get(), SoundSource.PLAYERS, 0.9f, 1.0f);
        AbilityHelper.broadcastAnim(player, "void_anchor");
        AbilityHelper.broadcastVfx(level, anchor.position().add(0, 0.5, 0), "anchor_place", 1.0f);

        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    // =====================================================================
    // Void Slash (public for keybind / event)
    // =====================================================================

    public static boolean tryVoidSlash(ServerLevel level, ServerPlayer player) {
        ItemStack stack = player.getMainHandItem();
        if (!stack.is(ModItems.ENDER_BLADE.get())) return false;

        PlayerBladeData data = AbilityHelper.data(player);
        long now = level.getGameTime();
        if (data.isOnCooldown(now, data.voidSlashCooldownUntil)) return false;

        VoidSlashEntity slash = new VoidSlashEntity(level, player);
        level.addFreshEntity(slash);
        data.voidSlashCooldownUntil = now + AbilityHelper.VOID_SLASH_COOLDOWN;
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                ModSounds.VOID_SLASH.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        AbilityHelper.broadcastAnim(player, "void_slash");
        return true;
    }

    // =====================================================================
    // Paradox Step
    // =====================================================================

    public static boolean tryParadoxStep(ServerLevel level, ServerPlayer player, LivingEntity attacker) {
        ItemStack stack = player.getMainHandItem();
        if (!stack.is(ModItems.ENDER_BLADE.get())) return false;

        PlayerBladeData data = AbilityHelper.data(player);
        long now = level.getGameTime();
        if (data.isOnCooldown(now, data.paradoxCooldownUntil)) return false;
        if (now > data.paradoxWindowUntil && attacker == null) {
            // Manual activation: allow if not on CD even outside window
        }

        LivingEntity target = attacker;
        if (target == null) {
            double best = 36.0;
            for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class,
                    player.getBoundingBox().inflate(6.0),
                    liv -> liv.isAlive() && liv != player && liv.hasLineOfSight(player))) {
                double d = e.distanceToSqr(player);
                if (d < best) {
                    best = d;
                    target = e;
                }
            }
        }
        if (target == null || target == player) return false;

        AbilityHelper.afterimageBurst(level, player.position().add(0, 1, 0));
        AbilityHelper.broadcastAnim(player, "paradox_step");
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                ModSounds.PARADOX.get(), SoundSource.PLAYERS, 1.0f, 1.1f);

        Optional<Vec3> behind = AbilityHelper.behindTarget(target, player);
        behind.ifPresent(p -> AbilityHelper.teleportPlayerNoDamage(player, p));

        // Auto crit slash
        target.hurt(level.damageSources().playerAttack(player), 12.0f);
        com.enderblade.effect.VoidMarkEffect.applyOrStack(target, player);
        AbilityHelper.spatialBurst(level, target.position().add(0, 1, 0));

        data.paradoxCooldownUntil = now + AbilityHelper.PARADOX_COOLDOWN;
        data.paradoxWindowUntil = 0;
        return true;
    }

    // =====================================================================
    // Ultimate
    // =====================================================================

    private InteractionResultHolder<ItemStack> tryUltimate(ServerLevel level, ServerPlayer player,
                                                           ItemStack stack, PlayerBladeData data, long now) {
        if (!activateUltimate(level, player, data, now)) {
            return InteractionResultHolder.fail(stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    /** Keybind / network entry for the End Dimension ultimate. */
    public static boolean tryUltimatePublic(ServerLevel level, ServerPlayer player) {
        if (!player.getMainHandItem().is(ModItems.ENDER_BLADE.get())
                && !player.getOffhandItem().is(ModItems.ENDER_BLADE.get())) {
            return false;
        }
        PlayerBladeData data = AbilityHelper.data(player);
        return activateUltimate(level, player, data, level.getGameTime());
    }

    private static boolean activateUltimate(ServerLevel level, ServerPlayer player,
                                            PlayerBladeData data, long now) {
        if (data.isOnCooldown(now, data.ultimateCooldownUntil)) {
            return false;
        }
        EndDimensionZoneEntity zone = new EndDimensionZoneEntity(level, player.position(), player.getUUID());
        level.addFreshEntity(zone);
        data.ultimateCooldownUntil = now + AbilityHelper.ULTIMATE_COOLDOWN;
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                ModSounds.ULTIMATE.get(), SoundSource.PLAYERS, 1.2f, 0.8f);
        AbilityHelper.broadcastAnim(player, "ultimate");
        AbilityHelper.broadcastVfx(level, player.position(), "ultimate_start", 2.0f);
        return true;
    }

    // =====================================================================
    // Tooltip
    // =====================================================================

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext ctx,
                                List<Component> tip, TooltipFlag flag) {
        tip.add(Component.translatable("item.enderblade.ender_blade.desc.passive")
                .withStyle(ChatFormatting.DARK_PURPLE));
        tip.add(Component.translatable("item.enderblade.ender_blade.desc.echo")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        tip.add(Component.translatable("item.enderblade.ender_blade.desc.anchor")
                .withStyle(ChatFormatting.DARK_AQUA));
        tip.add(Component.translatable("item.enderblade.ender_blade.desc.slash")
                .withStyle(ChatFormatting.BLUE));
        tip.add(Component.translatable("item.enderblade.ender_blade.desc.paradox")
                .withStyle(ChatFormatting.AQUA));
        tip.add(Component.translatable("item.enderblade.ender_blade.desc.ultimate")
                .withStyle(ChatFormatting.GOLD));
        tip.add(Component.translatable("item.enderblade.ender_blade.desc.reach")
                .withStyle(ChatFormatting.GRAY));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        // Soft epic glow without vanilla enchant glint spam — keep false; emissive model handles it
        return false;
    }
}

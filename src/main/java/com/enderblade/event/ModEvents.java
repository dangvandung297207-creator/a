package com.enderblade.event;

import com.enderblade.EnderBladeMod;
import com.enderblade.ability.AbilityHelper;
import com.enderblade.ability.PlayerBladeData;
import com.enderblade.item.EnderBladeItem;
import com.enderblade.registry.ModItems;
import com.enderblade.registry.ModParticles;
import com.enderblade.registry.ModSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Server-side combat hooks: Paradox Step auto-trigger, mark visuals, equip sound.
 */
@EventBusSubscriber(modid = EnderBladeMod.MOD_ID)
public final class ModEvents {

    private ModEvents() {
    }

    /**
     * When a player holding the blade is about to take damage during the paradox window,
     * cancel and counter.
     */
    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!player.getMainHandItem().is(ModItems.ENDER_BLADE.get())) return;
        if (!(player.level() instanceof ServerLevel level)) return;

        PlayerBladeData data = AbilityHelper.data(player);
        long now = level.getGameTime();
        if (now > data.paradoxWindowUntil) return;
        if (data.isOnCooldown(now, data.paradoxCooldownUntil)) return;

        LivingEntity attacker = null;
        if (event.getSource().getEntity() instanceof LivingEntity le) {
            attacker = le;
        }
        if (attacker == null || attacker == player) return;

        // Counter!
        event.setCanceled(true);
        EnderBladeItem.tryParadoxStep(level, player, attacker);
    }

    /** Ambient mark particles on marked entities near blade wielders. */
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;
        if (!player.getMainHandItem().is(ModItems.ENDER_BLADE.get())) return;
        if (!(player.level() instanceof ServerLevel level)) return;

        // Equip cue once when switching to blade
        // (lightweight: play soft idle core hum rarely)
        if (player.tickCount % 80 == 0) {
            level.sendParticles(ModParticles.VOID_SPARK.get(),
                    player.getX(), player.getY() + 1.0, player.getZ(),
                    2, 0.2, 0.3, 0.2, 0.01);
        }
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        // Ensure attack anim path when hitting non-living (extra feedback)
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;
        if (!sp.getMainHandItem().is(ModItems.ENDER_BLADE.get())) return;
        if (event.getTarget() instanceof LivingEntity) return; // hurtEnemy handles living

        if (sp.level() instanceof ServerLevel level) {
            level.playSound(null, sp.getX(), sp.getY(), sp.getZ(),
                    ModSounds.SLASH.get(), SoundSource.PLAYERS, 0.5f, 1.2f);
            AbilityHelper.broadcastAnim(sp, "attack1");
        }
    }
}

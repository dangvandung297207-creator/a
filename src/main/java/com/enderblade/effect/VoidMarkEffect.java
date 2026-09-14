package com.enderblade.effect;

import com.enderblade.ability.AbilityHelper;
import com.enderblade.registry.ModEffects;
import com.enderblade.registry.ModSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

/**
 * Void Mark stacks (amplifier 0..2 = 1..3 stacks).
 * At 3 stacks, triggers Rift Collapse on the next application tick.
 */
public class VoidMarkEffect extends MobEffect {

    public static final int MAX_STACKS = 3;
    public static final int DEFAULT_DURATION = 100; // 5 seconds

    public VoidMarkEffect() {
        super(MobEffectCategory.HARMFUL, 0xB45CFF);
    }

    /**
     * Apply or stack Void Mark. Returns true if Rift Collapse was triggered.
     */
    public static boolean applyOrStack(LivingEntity target, LivingEntity attacker) {
        MobEffectInstance existing = target.getEffect(ModEffects.VOID_MARK);
        int stacks = existing == null ? 0 : existing.getAmplifier() + 1;

        if (stacks >= MAX_STACKS - 1) {
            // About to hit 3 — collapse instead of applying 3rd visual stack long-term
            target.removeEffect(ModEffects.VOID_MARK);
            if (target.level() instanceof ServerLevel server) {
                AbilityHelper.riftCollapse(server, target, attacker);
            }
            return true;
        }

        int newAmp = stacks; // 0 = 1 stack, 1 = 2 stacks
        target.addEffect(new MobEffectInstance(ModEffects.VOID_MARK, DEFAULT_DURATION, newAmp, false, true, true));
        target.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                ModSounds.MARK.get(), SoundSource.PLAYERS, 0.6f, 1.2f + newAmp * 0.15f);
        return false;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return false;
    }
}

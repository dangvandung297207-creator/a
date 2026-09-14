package com.enderblade.registry;

import com.enderblade.EnderBladeMod;
import com.enderblade.effect.VoidMarkEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEffects {

    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, EnderBladeMod.MOD_ID);

    public static final DeferredHolder<MobEffect, VoidMarkEffect> VOID_MARK =
            MOB_EFFECTS.register("void_mark", VoidMarkEffect::new);

    private ModEffects() {
    }
}

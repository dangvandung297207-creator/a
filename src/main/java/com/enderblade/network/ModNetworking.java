package com.enderblade.network;

import com.enderblade.EnderBladeMod;
import com.enderblade.item.EnderBladeItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ModNetworking {

    private ModNetworking() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar reg = event.registrar(EnderBladeMod.MOD_ID).versioned("1");

        reg.playToClient(PlayAnimationPayload.TYPE, PlayAnimationPayload.STREAM_CODEC,
                ModNetworking::handleAnimClient);

        reg.playToClient(SpawnVfxPayload.TYPE, SpawnVfxPayload.STREAM_CODEC,
                ModNetworking::handleVfxClient);

        reg.playToServer(AbilityKeyPayload.TYPE, AbilityKeyPayload.STREAM_CODEC,
                ModNetworking::handleAbilityServer);
    }

    private static void handleAnimClient(PlayAnimationPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            // Client-only class accessed reflectively-safe via ClientHooks
            com.enderblade.client.ClientHooks.playAnimation(payload.entityId(), payload.animId());
        });
    }

    private static void handleVfxClient(SpawnVfxPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() ->
                com.enderblade.client.ClientHooks.spawnVfx(payload.vfxType(),
                        payload.x(), payload.y(), payload.z(), payload.scale()));
    }

    private static void handleAbilityServer(AbilityKeyPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer sp)) return;
            if (!(sp.level() instanceof ServerLevel level)) return;

            switch (payload.abilityId()) {
                case "void_slash" -> EnderBladeItem.tryVoidSlash(level, sp);
                case "paradox" -> EnderBladeItem.tryParadoxStep(level, sp, null);
                case "ultimate" -> EnderBladeItem.tryUltimatePublic(level, sp);
                default -> EnderBladeMod.LOGGER.debug("Unknown ability key: {}", payload.abilityId());
            }
        });
    }
}

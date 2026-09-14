package com.enderblade.registry;

import com.enderblade.EnderBladeMod;
import com.enderblade.ability.PlayerBladeData;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

/**
 * Player-bound combat state (combo index, ability cooldowns, ultimate zone).
 */
public final class ModAttachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, EnderBladeMod.MOD_ID);

    public static final Supplier<AttachmentType<PlayerBladeData>> BLADE_DATA =
            ATTACHMENT_TYPES.register("blade_data", () ->
                    AttachmentType.builder(PlayerBladeData::new)
                            .serialize(PlayerBladeData.CODEC)
                            .copyOnDeath()
                            .build());

    private ModAttachments() {
    }
}

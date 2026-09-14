package com.enderblade.network;

import com.enderblade.EnderBladeMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Client → server: request advanced ability (void_slash / paradox / ultimate).
 */
public record AbilityKeyPayload(String abilityId) implements CustomPacketPayload {

    public static final Type<AbilityKeyPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(EnderBladeMod.MOD_ID, "ability_key"));

    public static final StreamCodec<ByteBuf, AbilityKeyPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, AbilityKeyPayload::abilityId,
            AbilityKeyPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

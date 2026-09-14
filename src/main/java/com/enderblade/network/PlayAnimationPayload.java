package com.enderblade.network;

import com.enderblade.EnderBladeMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Server → client: play a named weapon/player animation.
 */
public record PlayAnimationPayload(int entityId, String animId) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PlayAnimationPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(EnderBladeMod.MOD_ID, "play_anim"));

    public static final StreamCodec<ByteBuf, PlayAnimationPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, PlayAnimationPayload::entityId,
            ByteBufCodecs.STRING_UTF8, PlayAnimationPayload::animId,
            PlayAnimationPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

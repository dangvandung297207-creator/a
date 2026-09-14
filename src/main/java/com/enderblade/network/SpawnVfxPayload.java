package com.enderblade.network;

import com.enderblade.EnderBladeMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Server → client: spawn a one-shot mesh/shader VFX at a world position.
 */
public record SpawnVfxPayload(String vfxType, double x, double y, double z, float scale)
        implements CustomPacketPayload {

    public static final Type<SpawnVfxPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(EnderBladeMod.MOD_ID, "spawn_vfx"));

    public static final StreamCodec<ByteBuf, SpawnVfxPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, SpawnVfxPayload::vfxType,
            ByteBufCodecs.DOUBLE, SpawnVfxPayload::x,
            ByteBufCodecs.DOUBLE, SpawnVfxPayload::y,
            ByteBufCodecs.DOUBLE, SpawnVfxPayload::z,
            ByteBufCodecs.FLOAT, SpawnVfxPayload::scale,
            SpawnVfxPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

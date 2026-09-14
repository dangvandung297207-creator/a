package com.enderblade.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Optional;
import java.util.UUID;

/**
 * Links the Ender Blade stack to an active Ender Echo projectile.
 */
public record PhantomLink(Optional<UUID> projectileId, long expireGameTime) {

    public static final PhantomLink EMPTY = new PhantomLink(Optional.empty(), 0L);

    public static final Codec<PhantomLink> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    UUIDUtil.CODEC.optionalFieldOf("projectile_id").forGetter(PhantomLink::projectileId),
                    Codec.LONG.fieldOf("expire_game_time").forGetter(PhantomLink::expireGameTime)
            ).apply(instance, PhantomLink::new)
    );

    public static final StreamCodec<ByteBuf, PhantomLink> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), PhantomLink::projectileId,
            ByteBufCodecs.VAR_LONG, PhantomLink::expireGameTime,
            PhantomLink::new
    );

    public static PhantomLink of(UUID id, long expireAt) {
        return new PhantomLink(Optional.of(id), expireAt);
    }

    public boolean isActive(long gameTime) {
        return projectileId.isPresent() && gameTime <= expireGameTime;
    }
}

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
 * Links the blade to a placed Void Anchor entity.
 */
public record AnchorLink(Optional<UUID> anchorId) {

    public static final AnchorLink EMPTY = new AnchorLink(Optional.empty());

    public static final Codec<AnchorLink> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    UUIDUtil.CODEC.optionalFieldOf("anchor_id").forGetter(AnchorLink::anchorId)
            ).apply(instance, AnchorLink::new)
    );

    public static final StreamCodec<ByteBuf, AnchorLink> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), AnchorLink::anchorId,
            AnchorLink::new
    );

    public static AnchorLink of(UUID id) {
        return new AnchorLink(Optional.of(id));
    }

    public boolean isPresent() {
        return anchorId.isPresent();
    }
}

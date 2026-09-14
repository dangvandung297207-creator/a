package com.truemetallurgy.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/** Live state of a billet (or bloom being heated). */
public record BilletData(String materialId, int temperature, float purity, int reheats) {
    public static final BilletData EMPTY = new BilletData("iron", 20, 90.0F, 0);

    public static final Codec<BilletData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.optionalFieldOf("material", "iron").forGetter(BilletData::materialId),
        Codec.INT.optionalFieldOf("temperature", 20).forGetter(BilletData::temperature),
        Codec.FLOAT.optionalFieldOf("purity", 90.0F).forGetter(BilletData::purity),
        Codec.INT.optionalFieldOf("reheats", 0).forGetter(BilletData::reheats)
    ).apply(instance, BilletData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, BilletData> STREAM_CODEC = StreamCodec.of(
        (buf, v) -> {
            buf.writeUtf(v.materialId());
            buf.writeInt(v.temperature());
            buf.writeFloat(v.purity());
            buf.writeInt(v.reheats());
        },
        buf -> new BilletData(buf.readUtf(), buf.readInt(), buf.readFloat(), buf.readInt()));

    public BilletData withTemp(int temp) {
        return new BilletData(materialId, temp, purity, reheats);
    }

    public BilletData reheated(float purityLoss) {
        return new BilletData(materialId, temperature, Math.max(0.0F, purity - purityLoss), reheats + 1);
    }
}

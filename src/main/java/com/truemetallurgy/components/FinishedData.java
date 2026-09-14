package com.truemetallurgy.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/** Identity of a finished weapon/tool, written once at assembly. */
public record FinishedData(
    String materialId,
    String toolKind,
    int score,
    float purity,
    String quenchId,
    int grindAngle,
    float grindQuality,
    String crafterName,
    String crafterId,
    boolean makerMark,
    String legendaryName,
    String designId,
    float weightKg
) {
    public static final FinishedData EMPTY =
        new FinishedData("iron", "sword", 50, 90.0F, "none", -1, 0.0F, "", "", false, "", "", 2.4F);

    public static final Codec<FinishedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.optionalFieldOf("material", "iron").forGetter(FinishedData::materialId),
        Codec.STRING.optionalFieldOf("tool", "sword").forGetter(FinishedData::toolKind),
        Codec.INT.optionalFieldOf("score", 50).forGetter(FinishedData::score),
        Codec.FLOAT.optionalFieldOf("purity", 90.0F).forGetter(FinishedData::purity),
        Codec.STRING.optionalFieldOf("quench", "none").forGetter(FinishedData::quenchId),
        Codec.INT.optionalFieldOf("grind_angle", -1).forGetter(FinishedData::grindAngle),
        Codec.FLOAT.optionalFieldOf("grind_quality", 0.0F).forGetter(FinishedData::grindQuality),
        Codec.STRING.optionalFieldOf("crafter", "").forGetter(FinishedData::crafterName),
        Codec.STRING.optionalFieldOf("crafter_id", "").forGetter(FinishedData::crafterId),
        Codec.BOOL.optionalFieldOf("maker_mark", false).forGetter(FinishedData::makerMark),
        Codec.STRING.optionalFieldOf("legendary_name", "").forGetter(FinishedData::legendaryName),
        Codec.STRING.optionalFieldOf("design", "").forGetter(FinishedData::designId),
        Codec.FLOAT.optionalFieldOf("weight", 2.4F).forGetter(FinishedData::weightKg)
    ).apply(instance, FinishedData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FinishedData> STREAM_CODEC = StreamCodec.of(
        (buf, v) -> {
            buf.writeUtf(v.materialId());
            buf.writeUtf(v.toolKind());
            buf.writeInt(v.score());
            buf.writeFloat(v.purity());
            buf.writeUtf(v.quenchId());
            buf.writeInt(v.grindAngle());
            buf.writeFloat(v.grindQuality());
            buf.writeUtf(v.crafterName());
            buf.writeUtf(v.crafterId());
            buf.writeBoolean(v.makerMark());
            buf.writeUtf(v.legendaryName());
            buf.writeUtf(v.designId());
            buf.writeFloat(v.weightKg());
        },
        buf -> new FinishedData(
            buf.readUtf(), buf.readUtf(), buf.readInt(), buf.readFloat(), buf.readUtf(),
            buf.readInt(), buf.readFloat(), buf.readUtf(), buf.readUtf(), buf.readBoolean(),
            buf.readUtf(), buf.readUtf(), buf.readFloat()));
}

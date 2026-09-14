package com.truemetallurgy.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * Full forging history of a blade/head component: stages, strike tallies,
 * purity, quenching and grinding. Written by the server at every step.
 */
public record ForgedComponentData(
    String materialId,
    String kind,
    int stage,
    int stageProgress,
    int strikes,
    int perfects,
    int goods,
    int misses,
    int bads,
    int reheats,
    float purity,
    float score,
    int temperature,
    boolean quenched,
    String quenchId,
    int grindAngle,
    float grindQuality,
    String crafterName,
    String crafterId
) {
    public static final ForgedComponentData EMPTY =
        new ForgedComponentData("iron", "sword_blade", 0, 0, 0, 0, 0, 0, 0, 0, 90.0F, 60.0F, 20, false, "none", -1, 0.0F, "", "");

    private static java.util.List<Integer> tallyOf(ForgedComponentData d) {
        return java.util.List.of(d.strikes(), d.perfects(), d.goods(), d.misses(), d.bads());
    }

    private static int tallyAt(java.util.List<Integer> tally, int index) {
        return index < tally.size() ? tally.get(index) : 0;
    }

    private static ForgedComponentData create(String materialId, String kind, int stage, int stageProgress,
            java.util.List<Integer> tally, int reheats, float purity, float score, int temperature,
            boolean quenched, String quenchId, int grindAngle, float grindQuality,
            String crafterName, String crafterId) {
        return new ForgedComponentData(materialId, kind, stage, stageProgress,
            tallyAt(tally, 0), tallyAt(tally, 1), tallyAt(tally, 2), tallyAt(tally, 3), tallyAt(tally, 4),
            reheats, purity, score, temperature, quenched, quenchId, grindAngle, grindQuality,
            crafterName, crafterId);
    }

    public static final Codec<ForgedComponentData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.optionalFieldOf("material", "iron").forGetter(ForgedComponentData::materialId),
        Codec.STRING.optionalFieldOf("kind", "sword_blade").forGetter(ForgedComponentData::kind),
        Codec.INT.optionalFieldOf("stage", 0).forGetter(ForgedComponentData::stage),
        Codec.INT.optionalFieldOf("stage_progress", 0).forGetter(ForgedComponentData::stageProgress),
        Codec.INT.listOf().optionalFieldOf("tally", java.util.List.of(0, 0, 0, 0, 0)).forGetter(ForgedComponentData::tallyOf),
        Codec.INT.optionalFieldOf("reheats", 0).forGetter(ForgedComponentData::reheats),
        Codec.FLOAT.optionalFieldOf("purity", 90.0F).forGetter(ForgedComponentData::purity),
        Codec.FLOAT.optionalFieldOf("score", 60.0F).forGetter(ForgedComponentData::score),
        Codec.INT.optionalFieldOf("temperature", 20).forGetter(ForgedComponentData::temperature),
        Codec.BOOL.optionalFieldOf("quenched", false).forGetter(ForgedComponentData::quenched),
        Codec.STRING.optionalFieldOf("quench", "none").forGetter(ForgedComponentData::quenchId),
        Codec.INT.optionalFieldOf("grind_angle", -1).forGetter(ForgedComponentData::grindAngle),
        Codec.FLOAT.optionalFieldOf("grind_quality", 0.0F).forGetter(ForgedComponentData::grindQuality),
        Codec.STRING.optionalFieldOf("crafter", "").forGetter(ForgedComponentData::crafterName),
        Codec.STRING.optionalFieldOf("crafter_id", "").forGetter(ForgedComponentData::crafterId)
    ).apply(instance, ForgedComponentData::create));

    public static final StreamCodec<RegistryFriendlyByteBuf, ForgedComponentData> STREAM_CODEC = StreamCodec.of(
        (buf, v) -> {
            buf.writeUtf(v.materialId());
            buf.writeUtf(v.kind());
            buf.writeInt(v.stage());
            buf.writeInt(v.stageProgress());
            buf.writeInt(v.strikes());
            buf.writeInt(v.perfects());
            buf.writeInt(v.goods());
            buf.writeInt(v.misses());
            buf.writeInt(v.bads());
            buf.writeInt(v.reheats());
            buf.writeFloat(v.purity());
            buf.writeFloat(v.score());
            buf.writeInt(v.temperature());
            buf.writeBoolean(v.quenched());
            buf.writeUtf(v.quenchId());
            buf.writeInt(v.grindAngle());
            buf.writeFloat(v.grindQuality());
            buf.writeUtf(v.crafterName());
            buf.writeUtf(v.crafterId());
        },
        buf -> new ForgedComponentData(
            buf.readUtf(), buf.readUtf(), buf.readInt(), buf.readInt(), buf.readInt(),
            buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(),
            buf.readFloat(), buf.readFloat(), buf.readInt(), buf.readBoolean(), buf.readUtf(),
            buf.readInt(), buf.readFloat(), buf.readUtf(), buf.readUtf()));

    public boolean isFinished() {
        return stage < 0;
    }
}

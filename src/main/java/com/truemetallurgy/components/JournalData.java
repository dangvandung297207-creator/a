package com.truemetallurgy.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.List;

/** Blacksmith's journal: discovered materials and recorded creations. */
public record JournalData(List<String> discovered, int bestScore, String bestName, List<Creation> creations) {
    public record Creation(String name, int score, String detail) {
        public static final Codec<Creation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("name", "?").forGetter(Creation::name),
            Codec.INT.optionalFieldOf("score", 0).forGetter(Creation::score),
            Codec.STRING.optionalFieldOf("detail", "").forGetter(Creation::detail)
        ).apply(instance, Creation::new));
    }

    public static final JournalData EMPTY = new JournalData(List.of(), 0, "", List.of());

    public static final Codec<JournalData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.listOf().optionalFieldOf("discovered", List.of()).forGetter(JournalData::discovered),
        Codec.INT.optionalFieldOf("best_score", 0).forGetter(JournalData::bestScore),
        Codec.STRING.optionalFieldOf("best_name", "").forGetter(JournalData::bestName),
        Creation.CODEC.listOf().optionalFieldOf("creations", List.of()).forGetter(JournalData::creations)
    ).apply(instance, JournalData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, JournalData> STREAM_CODEC = StreamCodec.of(
        (buf, v) -> {
            buf.writeVarInt(v.discovered().size());
            for (String s : v.discovered()) buf.writeUtf(s);
            buf.writeVarInt(v.bestScore());
            buf.writeUtf(v.bestName());
            buf.writeVarInt(v.creations().size());
            for (Creation c : v.creations()) {
                buf.writeUtf(c.name());
                buf.writeVarInt(c.score());
                buf.writeUtf(c.detail());
            }
        },
        buf -> {
            int n = buf.readVarInt();
            List<String> discovered = new ArrayList<>(n);
            for (int i = 0; i < n; i++) discovered.add(buf.readUtf());
            int best = buf.readVarInt();
            String bestName = buf.readUtf();
            int m = buf.readVarInt();
            List<Creation> creations = new ArrayList<>(m);
            for (int i = 0; i < m; i++) creations.add(new Creation(buf.readUtf(), buf.readVarInt(), buf.readUtf()));
            return new JournalData(discovered, best, bestName, creations);
        });
}

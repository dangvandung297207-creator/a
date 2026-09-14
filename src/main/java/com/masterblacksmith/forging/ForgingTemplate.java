package com.masterblacksmith.forging;

import net.minecraft.network.chat.Component;

import java.util.List;

/** Shape template: the staged journey billet -&gt; finished component. */
public class ForgingTemplate {
    public record Stage(String key, int requiredStrikes) {
        public Component displayName() {
            return Component.translatable("stage.masterblacksmith." + key);
        }
    }

    private final String id;
    private final List<Stage> stages;
    private final String finishedItem;

    public ForgingTemplate(String id, List<Stage> stages, String finishedItem) {
        this.id = id;
        this.stages = List.copyOf(stages);
        this.finishedItem = finishedItem;
    }

    public String getId() { return id; }
    public List<Stage> getStages() { return stages; }
    public String getFinishedItem() { return finishedItem; }

    public int stageCount() { return stages.size(); }

    public Component getDisplayName() {
        return Component.translatable("template.masterblacksmith." + id);
    }
}

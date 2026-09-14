package com.truemetallurgy.forging;

import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

/**
 * Server-side forging session held by the anvil block entity. Tracks the
 * moving target zone, strike rhythm, fractional progress carry and the
 * delayed windup impact. The workpiece stack itself carries the permanent
 * history, so nothing is lost on unload.
 */
public class ForgingSession {
    private float targetX;
    private float targetZ;
    private long lastStrikeTick = -1000;
    private float progressCarry;
    private String targetKind = "sword_blade";

    // Transient windup state (never persisted; safe to drop on unload).
    private long pendingTick = -1;
    private UUID pendingPlayer;
    private int pendingHammerOrdinal = -1;
    private float pendingHitX;
    private float pendingHitZ;
    private long pendingSwingTick;

    public float targetX() { return targetX; }
    public float targetZ() { return targetZ; }
    public long lastStrikeTick() { return lastStrikeTick; }
    public String targetKind() { return targetKind; }

    public void setTargetKind(String kind) {
        this.targetKind = kind;
    }

    public void markStruck(long gameTime) {
        this.lastStrikeTick = gameTime;
    }

    /** Shift the target zone after every resolved strike. */
    public void retarget(net.minecraft.util.RandomSource random) {
        this.targetX = (random.nextFloat() - 0.5F) * 0.56F;
        this.targetZ = (random.nextFloat() - 0.5F) * 0.56F;
    }

    public float takeCarry(float add) {
        this.progressCarry += add;
        float whole = (float) Math.floor(this.progressCarry);
        this.progressCarry -= whole;
        return whole;
    }

    public boolean hasPending(long gameTime) {
        return pendingTick >= 0 && gameTime >= pendingTick;
    }

    public boolean hasPendingAny() {
        return pendingTick >= 0;
    }

    public void queueImpact(long impactTick, UUID player, int hammerOrdinal, float hitX, float hitZ, long swingTick) {
        this.pendingTick = impactTick;
        this.pendingPlayer = player;
        this.pendingHammerOrdinal = hammerOrdinal;
        this.pendingHitX = hitX;
        this.pendingHitZ = hitZ;
        this.pendingSwingTick = swingTick;
    }

    public void clearPending() {
        this.pendingTick = -1;
        this.pendingPlayer = null;
        this.pendingHammerOrdinal = -1;
    }

    public UUID pendingPlayer() { return pendingPlayer; }
    public int pendingHammerOrdinal() { return pendingHammerOrdinal; }
    public float pendingHitX() { return pendingHitX; }
    public float pendingHitZ() { return pendingHitZ; }
    public long pendingSwingTick() { return pendingSwingTick; }

    public void save(CompoundTag tag) {
        tag.putFloat("TargetX", targetX);
        tag.putFloat("TargetZ", targetZ);
        tag.putLong("LastStrike", lastStrikeTick);
        tag.putFloat("Carry", progressCarry);
        tag.putString("TargetKind", targetKind);
    }

    public void load(CompoundTag tag) {
        targetX = tag.getFloat("TargetX");
        targetZ = tag.getFloat("TargetZ");
        lastStrikeTick = tag.getLong("LastStrike");
        progressCarry = tag.getFloat("Carry");
        targetKind = tag.contains("TargetKind") ? tag.getString("TargetKind") : "sword_blade";
    }
}

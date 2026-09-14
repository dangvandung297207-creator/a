package com.enderblade.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Per-player combat state for the Ender Blade.
 * Serialized via NeoForge AttachmentType.
 */
public class PlayerBladeData {

    public static final Codec<PlayerBladeData> CODEC = RecordCodecBuilder.create(inst ->
            inst.group(
                    Codec.INT.fieldOf("combo").forGetter(d -> d.comboIndex),
                    Codec.LONG.fieldOf("combo_expire").forGetter(d -> d.comboExpireTick),
                    Codec.LONG.fieldOf("echo_cd").forGetter(d -> d.echoCooldownUntil),
                    Codec.LONG.fieldOf("anchor_cd").forGetter(d -> d.anchorCooldownUntil),
                    Codec.LONG.fieldOf("slash_cd").forGetter(d -> d.voidSlashCooldownUntil),
                    Codec.LONG.fieldOf("paradox_cd").forGetter(d -> d.paradoxCooldownUntil),
                    Codec.LONG.fieldOf("ultimate_cd").forGetter(d -> d.ultimateCooldownUntil),
                    Codec.LONG.fieldOf("paradox_window").forGetter(d -> d.paradoxWindowUntil)
            ).apply(inst, PlayerBladeData::new)
    );

    public int comboIndex;
    public long comboExpireTick;
    public long echoCooldownUntil;
    public long anchorCooldownUntil;
    public long voidSlashCooldownUntil;
    public long paradoxCooldownUntil;
    public long ultimateCooldownUntil;
    /** While gameTime &lt; this, Paradox Step can auto-trigger on damage. */
    public long paradoxWindowUntil;

    public PlayerBladeData() {
        this(0, 0L, 0L, 0L, 0L, 0L, 0L, 0L);
    }

    public PlayerBladeData(int combo, long comboExpire, long echoCd, long anchorCd,
                           long slashCd, long paradoxCd, long ultimateCd, long paradoxWindow) {
        this.comboIndex = combo;
        this.comboExpireTick = comboExpire;
        this.echoCooldownUntil = echoCd;
        this.anchorCooldownUntil = anchorCd;
        this.voidSlashCooldownUntil = slashCd;
        this.paradoxCooldownUntil = paradoxCd;
        this.ultimateCooldownUntil = ultimateCd;
        this.paradoxWindowUntil = paradoxWindow;
    }

    public boolean isOnCooldown(long now, long until) {
        return now < until;
    }

    public int nextCombo(long now) {
        if (now > comboExpireTick) {
            comboIndex = 0;
        }
        int hit = comboIndex;
        comboIndex = (comboIndex + 1) % 3;
        comboExpireTick = now + 40; // 2s window between combo hits
        return hit;
    }
}

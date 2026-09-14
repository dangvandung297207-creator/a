package com.masterblacksmith.forging;

import com.masterblacksmith.item.BilletItem;
import com.masterblacksmith.item.BloomItem;
import com.masterblacksmith.item.ForgedComponentItem;
import com.masterblacksmith.material.MetalMaterial;
import com.masterblacksmith.material.MetalMaterials;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * All per-workpiece forging state lives on the ItemStack: temperature, template,
 * stage, strike history, purity, quench and grind records. This is what makes
 * every high-end piece one of a kind.
 */
public final class ForgingData {
    public static final String TAG = "MBSForge";
    public static final float ROOM_TEMP = 20F;

    private ForgingData() {}

    public static CompoundTag tag(ItemStack stack) {
        CompoundTag root = stack.getOrCreateTag();
        if (!root.contains(TAG)) root.put(TAG, new CompoundTag());
        return root.getCompound(TAG);
    }

    public static CompoundTag peek(ItemStack stack) {
        CompoundTag root = stack.getTag();
        if (root == null || !root.contains(TAG)) return new CompoundTag();
        return root.getCompound(TAG);
    }

    public static boolean isWorkable(ItemStack stack) {
        return stack.getItem() instanceof BilletItem
                || stack.getItem() instanceof BloomItem
                || (stack.getItem() instanceof ForgedComponentItem c && !c.isFinished());
    }

    public static boolean isQuenchable(ItemStack stack) {
        return stack.getItem() instanceof ForgedComponentItem c && c.isFinished() && !isQuenched(stack);
    }

    public static boolean isAssemblyReady(ItemStack stack) {
        return stack.getItem() instanceof ForgedComponentItem c && c.isFinished() && isQuenched(stack);
    }

    public static void initBillet(ItemStack stack, String metal) {
        CompoundTag t = tag(stack);
        t.putString("Metal", metal);
        t.putFloat("Temp", ROOM_TEMP);
        t.putFloat("Purity", 88F);
        t.putInt("Reheats", 0);
    }

    public static void initBlank(ItemStack stack, String metal, String template, float purity, int reheats) {
        CompoundTag t = tag(stack);
        t.putString("Metal", metal);
        t.putString("Template", template);
        t.putInt("Stage", 0);
        t.putInt("Strikes", 0);
        t.putFloat("Temp", ROOM_TEMP);
        t.putFloat("Purity", purity);
        t.putInt("Reheats", reheats);
        t.putFloat("QSum", 0F);
        t.putInt("QN", 0);
        t.putFloat("HeatSum", 0F);
        t.putInt("HeatN", 0);
    }

    public static String getMetalId(ItemStack stack) {
        String id = peek(stack).getString("Metal");
        if (!id.isEmpty()) return id;
        if (stack.getItem() instanceof BilletItem b) return b.getMetalId();
        if (stack.getItem() instanceof BloomItem b) return b.getMetalId();
        return "iron";
    }

    public static MetalMaterial getMetal(ItemStack stack) {
        return MetalMaterials.get(getMetalId(stack));
    }

    public static void setMetal(ItemStack stack, String metal) {
        tag(stack).putString("Metal", metal);
    }

    public static float getTemp(ItemStack stack) {
        CompoundTag t = peek(stack);
        return t.contains("Temp") ? t.getFloat("Temp") : ROOM_TEMP;
    }

    public static void setTemp(ItemStack stack, float temp) {
        tag(stack).putFloat("Temp", Math.max(ROOM_TEMP, temp));
    }

    public static void cool(ItemStack stack, float degrees) {
        setTemp(stack, getTemp(stack) - degrees);
    }

    public static String getTemplateId(ItemStack stack) {
        return peek(stack).getString("Template");
    }

    public static int getStage(ItemStack stack) {
        return peek(stack).getInt("Stage");
    }

    public static void setStage(ItemStack stack, int stage) {
        tag(stack).putInt("Stage", stage);
    }

    public static int getStrikes(ItemStack stack) {
        return peek(stack).getInt("Strikes");
    }

    public static void setStrikes(ItemStack stack, int strikes) {
        tag(stack).putInt("Strikes", strikes);
    }

    public static void recordStrike(ItemStack stack, float score, float heat) {
        CompoundTag t = tag(stack);
        t.putFloat("QSum", t.getFloat("QSum") + score);
        t.putInt("QN", t.getInt("QN") + 1);
        t.putFloat("HeatSum", t.getFloat("HeatSum") + heat);
        t.putInt("HeatN", t.getInt("HeatN") + 1);
        t.putInt("Strikes", t.getInt("Strikes") + 1);
    }

    public static float avgStrikeQuality(ItemStack stack) {
        CompoundTag t = peek(stack);
        int n = t.getInt("QN");
        return n <= 0 ? 0F : t.getFloat("QSum") / n;
    }

    public static float avgHeatQuality(ItemStack stack) {
        CompoundTag t = peek(stack);
        int n = t.getInt("HeatN");
        return n <= 0 ? 0F : t.getFloat("HeatSum") / n;
    }

    public static int getReheats(ItemStack stack) {
        return peek(stack).getInt("Reheats");
    }

    public static void addReheat(ItemStack stack) {
        CompoundTag t = tag(stack);
        t.putInt("Reheats", t.getInt("Reheats") + 1);
    }

    public static float getPurity(ItemStack stack) {
        CompoundTag t = peek(stack);
        return t.contains("Purity") ? t.getFloat("Purity") : 88F;
    }

    public static void setPurity(ItemStack stack, float purity) {
        tag(stack).putFloat("Purity", Math.max(20F, Math.min(100F, purity)));
    }

    public static boolean isQuenched(ItemStack stack) {
        return peek(stack).getBoolean("Quenched");
    }

    public static void setQuenched(ItemStack stack, String quenchId, float quenchQuality) {
        CompoundTag t = tag(stack);
        t.putBoolean("Quenched", true);
        t.putString("Quench", quenchId);
        t.putFloat("QuenchQ", quenchQuality);
    }

    public static String getQuenchId(ItemStack stack) {
        return peek(stack).getString("Quench");
    }

    public static float getQuenchQ(ItemStack stack) {
        return peek(stack).getFloat("QuenchQ");
    }

    public static float getGrindAngle(ItemStack stack) {
        CompoundTag t = peek(stack);
        return t.contains("GrindAngle") ? t.getFloat("GrindAngle") : 25F;
    }

    public static void setGrind(ItemStack stack, float angle, float quality) {
        CompoundTag t = tag(stack);
        t.putFloat("GrindAngle", angle);
        t.putFloat("GrindQ", quality);
    }

    public static float getGrindQ(ItemStack stack) {
        return peek(stack).getFloat("GrindQ");
    }

    public static boolean isGround(ItemStack stack) {
        return peek(stack).contains("GrindQ");
    }

    public static void setForgingQ(ItemStack stack, float q) {
        tag(stack).putFloat("ForgingQ", q);
    }

    public static float getForgingQ(ItemStack stack) {
        return peek(stack).getFloat("ForgingQ");
    }

    public static void setAnvilCap(ItemStack stack, int cap) {
        tag(stack).putInt("AnvilCap", cap);
    }

    public static int getAnvilCap(ItemStack stack) {
        CompoundTag t = peek(stack);
        return t.contains("AnvilCap") ? t.getInt("AnvilCap") : 100;
    }

    public static void addHistory(ItemStack stack, String line) {
        CompoundTag t = tag(stack);
        ListTag list = t.contains("History") ? t.getList("History", 8) : new ListTag();
        if (list.size() < 24) {
            list.add(StringTag.valueOf(line));
            t.put("History", list);
        }
    }

    public static List<String> getHistory(ItemStack stack) {
        List<String> out = new ArrayList<>();
        CompoundTag t = peek(stack);
        if (!t.contains("History")) return out;
        for (var e : t.getList("History", 8)) out.add(e.getAsString());
        return out;
    }
}

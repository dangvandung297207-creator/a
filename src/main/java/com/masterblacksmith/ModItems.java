package com.masterblacksmith;

import com.masterblacksmith.item.BilletItem;
import com.masterblacksmith.item.BlacksmithJournalItem;
import com.masterblacksmith.item.BlacksmithTongsItem;
import com.masterblacksmith.item.BloomItem;
import com.masterblacksmith.item.BlueprintItem;
import com.masterblacksmith.item.ComponentPartItem;
import com.masterblacksmith.item.ForgedArmorItem;
import com.masterblacksmith.item.ForgedArmorMaterial;
import com.masterblacksmith.item.ForgedAxeItem;
import com.masterblacksmith.item.ForgedComponentItem;
import com.masterblacksmith.item.ForgedPickaxeItem;
import com.masterblacksmith.item.ForgedSpearItem;
import com.masterblacksmith.item.ForgedSwordItem;
import com.masterblacksmith.item.HammerTier;
import com.masterblacksmith.item.HandleItem;
import com.masterblacksmith.item.QuenchBucketItem;
import com.masterblacksmith.item.SmithingHammerItem;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

/** Every hammer, billet, component, part, weapon and trinket of the forge. */
public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MasterBlacksmith.MOD_ID);

    // --- Smithing hammers (5 tiers) ---
    public static final RegistryObject<Item> HAMMER_COPPER = register("hammer_copper",
            () -> new SmithingHammerItem(HammerTier.COPPER));
    public static final RegistryObject<Item> HAMMER_IRON = register("hammer_iron",
            () -> new SmithingHammerItem(HammerTier.IRON));
    public static final RegistryObject<Item> HAMMER_STEEL = register("hammer_steel",
            () -> new SmithingHammerItem(HammerTier.STEEL));
    public static final RegistryObject<Item> HAMMER_HARDENED = register("hammer_hardened",
            () -> new SmithingHammerItem(HammerTier.HARDENED));
    public static final RegistryObject<Item> HAMMER_MASTERWORK = register("hammer_masterwork",
            () -> new SmithingHammerItem(HammerTier.MASTERWORK));

    // --- Tongs ---
    public static final RegistryObject<Item> TONGS = register("tongs",
            BlacksmithTongsItem::new);

    // --- Metallurgy trios: processed ore / bloom / billet (8 metals) ---
    public static final String[] METALS = {
            "copper", "tin", "bronze", "iron", "steel", "hardened_steel", "damascus_steel", "starfall_steel"
    };

    static {
        for (String metal : METALS) {
            final String m = metal;
            ITEMS.register("processed_" + m, () -> new Item(new Item.Properties()));
            ITEMS.register("bloom_" + m, () -> new BloomItem(m));
            ITEMS.register("billet_" + m, () -> new BilletItem(m));
        }
    }

    // --- Raw materials & ingots ---
    public static final RegistryObject<Item> RAW_TIN = simple("raw_tin");
    public static final RegistryObject<Item> STARFALL_SHARD = simple("starfall_shard");
    public static final RegistryObject<Item> TIN_INGOT = simple("tin_ingot");
    public static final RegistryObject<Item> BRONZE_INGOT = simple("bronze_ingot");
    public static final RegistryObject<Item> STEEL_INGOT = simple("steel_ingot");
    public static final RegistryObject<Item> HARDENED_STEEL_INGOT = simple("hardened_steel_ingot");
    public static final RegistryObject<Item> DAMASCUS_STEEL_INGOT = simple("damascus_steel_ingot");
    public static final RegistryObject<Item> STARFALL_STEEL_INGOT = simple("starfall_steel_ingot");
    public static final RegistryObject<Item> REFRACTORY_BRICK = simple("refractory_brick");
    public static final RegistryObject<Item> COKE = simple("coke");
    public static final RegistryObject<Item> LEATHER_STRIP = simple("leather_strip");
    public static final RegistryObject<Item> METAL_RIVET = simple("metal_rivet");
    public static final RegistryObject<Item> SLAG = simple("slag");

    // --- Forging components: unfinished blanks + finished (quench-ready) pieces ---
    public static final RegistryObject<Item> BLADE_BLANK = register("blade_blank",
            () -> new ForgedComponentItem("sword", false));
    public static final RegistryObject<Item> AXE_HEAD_BLANK = register("axe_head_blank",
            () -> new ForgedComponentItem("axe", false));
    public static final RegistryObject<Item> PICK_HEAD_BLANK = register("pick_head_blank",
            () -> new ForgedComponentItem("pickaxe", false));
    public static final RegistryObject<Item> SPEAR_HEAD_BLANK = register("spear_head_blank",
            () -> new ForgedComponentItem("spear", false));
    public static final RegistryObject<Item> ARMOR_PLATE_BLANK = register("armor_plate_blank",
            () -> new ForgedComponentItem("plate", false));
    public static final RegistryObject<Item> SWORD_BLADE = register("sword_blade",
            () -> new ForgedComponentItem("sword", true));
    public static final RegistryObject<Item> AXE_HEAD = register("axe_head",
            () -> new ForgedComponentItem("axe", true));
    public static final RegistryObject<Item> PICKAXE_HEAD = register("pickaxe_head",
            () -> new ForgedComponentItem("pickaxe", true));
    public static final RegistryObject<Item> SPEAR_HEAD = register("spear_head",
            () -> new ForgedComponentItem("spear", true));
    public static final RegistryObject<Item> ARMOR_PLATE = register("armor_plate",
            () -> new ForgedComponentItem("plate", true));

    // --- Assembly parts ---
    public static final RegistryObject<Item> GUARD_IRON = register("guard_iron",
            () -> new ComponentPartItem(ComponentPartItem.PartKind.GUARD, "iron", 0));
    public static final RegistryObject<Item> GUARD_STEEL = register("guard_steel",
            () -> new ComponentPartItem(ComponentPartItem.PartKind.GUARD, "steel", 6));
    public static final RegistryObject<Item> GUARD_BRASS = register("guard_brass",
            () -> new ComponentPartItem(ComponentPartItem.PartKind.GUARD, "brass", 10));
    public static final RegistryObject<Item> GRIP_LEATHER = register("grip_leather",
            () -> new ComponentPartItem(ComponentPartItem.PartKind.GRIP, "leather", 4));
    public static final RegistryObject<Item> GRIP_BONE = register("grip_bone",
            () -> new ComponentPartItem(ComponentPartItem.PartKind.GRIP, "bone", 8));
    public static final RegistryObject<Item> GRIP_EXOTIC = register("grip_exotic",
            () -> new ComponentPartItem(ComponentPartItem.PartKind.GRIP, "exotic", 12));
    public static final RegistryObject<Item> POMMEL_IRON = register("pommel_iron",
            () -> new ComponentPartItem(ComponentPartItem.PartKind.POMMEL, "iron", 0));
    public static final RegistryObject<Item> POMMEL_STEEL = register("pommel_steel",
            () -> new ComponentPartItem(ComponentPartItem.PartKind.POMMEL, "steel", 6));
    public static final RegistryObject<Item> POMMEL_BRASS = register("pommel_brass",
            () -> new ComponentPartItem(ComponentPartItem.PartKind.POMMEL, "brass", 10));

    // --- Handles (8 materials) ---
    public static final String[] HANDLES = {
            "oak", "spruce", "birch", "dark_oak", "bamboo", "reinforced", "leather_wrapped", "bone"
    };

    static {
        for (String handle : HANDLES) {
            final String h = handle;
            ITEMS.register("handle_" + h, () -> new HandleItem(h));
        }
    }

    // --- Finished forged weapons & armor (stats live in NBT identity) ---
    public static final RegistryObject<Item> FORGED_SWORD = register("forged_sword", ForgedSwordItem::new);
    public static final RegistryObject<Item> FORGED_AXE = register("forged_axe", ForgedAxeItem::new);
    public static final RegistryObject<Item> FORGED_PICKAXE = register("forged_pickaxe", ForgedPickaxeItem::new);
    public static final RegistryObject<Item> FORGED_SPEAR = register("forged_spear", ForgedSpearItem::new);
    public static final RegistryObject<Item> FORGED_HELMET = register("forged_helmet",
            () -> new ForgedArmorItem(ForgedArmorMaterial.FORGED, ArmorItem.Type.HELMET));
    public static final RegistryObject<Item> FORGED_CHESTPLATE = register("forged_chestplate",
            () -> new ForgedArmorItem(ForgedArmorMaterial.FORGED, ArmorItem.Type.CHESTPLATE));
    public static final RegistryObject<Item> FORGED_LEGGINGS = register("forged_leggings",
            () -> new ForgedArmorItem(ForgedArmorMaterial.FORGED, ArmorItem.Type.LEGGINGS));
    public static final RegistryObject<Item> FORGED_BOOTS = register("forged_boots",
            () -> new ForgedArmorItem(ForgedArmorMaterial.FORGED, ArmorItem.Type.BOOTS));

    // --- Quenching liquids ---
    public static final RegistryObject<Item> OIL_BUCKET = register("oil_bucket",
            () -> new QuenchBucketItem("oil"));
    public static final RegistryObject<Item> SALT_WATER_BUCKET = register("salt_water_bucket",
            () -> new QuenchBucketItem("salt_water"));
    public static final RegistryObject<Item> HERBAL_OIL_BUCKET = register("herbal_oil_bucket",
            () -> new QuenchBucketItem("herbal_oil"));
    public static final RegistryObject<Item> MINERAL_OIL_BUCKET = register("mineral_oil_bucket",
            () -> new QuenchBucketItem("mineral_oil"));
    public static final RegistryObject<Item> ALCHEMICAL_OIL_BUCKET = register("alchemical_oil_bucket",
            () -> new QuenchBucketItem("alchemical_oil"));
    public static final RegistryObject<Item> BLOOD_QUENCH_VIAL = register("blood_quench_vial",
            () -> new QuenchBucketItem("blood_infused"));
    public static final RegistryObject<Item> STARFALL_QUENCH_VIAL = register("starfall_quench_vial",
            () -> new QuenchBucketItem("starfall"));

    // --- Journal & legendary blueprints ---
    public static final RegistryObject<Item> JOURNAL = register("blacksmith_journal", BlacksmithJournalItem::new);
    public static final RegistryObject<Item> BLUEPRINT_KINGS_EDGE = register("blueprint_kings_edge",
            () -> new BlueprintItem("kings_edge"));
    public static final RegistryObject<Item> BLUEPRINT_OATHKEEPER = register("blueprint_oathkeeper",
            () -> new BlueprintItem("oathkeeper"));
    public static final RegistryObject<Item> BLUEPRINT_STONESPLITTER = register("blueprint_stonesplitter",
            () -> new BlueprintItem("stonesplitter"));
    public static final RegistryObject<Item> BLUEPRINT_SKYPIERCER = register("blueprint_skypiercer",
            () -> new BlueprintItem("skypiercer"));
    public static final RegistryObject<Item> BLUEPRINT_AEGIS = register("blueprint_aegis",
            () -> new BlueprintItem("aegis"));

    private static RegistryObject<Item> simple(String name) {
        return register(name, () -> new Item(new Item.Properties()));
    }

    private static RegistryObject<Item> register(String name, Supplier<Item> sup) {
        return ITEMS.register(name, sup);
    }
}

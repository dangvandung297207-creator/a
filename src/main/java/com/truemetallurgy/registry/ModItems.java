package com.truemetallurgy.registry;

import com.truemetallurgy.TrueMetallurgy;
import com.truemetallurgy.entity.BlacksmithEntity;
import com.truemetallurgy.item.BilletItem;
import com.truemetallurgy.item.BlacksmithJournalItem;
import com.truemetallurgy.item.BlacksmithTongsItem;
import com.truemetallurgy.item.BlueprintItem;
import com.truemetallurgy.item.CokeItem;
import com.truemetallurgy.item.ComponentKind;
import com.truemetallurgy.item.ForgedAxeItem;
import com.truemetallurgy.item.ForgedComponentItem;
import com.truemetallurgy.item.ForgedPickaxeItem;
import com.truemetallurgy.item.ForgedSpearItem;
import com.truemetallurgy.item.ForgedSwordItem;
import com.truemetallurgy.item.HandleItem;
import com.truemetallurgy.item.HammerTier;
import com.truemetallurgy.item.MaterialItem;
import com.truemetallurgy.item.SmithingHammerItem;
import com.truemetallurgy.metallurgy.HandleMaterial;
import com.truemetallurgy.metallurgy.Materials;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/** Every item in the mod. Finished tools are registered per material. */
public final class ModItems {
    private ModItems() {}

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, TrueMetallurgy.MOD_ID);

    private static DeferredHolder<Item, BlockItem> blockItem(String name, Supplier<? extends Block> block) {
        return ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    // Workstation block items
    public static final DeferredHolder<Item, BlockItem> PRIMITIVE_FORGE = blockItem("primitive_forge", ModBlocks.PRIMITIVE_FORGE);
    public static final DeferredHolder<Item, BlockItem> IRON_FORGE = blockItem("iron_forge", ModBlocks.IRON_FORGE);
    public static final DeferredHolder<Item, BlockItem> STEEL_FORGE = blockItem("steel_forge", ModBlocks.STEEL_FORGE);
    public static final DeferredHolder<Item, BlockItem> MASTER_FORGE = blockItem("master_forge", ModBlocks.MASTER_FORGE);
    public static final DeferredHolder<Item, BlockItem> BELLOWS = blockItem("bellows", ModBlocks.BELLOWS);
    public static final DeferredHolder<Item, BlockItem> BASIC_ANVIL = blockItem("basic_anvil", ModBlocks.BASIC_ANVIL);
    public static final DeferredHolder<Item, BlockItem> IRON_ANVIL = blockItem("iron_anvil", ModBlocks.IRON_ANVIL);
    public static final DeferredHolder<Item, BlockItem> STEEL_ANVIL = blockItem("steel_anvil", ModBlocks.STEEL_ANVIL);
    public static final DeferredHolder<Item, BlockItem> MASTER_ANVIL = blockItem("master_anvil", ModBlocks.MASTER_ANVIL);
    public static final DeferredHolder<Item, BlockItem> QUENCHING_BARREL = blockItem("quenching_barrel", ModBlocks.QUENCHING_BARREL);
    public static final DeferredHolder<Item, BlockItem> GRINDING_WHEEL = blockItem("grinding_wheel", ModBlocks.GRINDING_WHEEL);
    public static final DeferredHolder<Item, BlockItem> ASSEMBLY_TABLE = blockItem("assembly_table", ModBlocks.ASSEMBLY_TABLE);
    public static final DeferredHolder<Item, BlockItem> STEEL_BLOCK = blockItem("steel_block", ModBlocks.STEEL_BLOCK);
    public static final DeferredHolder<Item, BlockItem> TOOL_RACK = blockItem("tool_rack", ModBlocks.TOOL_RACK);

    // Blacksmith tools
    public static final DeferredHolder<Item, BlacksmithTongsItem> TONGS = ITEMS.register("blacksmith_tongs",
        () -> new BlacksmithTongsItem(new Item.Properties().stacksTo(1).durability(256)));
    public static final DeferredHolder<Item, SmithingHammerItem> PRIMITIVE_HAMMER = ITEMS.register("primitive_hammer",
        () -> new SmithingHammerItem(HammerTier.PRIMITIVE));
    public static final DeferredHolder<Item, SmithingHammerItem> COPPER_HAMMER = ITEMS.register("copper_hammer",
        () -> new SmithingHammerItem(HammerTier.COPPER));
    public static final DeferredHolder<Item, SmithingHammerItem> IRON_HAMMER = ITEMS.register("iron_hammer",
        () -> new SmithingHammerItem(HammerTier.IRON));
    public static final DeferredHolder<Item, SmithingHammerItem> STEEL_HAMMER = ITEMS.register("steel_hammer",
        () -> new SmithingHammerItem(HammerTier.STEEL));
    public static final DeferredHolder<Item, SmithingHammerItem> HARDENED_HAMMER = ITEMS.register("hardened_hammer",
        () -> new SmithingHammerItem(HammerTier.HARDENED));
    public static final DeferredHolder<Item, SmithingHammerItem> MASTERWORK_HAMMER = ITEMS.register("masterwork_hammer",
        () -> new SmithingHammerItem(HammerTier.MASTERWORK));

    // Metallurgy pipeline: processed ore -> bloom -> billet
    public static final DeferredHolder<Item, MaterialItem> CRUSHED_IRON_ORE = ITEMS.register("crushed_iron_ore",
        () -> new MaterialItem("crushed"));
    public static final DeferredHolder<Item, MaterialItem> CRUSHED_COPPER_ORE = ITEMS.register("crushed_copper_ore",
        () -> new MaterialItem("crushed"));
    public static final DeferredHolder<Item, MaterialItem> IRON_BLOOM = ITEMS.register("iron_bloom",
        () -> new MaterialItem("bloom"));
    public static final DeferredHolder<Item, MaterialItem> COPPER_BLOOM = ITEMS.register("copper_bloom",
        () -> new MaterialItem("bloom"));
    public static final DeferredHolder<Item, MaterialItem> STEEL_BLOOM = ITEMS.register("steel_bloom",
        () -> new MaterialItem("bloom"));
    public static final DeferredHolder<Item, MaterialItem> HARDENED_BLOOM = ITEMS.register("hardened_bloom",
        () -> new MaterialItem("bloom"));
    public static final DeferredHolder<Item, BilletItem> COPPER_BILLET = ITEMS.register("copper_billet",
        () -> new BilletItem(Materials.COPPER));
    public static final DeferredHolder<Item, BilletItem> IRON_BILLET = ITEMS.register("iron_billet",
        () -> new BilletItem(Materials.IRON));
    public static final DeferredHolder<Item, BilletItem> STEEL_BILLET = ITEMS.register("steel_billet",
        () -> new BilletItem(Materials.STEEL));
    public static final DeferredHolder<Item, BilletItem> HARDENED_BILLET = ITEMS.register("hardened_billet",
        () -> new BilletItem(Materials.HARDENED_STEEL));

    // Forged components (hot-capable, stage-based)
    public static final DeferredHolder<Item, ForgedComponentItem> SWORD_BLADE = ITEMS.register("sword_blade",
        () -> new ForgedComponentItem(ComponentKind.SWORD_BLADE));
    public static final DeferredHolder<Item, ForgedComponentItem> AXE_HEAD = ITEMS.register("axe_head",
        () -> new ForgedComponentItem(ComponentKind.AXE_HEAD));
    public static final DeferredHolder<Item, ForgedComponentItem> PICKAXE_HEAD = ITEMS.register("pickaxe_head",
        () -> new ForgedComponentItem(ComponentKind.PICKAXE_HEAD));
    public static final DeferredHolder<Item, ForgedComponentItem> SPEAR_HEAD = ITEMS.register("spear_head",
        () -> new ForgedComponentItem(ComponentKind.SPEAR_HEAD));

    // Guards & pommels
    public static final DeferredHolder<Item, MaterialItem> IRON_GUARD = ITEMS.register("iron_guard", () -> new MaterialItem("guard"));
    public static final DeferredHolder<Item, MaterialItem> STEEL_GUARD = ITEMS.register("steel_guard", () -> new MaterialItem("guard"));
    public static final DeferredHolder<Item, MaterialItem> IRON_POMMEL = ITEMS.register("iron_pommel", () -> new MaterialItem("pommel"));
    public static final DeferredHolder<Item, MaterialItem> STEEL_POMMEL = ITEMS.register("steel_pommel", () -> new MaterialItem("pommel"));

    // Handles
    public static final DeferredHolder<Item, HandleItem> OAK_HANDLE = ITEMS.register("oak_handle",
        () -> new HandleItem(HandleMaterial.OAK));
    public static final DeferredHolder<Item, HandleItem> SPRUCE_HANDLE = ITEMS.register("spruce_handle",
        () -> new HandleItem(HandleMaterial.SPRUCE));
    public static final DeferredHolder<Item, HandleItem> BIRCH_HANDLE = ITEMS.register("birch_handle",
        () -> new HandleItem(HandleMaterial.BIRCH));
    public static final DeferredHolder<Item, HandleItem> DARK_OAK_HANDLE = ITEMS.register("dark_oak_handle",
        () -> new HandleItem(HandleMaterial.DARK_OAK));
    public static final DeferredHolder<Item, HandleItem> BAMBOO_HANDLE = ITEMS.register("bamboo_handle",
        () -> new HandleItem(HandleMaterial.BAMBOO));
    public static final DeferredHolder<Item, HandleItem> REINFORCED_HANDLE = ITEMS.register("reinforced_handle",
        () -> new HandleItem(HandleMaterial.REINFORCED));
    public static final DeferredHolder<Item, HandleItem> LEATHER_WRAPPED_HANDLE = ITEMS.register("leather_wrapped_handle",
        () -> new HandleItem(HandleMaterial.LEATHER_WRAPPED));
    public static final DeferredHolder<Item, HandleItem> BONE_HANDLE = ITEMS.register("bone_handle",
        () -> new HandleItem(HandleMaterial.BONE));
    public static final DeferredHolder<Item, MaterialItem> LONG_SHAFT = ITEMS.register("long_shaft", () -> new MaterialItem("shaft"));
    public static final DeferredHolder<Item, MaterialItem> BINDING = ITEMS.register("binding", () -> new MaterialItem("binding"));

    // Finished tools, one item per material for correct mining levels + identity.
    public static final DeferredHolder<Item, ForgedSwordItem> COPPER_SWORD = ITEMS.register("copper_sword",
        () -> new ForgedSwordItem(Materials.COPPER));
    public static final DeferredHolder<Item, ForgedSwordItem> IRON_SWORD = ITEMS.register("iron_sword",
        () -> new ForgedSwordItem(Materials.IRON));
    public static final DeferredHolder<Item, ForgedSwordItem> STEEL_SWORD = ITEMS.register("steel_sword",
        () -> new ForgedSwordItem(Materials.STEEL));
    public static final DeferredHolder<Item, ForgedSwordItem> HARDENED_SWORD = ITEMS.register("hardened_sword",
        () -> new ForgedSwordItem(Materials.HARDENED_STEEL));
    public static final DeferredHolder<Item, ForgedAxeItem> COPPER_AXE = ITEMS.register("copper_axe",
        () -> new ForgedAxeItem(Materials.COPPER));
    public static final DeferredHolder<Item, ForgedAxeItem> IRON_AXE = ITEMS.register("iron_axe",
        () -> new ForgedAxeItem(Materials.IRON));
    public static final DeferredHolder<Item, ForgedAxeItem> STEEL_AXE = ITEMS.register("steel_axe",
        () -> new ForgedAxeItem(Materials.STEEL));
    public static final DeferredHolder<Item, ForgedAxeItem> HARDENED_AXE = ITEMS.register("hardened_axe",
        () -> new ForgedAxeItem(Materials.HARDENED_STEEL));
    public static final DeferredHolder<Item, ForgedPickaxeItem> COPPER_PICKAXE = ITEMS.register("copper_pickaxe",
        () -> new ForgedPickaxeItem(Materials.COPPER));
    public static final DeferredHolder<Item, ForgedPickaxeItem> IRON_PICKAXE = ITEMS.register("iron_pickaxe",
        () -> new ForgedPickaxeItem(Materials.IRON));
    public static final DeferredHolder<Item, ForgedPickaxeItem> STEEL_PICKAXE = ITEMS.register("steel_pickaxe",
        () -> new ForgedPickaxeItem(Materials.STEEL));
    public static final DeferredHolder<Item, ForgedPickaxeItem> HARDENED_PICKAXE = ITEMS.register("hardened_pickaxe",
        () -> new ForgedPickaxeItem(Materials.HARDENED_STEEL));
    public static final DeferredHolder<Item, ForgedSpearItem> COPPER_SPEAR = ITEMS.register("copper_spear",
        () -> new ForgedSpearItem(Materials.COPPER));
    public static final DeferredHolder<Item, ForgedSpearItem> IRON_SPEAR = ITEMS.register("iron_spear",
        () -> new ForgedSpearItem(Materials.IRON));
    public static final DeferredHolder<Item, ForgedSpearItem> STEEL_SPEAR = ITEMS.register("steel_spear",
        () -> new ForgedSpearItem(Materials.STEEL));
    public static final DeferredHolder<Item, ForgedSpearItem> HARDENED_SPEAR = ITEMS.register("hardened_spear",
        () -> new ForgedSpearItem(Materials.HARDENED_STEEL));

    // Consumables & special
    public static final DeferredHolder<Item, CokeItem> COKE = ITEMS.register("coke", CokeItem::new);
    public static final DeferredHolder<Item, MaterialItem> QUENCH_OIL = ITEMS.register("quench_oil", () -> new MaterialItem("oil"));
    public static final DeferredHolder<Item, BlacksmithJournalItem> JOURNAL = ITEMS.register("blacksmith_journal",
        () -> new BlacksmithJournalItem(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, BlueprintItem> KINGS_EDGE_BLUEPRINT = ITEMS.register("kings_edge_blueprint",
        () -> new BlueprintItem("kings_edge"));

    public static final DeferredHolder<Item, SpawnEggItem> BLACKSMITH_SPAWN_EGG = ITEMS.register("blacksmith_spawn_egg",
        () -> new DeferredSpawnEggItem(entityHolder(), 0x4a3220, 0xd97b2b, new Item.Properties()));

    @SuppressWarnings("unchecked")
    private static Supplier<EntityType<? extends Mob>> entityHolder() {
        return () -> (EntityType<? extends Mob>) (EntityType<?>) ModEntities.BLACKSMITH.get();
    }

    /** Resolve a finished tool item for a material + tool kind. Never returns null for known ids. */
    public static Item finishedTool(String materialId, String toolKind) {
        return switch (toolKind) {
            case "axe" -> switch (materialId) {
                case "copper" -> COPPER_AXE.get();
                case "steel" -> STEEL_AXE.get();
                case "hardened_steel" -> HARDENED_AXE.get();
                default -> IRON_AXE.get();
            };
            case "pickaxe" -> switch (materialId) {
                case "copper" -> COPPER_PICKAXE.get();
                case "steel" -> STEEL_PICKAXE.get();
                case "hardened_steel" -> HARDENED_PICKAXE.get();
                default -> IRON_PICKAXE.get();
            };
            case "spear" -> switch (materialId) {
                case "copper" -> COPPER_SPEAR.get();
                case "steel" -> STEEL_SPEAR.get();
                case "hardened_steel" -> HARDENED_SPEAR.get();
                default -> IRON_SPEAR.get();
            };
            default -> switch (materialId) {
                case "copper" -> COPPER_SWORD.get();
                case "steel" -> STEEL_SWORD.get();
                case "hardened_steel" -> HARDENED_SWORD.get();
                default -> IRON_SWORD.get();
            };
        };
    }

    public static Item billetFor(String materialId) {
        return switch (materialId) {
            case "copper" -> COPPER_BILLET.get();
            case "steel" -> STEEL_BILLET.get();
            case "hardened_steel" -> HARDENED_BILLET.get();
            default -> IRON_BILLET.get();
        };
    }
}

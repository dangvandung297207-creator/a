package com.truemetallurgy.datagen;

import com.truemetallurgy.TrueMetallurgy;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

/** English localisation. Mirrored in {@code assets/.../lang/en_us.json}. */
public class TMLang extends LanguageProvider {
    public TMLang(PackOutput output) {
        super(output, TrueMetallurgy.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add("itemGroup.true_metallurgy", "True Metallurgy");

        // Blocks
        add("block.true_metallurgy.primitive_forge", "Primitive Forge");
        add("block.true_metallurgy.iron_forge", "Iron Forge");
        add("block.true_metallurgy.steel_forge", "Steel Forge");
        add("block.true_metallurgy.master_forge", "Master Forge");
        add("block.true_metallurgy.bellows", "Bellows");
        add("block.true_metallurgy.basic_anvil", "Basic Anvil");
        add("block.true_metallurgy.iron_anvil", "Iron Anvil");
        add("block.true_metallurgy.steel_anvil", "Steel Anvil");
        add("block.true_metallurgy.master_anvil", "Master Anvil");
        add("block.true_metallurgy.quenching_barrel", "Quenching Barrel");
        add("block.true_metallurgy.grinding_wheel", "Grinding Wheel");
        add("block.true_metallurgy.assembly_table", "Assembly Table");
        add("block.true_metallurgy.steel_block", "Block of Steel");
        add("block.true_metallurgy.tool_rack", "Tool Rack");

        // Tools
        add("item.true_metallurgy.blacksmith_tongs", "Blacksmith Tongs");
        add("item.true_metallurgy.primitive_hammer", "Primitive Hammer");
        add("item.true_metallurgy.copper_hammer", "Copper Hammer");
        add("item.true_metallurgy.iron_hammer", "Iron Hammer");
        add("item.true_metallurgy.steel_hammer", "Steel Hammer");
        add("item.true_metallurgy.hardened_hammer", "Hardened Hammer");
        add("item.true_metallurgy.masterwork_hammer", "Masterwork Hammer");

        // Pipeline
        add("item.true_metallurgy.crushed_iron_ore", "Crushed Iron Ore");
        add("item.true_metallurgy.crushed_copper_ore", "Crushed Copper Ore");
        add("item.true_metallurgy.iron_bloom", "Iron Bloom");
        add("item.true_metallurgy.copper_bloom", "Copper Bloom");
        add("item.true_metallurgy.steel_bloom", "Steel Bloom");
        add("item.true_metallurgy.hardened_bloom", "Hardened Bloom");
        add("item.true_metallurgy.copper_billet", "Copper Billet");
        add("item.true_metallurgy.iron_billet", "Iron Billet");
        add("item.true_metallurgy.steel_billet", "Steel Billet");
        add("item.true_metallurgy.hardened_billet", "Hardened Billet");
        add("item.true_metallurgy.sword_blade", "Sword Blade");
        add("item.true_metallurgy.axe_head", "Axe Head");
        add("item.true_metallurgy.pickaxe_head", "Pickaxe Head");
        add("item.true_metallurgy.spear_head", "Spear Head");
        add("item.true_metallurgy.iron_guard", "Iron Guard");
        add("item.true_metallurgy.steel_guard", "Steel Guard");
        add("item.true_metallurgy.iron_pommel", "Iron Pommel");
        add("item.true_metallurgy.steel_pommel", "Steel Pommel");
        add("item.true_metallurgy.oak_handle", "Oak Handle");
        add("item.true_metallurgy.spruce_handle", "Spruce Handle");
        add("item.true_metallurgy.birch_handle", "Birch Handle");
        add("item.true_metallurgy.dark_oak_handle", "Dark Oak Handle");
        add("item.true_metallurgy.bamboo_handle", "Bamboo Handle");
        add("item.true_metallurgy.reinforced_handle", "Reinforced Handle");
        add("item.true_metallurgy.leather_wrapped_handle", "Leather-Wrapped Handle");
        add("item.true_metallurgy.bone_handle", "Bone Handle");
        add("item.true_metallurgy.long_shaft", "Long Shaft");
        add("item.true_metallurgy.binding", "Leather Binding");

        // Finished
        add("item.true_metallurgy.copper_sword", "Copper Sword");
        add("item.true_metallurgy.iron_sword", "Iron Sword");
        add("item.true_metallurgy.steel_sword", "Steel Sword");
        add("item.true_metallurgy.hardened_sword", "Hardened Sword");
        add("item.true_metallurgy.copper_axe", "Copper Axe");
        add("item.true_metallurgy.iron_axe", "Iron Axe");
        add("item.true_metallurgy.steel_axe", "Steel Axe");
        add("item.true_metallurgy.hardened_axe", "Hardened Axe");
        add("item.true_metallurgy.copper_pickaxe", "Copper Pickaxe");
        add("item.true_metallurgy.iron_pickaxe", "Iron Pickaxe");
        add("item.true_metallurgy.steel_pickaxe", "Steel Pickaxe");
        add("item.true_metallurgy.hardened_pickaxe", "Hardened Pickaxe");
        add("item.true_metallurgy.copper_spear", "Copper Spear");
        add("item.true_metallurgy.iron_spear", "Iron Spear");
        add("item.true_metallurgy.steel_spear", "Steel Spear");
        add("item.true_metallurgy.hardened_spear", "Hardened Spear");
        add("item.true_metallurgy.forged_name", "%s %s %s");

        // Special
        add("item.true_metallurgy.coke", "Coke");
        add("item.true_metallurgy.quench_oil", "Quench Oil");
        add("item.true_metallurgy.blacksmith_journal", "The Blacksmith's Journal");
        add("item.true_metallurgy.kings_edge_blueprint", "Blueprint: The King's Edge");
        add("item.true_metallurgy.blacksmith_spawn_egg", "Blacksmith Spawn Egg");
        add("entity.true_metallurgy.blacksmith", "Blacksmith");

        // Menus + GUI
        add("menu.true_metallurgy.forge", "Forge Hearth");
        add("menu.true_metallurgy.forging", "Forging");
        add("menu.true_metallurgy.quenching", "Quenching Barrel");
        add("menu.true_metallurgy.grinding", "Grinding Wheel");
        add("menu.true_metallurgy.assembly", "Assembly Table");
        add("gui.true_metallurgy.take", "Take");
        add("gui.true_metallurgy.quench", "Quench");
        add("gui.true_metallurgy.grind", "Grind");
        add("gui.true_metallurgy.assemble", "Assemble");
        add("gui.true_metallurgy.score", "Quality: %s");
        add("gui.true_metallurgy.no_workpiece", "No workpiece");
        add("gui.true_metallurgy.empty", "Empty");
        add("gui.true_metallurgy.quench_hint", "Hot finished component + liquid");
        add("gui.true_metallurgy.edge_angle", "Edge: %s");
        add("gui.true_metallurgy.wear", "Wear: %s%%");
        add("gui.true_metallurgy.worn", "Worn! Redress with cobblestone");
        add("gui.true_metallurgy.blade", "Blade");
        add("gui.true_metallurgy.blueprint", "Design");
        add("gui.true_metallurgy.output", "Result");

        // Tooltips
        add("tooltip.true_metallurgy.temperature", "%s C - %s");
        add("tooltip.true_metallurgy.purity", "Purity: %s%%");
        add("tooltip.true_metallurgy.purity_craft", "Purity %s%% - Craft %s/100");
        add("tooltip.true_metallurgy.reheats", "Reheats: %s");
        add("tooltip.true_metallurgy.tongs", "Grip hot metal safely");
        add("tooltip.true_metallurgy.hammer_stats", "Tolerance %s - Strength %s - %skg");
        add("tooltip.true_metallurgy.material", "Material: %s");
        add("tooltip.true_metallurgy.stage", "Stage: %s (%s/%s)");
        add("tooltip.true_metallurgy.stage_done", "Shaping complete");
        add("tooltip.true_metallurgy.quality_progress", "Quality: %s");
        add("tooltip.true_metallurgy.quenched", "Quenched in %s");
        add("tooltip.true_metallurgy.edge", "Edge: %s deg");
        add("tooltip.true_metallurgy.strikes", "Strikes %s (P%s/G%s/M%s/B%s) Reheats %s");
        add("tooltip.true_metallurgy.forged_by", "Forged by %s");
        add("tooltip.true_metallurgy.handle_stats", "Weight %s - Durability %s - Recovery %s");
        add("tooltip.true_metallurgy.blueprint", "Legendary design: %s");
        add("tooltip.true_metallurgy.blueprint_hint", "Consumed at assembly for a one-of-a-kind piece");
        add("tooltip.true_metallurgy.unforged", "Unforged stock - assemble it properly");
        add("tooltip.true_metallurgy.weight", "Weight: %skg");
        add("tooltip.true_metallurgy.maker_mark", "Maker's mark");
        add("tooltip.true_metallurgy.material_stats", "Hard %s - Tough %s - Sharp %s - Flex %s");
        add("tooltip.true_metallurgy.journal_best", "Best: %s (%s)");
        add("tooltip.true_metallurgy.journal_empty", "Empty pages, waiting for a legend");
        add("tooltip.true_metallurgy.craft_score", "Craftsmanship: %s/100");
        add("tooltip.true_metallurgy.crushed", "Crushed ore, ready for the bloomery");
        add("tooltip.true_metallurgy.bloom", "Spongy bloom - consolidate it on the anvil");
        add("tooltip.true_metallurgy.guard", "Sword guard fitting");
        add("tooltip.true_metallurgy.pommel", "Sword pommel fitting");
        add("tooltip.true_metallurgy.shaft", "Long spear shaft");
        add("tooltip.true_metallurgy.binding", "Leather binding for spear sockets");
        add("tooltip.true_metallurgy.oil", "Slow, kind quenching oil");
        add("tooltip.true_metallurgy.fuel", "Fuel: %s");
        add("tooltip.true_metallurgy.airflow", "Airflow: %s");

        // Heat + states
        add("heat.true_metallurgy.cold", "Cold");
        add("heat.true_metallurgy.warm", "Warm");
        add("heat.true_metallurgy.forging", "Forging Range");
        add("heat.true_metallurgy.high_heat", "High Heat");
        add("heat.true_metallurgy.overheated", "Overheated");
        add("forge.true_metallurgy.cold", "Cold");
        add("forge.true_metallurgy.warm", "Warm");
        add("forge.true_metallurgy.hot", "Hot");
        add("forge.true_metallurgy.forging", "Forging Heat");
        add("forge.true_metallurgy.overheated", "Overheated");
        add("forge.true_metallurgy.molten", "Molten");

        // Strikes + quality
        add("strike.true_metallurgy.perfect", "Perfect hit!");
        add("strike.true_metallurgy.good", "Good hit");
        add("strike.true_metallurgy.miss", "Miss");
        add("strike.true_metallurgy.bad", "Bad hit!");
        add("quality.true_metallurgy.flawed", "Flawed");
        add("quality.true_metallurgy.standard", "Standard");
        add("quality.true_metallurgy.masterwork", "Masterwork");
        add("quality.true_metallurgy.legendary", "Legendary");

        // Materials + tools + media
        add("material.true_metallurgy.copper", "Copper");
        add("material.true_metallurgy.iron", "Iron");
        add("material.true_metallurgy.steel", "Steel");
        add("material.true_metallurgy.hardened_steel", "Hardened Steel");
        add("tool.true_metallurgy.sword", "Longsword");
        add("tool.true_metallurgy.axe", "Axe");
        add("tool.true_metallurgy.pickaxe", "Pickaxe");
        add("tool.true_metallurgy.spear", "Spear");
        add("quench.true_metallurgy.water", "Water");
        add("quench.true_metallurgy.oil", "Oil");
        add("grind.true_metallurgy.keen", "Keen");
        add("grind.true_metallurgy.balanced", "Balanced");
        add("grind.true_metallurgy.sturdy", "Sturdy");
        add("handle.true_metallurgy.oak", "Oak");
        add("handle.true_metallurgy.spruce", "Spruce");
        add("handle.true_metallurgy.birch", "Birch");
        add("handle.true_metallurgy.dark_oak", "Dark Oak");
        add("handle.true_metallurgy.bamboo", "Bamboo");
        add("handle.true_metallurgy.reinforced", "Reinforced");
        add("handle.true_metallurgy.leather_wrapped", "Leather-Wrapped");
        add("handle.true_metallurgy.bone", "Bone");

        // Forging stages + kinds
        add("stage.true_metallurgy.blade", "Blade");
        add("stage.true_metallurgy.edge", "Edge");
        add("stage.true_metallurgy.tang", "Tang");
        add("stage.true_metallurgy.finish_blade", "Finish Blade");
        add("stage.true_metallurgy.head", "Head");
        add("stage.true_metallurgy.eye", "Eye");
        add("stage.true_metallurgy.finish_head", "Finish Head");
        add("stage.true_metallurgy.pick_head", "Pick Head");
        add("stage.true_metallurgy.reinforced_eye", "Reinforced Eye");
        add("stage.true_metallurgy.spear_head", "Spear Head");
        add("stage.true_metallurgy.socket", "Socket");
        add("stage.true_metallurgy.consolidate", "Consolidate");
        add("stage.true_metallurgy.draw_out", "Draw Out");
        add("kind.true_metallurgy.short_0", "Blade");
        add("kind.true_metallurgy.short_1", "Axe");
        add("kind.true_metallurgy.short_2", "Pick");
        add("kind.true_metallurgy.short_3", "Spear");

        // Journal
        add("journal.true_metallurgy.title", "The Blacksmith's Journal");
        add("journal.true_metallurgy.intro", "Ore becomes bloom, bloom becomes billet, billet becomes blade. Mind the heat, strike true, quench with intent. Every hammer strike matters.");
        add("journal.true_metallurgy.best", "Finest work: %s (%s)");
        add("journal.true_metallurgy.materials", "Known Materials");
        add("journal.true_metallurgy.forging", "Forging");
        add("journal.true_metallurgy.forging_text", "Strike inside the forging range. Aim for the marked zone, keep a steady rhythm, and reheat before the glow dies. Overworking and cold hits crack the steel.");
        add("journal.true_metallurgy.quenching", "Quenching");
        add("journal.true_metallurgy.quenching_text", "Water quenches fast and hard: sharper, but brittle. Oil quenches slow and kind: tougher and longer-lived. Quench only while the piece still glows.");
        add("journal.true_metallurgy.grinding", "Grinding");
        add("journal.true_metallurgy.grinding_text", "A keen 15-degree edge bites deep but chips. A sturdy 35-degree edge endures. 25 degrees is the honest compromise. Dress a worn wheel with cobblestone.");
        add("journal.true_metallurgy.creations", "Creations");
        add("journal.true_metallurgy.no_creations", "No creations recorded yet.");

        // Messages
        add("message.true_metallurgy.tongs_grip", "Gripped at %s C");
        add("message.true_metallurgy.need_tongs", "Too hot! Use blacksmith tongs");
        add("message.true_metallurgy.forge_full", "The forge is full");
        add("message.true_metallurgy.forge_occupied", "The forge already holds a workpiece");
        add("message.true_metallurgy.anvil_occupied", "The anvil already holds a workpiece");
        add("message.true_metallurgy.anvil_empty", "No workpiece on the anvil");
        add("message.true_metallurgy.anvil_too_weak", "This anvil cannot shape such hard metal");
        add("message.true_metallurgy.too_cold", "Too cold - reheat the workpiece!");
        add("message.true_metallurgy.too_hot", "Too hot - oxidation is eating the steel!");
        add("message.true_metallurgy.kind_locked", "Shaping already begun - kind is locked");
        add("message.true_metallurgy.component_finished", "Component finished! Quality %s - quench while hot");
        add("message.true_metallurgy.barrel_mixed", "The barrel holds a different liquid");
        add("message.true_metallurgy.barrel_full", "The barrel is full");
        add("message.true_metallurgy.barrel_occupied", "The barrel already holds a piece");
        add("message.true_metallurgy.barrel_empty", "The barrel is empty");
        add("message.true_metallurgy.quench_not_component", "Only finished forged components can be quenched");
        add("message.true_metallurgy.quench_unfinished", "Finish shaping before quenching");
        add("message.true_metallurgy.quench_already", "Already quenched");
        add("message.true_metallurgy.quench_too_cold", "Too cold to quench - reheat first");
        add("message.true_metallurgy.quenched", "Quenched! Quality %s");
        add("message.true_metallurgy.grind_not_component", "Only forged components can be ground");
        add("message.true_metallurgy.grind_unfinished", "Finish shaping before grinding");
        add("message.true_metallurgy.wheel_dressed", "Wheel dressed and true");
        add("message.true_metallurgy.assembly_no_blade", "Assembly needs a forged blade or head");
        add("message.true_metallurgy.assembly_unfinished", "Finish shaping the component first");
        add("message.true_metallurgy.assembly_unquenched", "Quench the component before assembly");
        add("message.true_metallurgy.assembly_no_guard", "A sword needs a guard");
        add("message.true_metallurgy.assembly_no_handle", "Assembly needs a handle");
        add("message.true_metallurgy.assembly_no_pommel", "A sword needs a pommel");
        add("message.true_metallurgy.assembly_no_shaft", "A spear needs a long shaft");
        add("message.true_metallurgy.assembly_no_binding", "A spear needs a binding");
        add("message.true_metallurgy.assembly_unknown", "Unknown assembly");
        add("message.true_metallurgy.assembly_no_recipe", "Those parts do not fit together");
        add("message.true_metallurgy.output_occupied", "Clear the result slot first");
        add("message.true_metallurgy.assembled", "Weapon assembled");
        add("message.true_metallurgy.legendary_forged", "A LEGEND is forged!");
        add("message.true_metallurgy.journal_recorded", "Recorded in the journal");

        // Sound subtitles
        add("subtitle.true_metallurgy.forge_crackle", "Forge crackles");
        add("subtitle.true_metallurgy.bellows_whoosh", "Bellows whoosh");
        add("subtitle.true_metallurgy.hammer", "Hammer rings");
        add("subtitle.true_metallurgy.quench", "Metal hisses");
        add("subtitle.true_metallurgy.grind", "Wheel grinds");
        add("subtitle.true_metallurgy.assembly", "Workshop sounds");
        add("subtitle.true_metallurgy.chime", "Masterwork chimes");

        // Advancements
        add("advancement.true_metallurgy.root", "True Metallurgy");
        add("advancement.true_metallurgy.root.desc", "Ore, fire and iron will");
        add("advancement.true_metallurgy.first_billet", "First Billet");
        add("advancement.true_metallurgy.first_billet.desc", "Consolidate a bloom into a billet");
        add("advancement.true_metallurgy.masterwork", "Masterwork");
        add("advancement.true_metallurgy.masterwork.desc", "Assemble a masterwork piece");
        add("advancement.true_metallurgy.legendary", "Legendary");
        add("advancement.true_metallurgy.legendary.desc", "Forge a one-of-a-kind legend");
    }
}

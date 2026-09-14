package com.truemetallurgy.datagen;

import com.truemetallurgy.TrueMetallurgy;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

/** Item model generation, including hot-metal heat overrides. */
public class TMItemModels extends ItemModelProvider {
    private static final ModelFile GENERATED = new ModelFile.UncheckedModelFile("minecraft:item/generated");
    private static final ModelFile HANDHELD = new ModelFile.UncheckedModelFile("minecraft:item/handheld");

    public TMItemModels(PackOutput output, ExistingFileHelper files) {
        super(output, TrueMetallurgy.MOD_ID, files);
    }

    @Override
    protected void registerModels() {
        // Block items reuse their block models.
        blockItem("primitive_forge", "block/forge_primitive_0");
        blockItem("iron_forge", "block/forge_iron_0");
        blockItem("steel_forge", "block/forge_steel_0");
        blockItem("master_forge", "block/forge_master_0");
        blockItem("bellows", "block/bellows_0");
        blockItem("basic_anvil", "block/anvil_basic");
        blockItem("iron_anvil", "block/anvil_iron");
        blockItem("steel_anvil", "block/anvil_steel");
        blockItem("master_anvil", "block/anvil_master");
        blockItem("quenching_barrel", "block/quenching_barrel_none");
        blockItem("grinding_wheel", "block/grinding_wheel_0");
        blockItem("assembly_table", "block/assembly_table");
        blockItem("tool_rack", "block/tool_rack");
        blockItem("steel_block", "block/steel_block");

        // Handheld tools.
        handheld("blacksmith_tongs");
        handheld("primitive_hammer");
        handheld("copper_hammer");
        handheld("iron_hammer");
        handheld("steel_hammer");
        handheld("hardened_hammer");
        handheld("masterwork_hammer");

        // Flat goods.
        flat("crushed_iron_ore");
        flat("crushed_copper_ore");
        flat("iron_bloom");
        flat("copper_bloom");
        flat("steel_bloom");
        flat("hardened_bloom");
        flat("iron_guard");
        flat("steel_guard");
        flat("iron_pommel");
        flat("steel_pommel");
        flat("oak_handle");
        flat("spruce_handle");
        flat("birch_handle");
        flat("dark_oak_handle");
        flat("bamboo_handle");
        flat("reinforced_handle");
        flat("leather_wrapped_handle");
        flat("bone_handle");
        flat("long_shaft");
        flat("binding");
        flat("coke");
        flat("quench_oil");
        flat("blacksmith_journal");
        flat("kings_edge_blueprint");

        // Billets: 5 heat stages per material.
        heatStages("copper_billet");
        heatStages("iron_billet");
        heatStages("steel_billet");
        heatStages("hardened_billet");

        // Components: tinted by material/heat in code, single texture each.
        flat("sword_blade");
        flat("axe_head");
        flat("pickaxe_head");
        flat("spear_head");

        // Finished tools, per material.
        for (String mat : new String[]{"copper", "iron", "steel", "hardened"}) {
            getBuilder(mat + "_sword").parent(HANDHELD).texture("layer0", modLoc("item/" + mat + "_sword"));
            getBuilder(mat + "_axe").parent(HANDHELD).texture("layer0", modLoc("item/" + mat + "_axe"));
            getBuilder(mat + "_pickaxe").parent(HANDHELD).texture("layer0", modLoc("item/" + mat + "_pickaxe"));
            getBuilder(mat + "_spear").parent(HANDHELD).texture("layer0", modLoc("item/" + mat + "_spear"));
        }

        withExistingParent("blacksmith_spawn_egg", ResourceLocation.parse("minecraft:item/template_spawn_egg"));
    }

    private void blockItem(String name, String model) {
        withExistingParent(name, modLoc(model));
    }

    private void flat(String name) {
        getBuilder(name).parent(GENERATED).texture("layer0", modLoc("item/" + name));
    }

    private void handheld(String name) {
        getBuilder(name).parent(HANDHELD).texture("layer0", modLoc("item/" + name));
    }

    private void heatStages(String name) {
        ItemModelBuilder base = getBuilder(name).parent(GENERATED).texture("layer0", modLoc("item/" + name + "_0"));
        for (int i = 1; i <= 4; i++) {
            ItemModelBuilder stage = getBuilder(name + "_" + i).parent(GENERATED)
                .texture("layer0", modLoc("item/" + name + "_" + i));
            base.override().model(stage).predicate(modLoc("heat"), 0.25F * i).end();
        }
    }
}

package com.masterblacksmith;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/** Workshop, metallurgy and masterwork creative tabs. */
public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MasterBlacksmith.MOD_ID);

    public static final RegistryObject<CreativeModeTab> WORKSHOP = TABS.register("workshop",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.masterblacksmith.workshop"))
                    .icon(() -> new ItemStack(ModBlocks.ANVIL_STEEL.get()))
                    .displayItems((params, out) -> {
                        out.accept(ModBlocks.REFRACTORY_BRICK_BLOCK.get());
                        out.accept(ModBlocks.FORGE_HEARTH_PRIMITIVE.get());
                        out.accept(ModBlocks.FORGE_HEARTH_IRON.get());
                        out.accept(ModBlocks.FORGE_HEARTH_STEEL.get());
                        out.accept(ModBlocks.FORGE_HEARTH_MASTER.get());
                        out.accept(ModBlocks.BELLOWS.get());
                        out.accept(ModBlocks.ANVIL_BASIC.get());
                        out.accept(ModBlocks.ANVIL_IRON.get());
                        out.accept(ModBlocks.ANVIL_STEEL.get());
                        out.accept(ModBlocks.ANVIL_MASTER.get());
                        out.accept(ModBlocks.QUENCHING_BARREL.get());
                        out.accept(ModBlocks.GRINDING_WHEEL.get());
                        out.accept(ModBlocks.ASSEMBLY_TABLE.get());
                        out.accept(ModBlocks.TOOL_RACK.get());
                        out.accept(ModBlocks.METAL_SHELF.get());
                        out.accept(ModItems.TONGS.get());
                        out.accept(ModItems.JOURNAL.get());
                    }).build());

    public static final RegistryObject<CreativeModeTab> METALLURGY = TABS.register("metallurgy",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.masterblacksmith.metallurgy"))
                    .icon(() -> new ItemStack(ModItems.STEEL_INGOT.get()))
                    .displayItems((params, out) -> {
                        out.accept(ModBlocks.TIN_ORE.get());
                        out.accept(ModBlocks.DEEPSLATE_TIN_ORE.get());
                        out.accept(ModBlocks.STARFALL_ORE.get());
                        out.accept(ModItems.RAW_TIN.get());
                        out.accept(ModItems.STARFALL_SHARD.get());
                        out.accept(ModItems.TIN_INGOT.get());
                        out.accept(ModItems.BRONZE_INGOT.get());
                        out.accept(ModItems.STEEL_INGOT.get());
                        out.accept(ModItems.HARDENED_STEEL_INGOT.get());
                        out.accept(ModItems.DAMASCUS_STEEL_INGOT.get());
                        out.accept(ModItems.STARFALL_STEEL_INGOT.get());
                        out.accept(ModBlocks.TIN_BLOCK.get());
                        out.accept(ModBlocks.BRONZE_BLOCK.get());
                        out.accept(ModBlocks.STEEL_BLOCK.get());
                        out.accept(ModBlocks.STARFALL_BLOCK.get());
                        out.accept(ModItems.REFRACTORY_BRICK.get());
                        out.accept(ModItems.COKE.get());
                        for (String m : ModItems.METALS) {
                            out.accept(ModItems.ITEMS.getEntries().stream()
                                    .filter(o -> o.getId().getPath().equals("billet_" + m))
                                    .findFirst().orElseThrow().get());
                        }
                        out.accept(ModItems.OIL_BUCKET.get());
                        out.accept(ModItems.SALT_WATER_BUCKET.get());
                        out.accept(ModItems.HERBAL_OIL_BUCKET.get());
                        out.accept(ModItems.MINERAL_OIL_BUCKET.get());
                        out.accept(ModItems.ALCHEMICAL_OIL_BUCKET.get());
                        out.accept(ModItems.BLOOD_QUENCH_VIAL.get());
                        out.accept(ModItems.STARFALL_QUENCH_VIAL.get());
                    }).build());

    public static final RegistryObject<CreativeModeTab> MASTERWORK = TABS.register("masterwork",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.masterblacksmith.masterwork"))
                    .icon(() -> new ItemStack(ModItems.HAMMER_MASTERWORK.get()))
                    .displayItems((params, out) -> {
                        out.accept(ModItems.HAMMER_COPPER.get());
                        out.accept(ModItems.HAMMER_IRON.get());
                        out.accept(ModItems.HAMMER_STEEL.get());
                        out.accept(ModItems.HAMMER_HARDENED.get());
                        out.accept(ModItems.HAMMER_MASTERWORK.get());
                        out.accept(ModItems.BLADE_BLANK.get());
                        out.accept(ModItems.AXE_HEAD_BLANK.get());
                        out.accept(ModItems.PICK_HEAD_BLANK.get());
                        out.accept(ModItems.SPEAR_HEAD_BLANK.get());
                        out.accept(ModItems.ARMOR_PLATE_BLANK.get());
                        out.accept(ModItems.GUARD_IRON.get());
                        out.accept(ModItems.GUARD_STEEL.get());
                        out.accept(ModItems.GUARD_BRASS.get());
                        out.accept(ModItems.GRIP_LEATHER.get());
                        out.accept(ModItems.GRIP_BONE.get());
                        out.accept(ModItems.GRIP_EXOTIC.get());
                        out.accept(ModItems.POMMEL_IRON.get());
                        out.accept(ModItems.POMMEL_STEEL.get());
                        out.accept(ModItems.POMMEL_BRASS.get());
                        for (String h : ModItems.HANDLES) {
                            out.accept(ModItems.ITEMS.getEntries().stream()
                                    .filter(o -> o.getId().getPath().equals("handle_" + h))
                                    .findFirst().orElseThrow().get());
                        }
                        out.accept(ModItems.FORGED_SWORD.get());
                        out.accept(ModItems.FORGED_AXE.get());
                        out.accept(ModItems.FORGED_PICKAXE.get());
                        out.accept(ModItems.FORGED_SPEAR.get());
                        out.accept(ModItems.FORGED_HELMET.get());
                        out.accept(ModItems.FORGED_CHESTPLATE.get());
                        out.accept(ModItems.FORGED_LEGGINGS.get());
                        out.accept(ModItems.FORGED_BOOTS.get());
                        out.accept(ModItems.BLUEPRINT_KINGS_EDGE.get());
                        out.accept(ModItems.BLUEPRINT_OATHKEEPER.get());
                        out.accept(ModItems.BLUEPRINT_STONESPLITTER.get());
                        out.accept(ModItems.BLUEPRINT_SKYPIERCER.get());
                        out.accept(ModItems.BLUEPRINT_AEGIS.get());
                    }).build());
}

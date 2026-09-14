package com.truemetallurgy.registry;

import com.truemetallurgy.TrueMetallurgy;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Single development/testing tab. Survival progression never depends on it. */
public final class ModCreativeTabs {
    private ModCreativeTabs() {}

    public static final DeferredRegister<CreativeModeTab> TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TrueMetallurgy.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register("main",
        () -> CreativeModeTab.builder()
            .icon(() -> new ItemStack(ModItems.MASTERWORK_HAMMER.get()))
            .title(Component.translatable("itemGroup.true_metallurgy"))
            .displayItems((params, output) -> {
                output.accept(ModItems.PRIMITIVE_FORGE.get());
                output.accept(ModItems.IRON_FORGE.get());
                output.accept(ModItems.STEEL_FORGE.get());
                output.accept(ModItems.MASTER_FORGE.get());
                output.accept(ModItems.BELLOWS.get());
                output.accept(ModItems.BASIC_ANVIL.get());
                output.accept(ModItems.IRON_ANVIL.get());
                output.accept(ModItems.STEEL_ANVIL.get());
                output.accept(ModItems.MASTER_ANVIL.get());
                output.accept(ModItems.QUENCHING_BARREL.get());
                output.accept(ModItems.GRINDING_WHEEL.get());
                output.accept(ModItems.ASSEMBLY_TABLE.get());
                output.accept(ModItems.STEEL_BLOCK.get());
                output.accept(ModItems.TOOL_RACK.get());
                output.accept(ModItems.TONGS.get());
                output.accept(ModItems.PRIMITIVE_HAMMER.get());
                output.accept(ModItems.COPPER_HAMMER.get());
                output.accept(ModItems.IRON_HAMMER.get());
                output.accept(ModItems.STEEL_HAMMER.get());
                output.accept(ModItems.HARDENED_HAMMER.get());
                output.accept(ModItems.MASTERWORK_HAMMER.get());
                output.accept(ModItems.CRUSHED_IRON_ORE.get());
                output.accept(ModItems.CRUSHED_COPPER_ORE.get());
                output.accept(ModItems.IRON_BLOOM.get());
                output.accept(ModItems.COPPER_BLOOM.get());
                output.accept(ModItems.STEEL_BLOOM.get());
                output.accept(ModItems.HARDENED_BLOOM.get());
                output.accept(ModItems.COPPER_BILLET.get());
                output.accept(ModItems.IRON_BILLET.get());
                output.accept(ModItems.STEEL_BILLET.get());
                output.accept(ModItems.HARDENED_BILLET.get());
                output.accept(ModItems.SWORD_BLADE.get());
                output.accept(ModItems.AXE_HEAD.get());
                output.accept(ModItems.PICKAXE_HEAD.get());
                output.accept(ModItems.SPEAR_HEAD.get());
                output.accept(ModItems.IRON_GUARD.get());
                output.accept(ModItems.STEEL_GUARD.get());
                output.accept(ModItems.IRON_POMMEL.get());
                output.accept(ModItems.STEEL_POMMEL.get());
                output.accept(ModItems.OAK_HANDLE.get());
                output.accept(ModItems.SPRUCE_HANDLE.get());
                output.accept(ModItems.BIRCH_HANDLE.get());
                output.accept(ModItems.DARK_OAK_HANDLE.get());
                output.accept(ModItems.BAMBOO_HANDLE.get());
                output.accept(ModItems.REINFORCED_HANDLE.get());
                output.accept(ModItems.LEATHER_WRAPPED_HANDLE.get());
                output.accept(ModItems.BONE_HANDLE.get());
                output.accept(ModItems.LONG_SHAFT.get());
                output.accept(ModItems.BINDING.get());
                output.accept(ModItems.COKE.get());
                output.accept(ModItems.QUENCH_OIL.get());
                output.accept(ModItems.JOURNAL.get());
                output.accept(ModItems.KINGS_EDGE_BLUEPRINT.get());
                output.accept(ModItems.BLACKSMITH_SPAWN_EGG.get());
            })
            .build());
}

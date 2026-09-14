package com.truemetallurgy.datagen;

import com.truemetallurgy.TrueMetallurgy;
import com.truemetallurgy.registry.ModItems;
import com.truemetallurgy.util.TMUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagLookup;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ItemTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

/** Component tags used by assembly recipes and workstation filters. */
public class TMItemTags extends ItemTagsProvider {
    public TMItemTags(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup,
            CompletableFuture<TagLookup<Block>> blockTags, ExistingFileHelper files) {
        super(output, lookup, blockTags, TrueMetallurgy.MOD_ID, files);
    }

    private static TagKey<Item> key(String name) {
        return TagKey.create(Registries.ITEM, TMUtil.rl(name));
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(key("hammers"))
            .add(ModItems.PRIMITIVE_HAMMER.get(), ModItems.COPPER_HAMMER.get(), ModItems.IRON_HAMMER.get(),
                ModItems.STEEL_HAMMER.get(), ModItems.HARDENED_HAMMER.get(), ModItems.MASTERWORK_HAMMER.get());
        tag(key("handles"))
            .add(ModItems.OAK_HANDLE.get(), ModItems.SPRUCE_HANDLE.get(), ModItems.BIRCH_HANDLE.get(),
                ModItems.DARK_OAK_HANDLE.get(), ModItems.BAMBOO_HANDLE.get(),
                ModItems.REINFORCED_HANDLE.get(), ModItems.LEATHER_WRAPPED_HANDLE.get(), ModItems.BONE_HANDLE.get());
        tag(key("billets"))
            .add(ModItems.COPPER_BILLET.get(), ModItems.IRON_BILLET.get(),
                ModItems.STEEL_BILLET.get(), ModItems.HARDENED_BILLET.get());
        tag(key("blooms"))
            .add(ModItems.COPPER_BLOOM.get(), ModItems.IRON_BLOOM.get(),
                ModItems.STEEL_BLOOM.get(), ModItems.HARDENED_BLOOM.get());
        tag(key("guards")).add(ModItems.IRON_GUARD.get(), ModItems.STEEL_GUARD.get());
        tag(key("pommels")).add(ModItems.IRON_POMMEL.get(), ModItems.STEEL_POMMEL.get());
        tag(key("components"))
            .add(ModItems.SWORD_BLADE.get(), ModItems.AXE_HEAD.get(),
                ModItems.PICKAXE_HEAD.get(), ModItems.SPEAR_HEAD.get());
        tag(net.minecraft.tags.ItemTags.SWORDS)
            .add(ModItems.COPPER_SWORD.get(), ModItems.IRON_SWORD.get(),
                ModItems.STEEL_SWORD.get(), ModItems.HARDENED_SWORD.get(),
                ModItems.COPPER_SPEAR.get(), ModItems.IRON_SPEAR.get(),
                ModItems.STEEL_SPEAR.get(), ModItems.HARDENED_SPEAR.get());
        tag(net.minecraft.tags.ItemTags.AXES)
            .add(ModItems.COPPER_AXE.get(), ModItems.IRON_AXE.get(),
                ModItems.STEEL_AXE.get(), ModItems.HARDENED_AXE.get());
        tag(net.minecraft.tags.ItemTags.PICKAXES)
            .add(ModItems.COPPER_PICKAXE.get(), ModItems.IRON_PICKAXE.get(),
                ModItems.STEEL_PICKAXE.get(), ModItems.HARDENED_PICKAXE.get());
    }
}

package com.enderblade.registry;

import com.enderblade.EnderBladeMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, EnderBladeMod.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB =
            CREATIVE_MODE_TABS.register("ender_blade_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.enderblade"))
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> ModItems.ENDER_BLADE.get().getDefaultInstance())
                    .displayItems((params, out) -> out.accept(ModItems.ENDER_BLADE.get()))
                    .build());

    private ModCreativeTabs() {
    }
}

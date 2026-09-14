package com.enderblade.registry;

import com.enderblade.EnderBladeMod;
import com.enderblade.component.AnchorLink;
import com.enderblade.component.PhantomLink;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModDataComponents {

    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, EnderBladeMod.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<PhantomLink>> PHANTOM_LINK =
            DATA_COMPONENTS.registerComponentType("phantom_link",
                    b -> b.persistent(PhantomLink.CODEC).networkSynchronized(PhantomLink.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<AnchorLink>> ANCHOR_LINK =
            DATA_COMPONENTS.registerComponentType("anchor_link",
                    b -> b.persistent(AnchorLink.CODEC).networkSynchronized(AnchorLink.STREAM_CODEC));

    private ModDataComponents() {
    }
}

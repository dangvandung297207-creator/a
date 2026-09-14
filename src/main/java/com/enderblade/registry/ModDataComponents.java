package com.enderblade.registry;

import com.enderblade.EnderBladeMod;
import com.enderblade.component.PhantomLink;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Custom data components used by the Ender Blade item.
 * Replaces legacy ItemStack NBT for 1.20.5+ / 1.21.
 */
public final class ModDataComponents {

    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, EnderBladeMod.MOD_ID);

    /**
     * Links an Ender Blade stack to its active phantom projectile (if any).
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<PhantomLink>> PHANTOM_LINK =
            DATA_COMPONENTS.registerComponentType(
                    "phantom_link",
                    builder -> builder
                            .persistent(PhantomLink.CODEC)
                            .networkSynchronized(PhantomLink.STREAM_CODEC)
            );

    private ModDataComponents() {
    }
}

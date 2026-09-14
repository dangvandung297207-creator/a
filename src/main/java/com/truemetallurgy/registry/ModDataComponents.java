package com.truemetallurgy.registry;

import com.truemetallurgy.TrueMetallurgy;
import com.truemetallurgy.components.BilletData;
import com.truemetallurgy.components.FinishedData;
import com.truemetallurgy.components.ForgedComponentData;
import com.truemetallurgy.components.JournalData;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Item data components. All forging identity survives drops, containers,
 * death, chunk unload and server restart because it lives in components.
 */
public final class ModDataComponents {
    private ModDataComponents() {}

    public static final DeferredRegister.DataComponents TYPES =
        DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, TrueMetallurgy.MOD_ID);

    /** Live temperature + purity of a billet (or bloom treated as billet). */
    public static final Supplier<DataComponentType<BilletData>> BILLET = TYPES.registerComponentType(
        "billet", builder -> builder.persistent(BilletData.CODEC).networkSynchronized(BilletData.STREAM_CODEC));

    /** Forging history of a blade/head component. */
    public static final Supplier<DataComponentType<ForgedComponentData>> COMPONENT = TYPES.registerComponentType(
        "forged_component",
        builder -> builder.persistent(ForgedComponentData.CODEC).networkSynchronized(ForgedComponentData.STREAM_CODEC));

    /** Full identity of a finished weapon/tool. */
    public static final Supplier<DataComponentType<FinishedData>> FINISHED = TYPES.registerComponentType(
        "finished", builder -> builder.persistent(FinishedData.CODEC).networkSynchronized(FinishedData.STREAM_CODEC));

    /** Journal discoveries + recorded creations. */
    public static final Supplier<DataComponentType<JournalData>> JOURNAL = TYPES.registerComponentType(
        "journal", builder -> builder.persistent(JournalData.CODEC).networkSynchronized(JournalData.STREAM_CODEC));
}

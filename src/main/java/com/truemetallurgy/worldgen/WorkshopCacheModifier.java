package com.truemetallurgy.worldgen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.truemetallurgy.registry.ModItems;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

/** Adds smithing caches (coke, oil, journals) to village smith chests. */
public class WorkshopCacheModifier extends LootModifier {
    public static final MapCodec<WorkshopCacheModifier> CODEC =
        RecordCodecBuilder.mapCodec(instance -> codecStart(instance).apply(instance, WorkshopCacheModifier::new));

    public WorkshopCacheModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        RandomSource random = context.getRandom();
        if (random.nextFloat() < 0.4F) {
            generatedLoot.add(new ItemStack(ModItems.COKE.get(), 1 + random.nextInt(3)));
        }
        if (random.nextFloat() < 0.25F) {
            generatedLoot.add(new ItemStack(ModItems.QUENCH_OIL.get(), 1 + random.nextInt(2)));
        }
        if (random.nextFloat() < 0.12F) {
            generatedLoot.add(new ItemStack(ModItems.CRUSHED_IRON_ORE.get(), 2 + random.nextInt(4)));
        }
        if (random.nextFloat() < 0.06F) {
            generatedLoot.add(new ItemStack(ModItems.JOURNAL.get(), 1));
        }
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}

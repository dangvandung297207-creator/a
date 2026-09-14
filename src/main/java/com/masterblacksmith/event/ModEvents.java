package com.masterblacksmith.event;

import com.masterblacksmith.ModItems;
import com.masterblacksmith.ModVillagers;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.LootTableLoadEvent;

/** Resident smith trades and treasure injection into vanilla loot. */
public class ModEvents {

    @SubscribeEvent
    public static void onVillagerTrades(VillagerTradesEvent e) {
        if (e.getType() != ModVillagers.MASTER_SMITH.get()) return;
        var trades = e.getTrades();
        trades.get(1).add(new VillagerTrades.EmeraldForItems(Items.COAL, 10, 12, 2));
        trades.get(1).add(new VillagerTrades.ItemsForEmeralds(ModItems.COKE.get(), 1, 6, 12, 1));
        trades.get(1).add(new VillagerTrades.ItemsForEmeralds(ModItems.LEATHER_STRIP.get(), 1, 4, 12, 1));
        trades.get(2).add(new VillagerTrades.ItemsForEmeralds(ModItems.TONGS.get(), 8, 1, 4, 5));
        trades.get(2).add(new VillagerTrades.ItemsForEmeralds(ModItems.OIL_BUCKET.get(), 6, 1, 4, 5));
        trades.get(2).add(new VillagerTrades.ItemsForEmeralds(ModItems.STEEL_INGOT.get(), 4, 2, 8, 5));
        trades.get(3).add(new VillagerTrades.ItemsForEmeralds(ModItems.HAMMER_STEEL.get(), 16, 1, 2, 10));
        trades.get(3).add(new VillagerTrades.ItemsForEmeralds(ModItems.HERBAL_OIL_BUCKET.get(), 18, 1, 2, 10));
        trades.get(3).add(new VillagerTrades.ItemsForEmeralds(ModItems.GRIP_BONE.get(), 6, 1, 4, 10));
        trades.get(4).add(new VillagerTrades.ItemsForEmeralds(ModItems.BLUEPRINT_OATHKEEPER.get(), 40, 1, 1, 20));
        trades.get(4).add(new VillagerTrades.ItemsForEmeralds(ModItems.DAMASCUS_STEEL_INGOT.get(), 12, 1, 2, 20));
        trades.get(5).add(new VillagerTrades.ItemsForEmeralds(ModItems.HAMMER_MASTERWORK.get(), 48, 1, 1, 30));
        trades.get(5).add(new VillagerTrades.ItemsForEmeralds(ModItems.BLUEPRINT_KINGS_EDGE.get(), 64, 1, 1, 30));
    }

    @SubscribeEvent
    public static void onLootLoad(LootTableLoadEvent e) {
        String name = e.getName().toString();
        if (name.equals("minecraft:chests/village/village_weaponsmith")
                || name.equals("minecraft:chests/village/village_toolsmith")) {
            e.getTable().addPool(LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(ModItems.COKE.get()).setWeight(12)
                            .apply(SetItemCountFunction.setCount(ConstantValue.exactly(4))))
                    .add(LootItem.lootTableItem(ModItems.STEEL_INGOT.get()).setWeight(6)
                            .apply(SetItemCountFunction.setCount(ConstantValue.exactly(2))))
                    .add(LootItem.lootTableItem(ModItems.BLUEPRINT_STONESPLITTER.get()).setWeight(1))
                    .build());
        }
        if (name.equals("minecraft:chests/abandoned_mineshaft")) {
            e.getTable().addPool(LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(ModItems.MINERAL_OIL_BUCKET.get()).setWeight(4))
                    .add(LootItem.lootTableItem(ModItems.BLUEPRINT_SKYPIERCER.get()).setWeight(1))
                    .build());
        }
    }
}

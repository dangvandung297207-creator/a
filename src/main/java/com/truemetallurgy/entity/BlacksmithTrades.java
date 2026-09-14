package com.truemetallurgy.entity;

import com.truemetallurgy.registry.ModItems;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

/** The blacksmith's stock: fuels, materials, handles and rare blueprints. */
public final class BlacksmithTrades {
    private BlacksmithTrades() {}

    public static MerchantOffers createOffers(RandomSource random) {
        MerchantOffers offers = new MerchantOffers();
        // Buys coal - every forge is hungry.
        offers.add(new MerchantOffer(new ItemStack(Items.COAL, 12), new ItemStack(Items.EMERALD, 1), 24, 1, 0.05F));
        offers.add(new MerchantOffer(new ItemStack(Items.CHARCOAL, 12), new ItemStack(Items.EMERALD, 1), 24, 1, 0.05F));
        // Sells metallurgy goods.
        offers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 2), new ItemStack(ModItems.COKE.get(), 4), 24, 2, 0.05F));
        offers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 3), new ItemStack(ModItems.CRUSHED_IRON_ORE.get(), 8), 16, 2, 0.05F));
        offers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 3), new ItemStack(ModItems.CRUSHED_COPPER_ORE.get(), 8), 16, 2, 0.05F));
        offers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 4), new ItemStack(ModItems.QUENCH_OIL.get(), 2), 12, 3, 0.08F));
        offers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 6), new ItemStack(ModItems.STEEL_BLOOM.get(), 1), 8, 4, 0.1F));
        offers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 3), new ItemStack(ModItems.OAK_HANDLE.get(), 2), 16, 2, 0.05F));
        offers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 5), new ItemStack(ModItems.LEATHER_WRAPPED_HANDLE.get(), 1), 8, 3, 0.08F));
        offers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 2), new ItemStack(ModItems.JOURNAL.get(), 1), 4, 5, 0.1F));
        // The prize: a legendary design. One per restock, rarely in stock.
        if (random.nextFloat() < 0.6F) {
            offers.add(new MerchantOffer(new ItemStack(Items.EMERALD, 24),
                new ItemStack(Items.DIAMOND, 2), new ItemStack(ModItems.KINGS_EDGE_BLUEPRINT.get(), 1), 1, 10, 0.2F));
        }
        return offers;
    }
}

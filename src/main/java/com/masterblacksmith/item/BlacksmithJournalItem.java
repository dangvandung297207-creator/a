package com.masterblacksmith.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Opens the smith's journal: steels, quenches, grinds and held-piece lore. */
public class BlacksmithJournalItem extends Item {
    public BlacksmithJournalItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            com.masterblacksmith.client.screen.JournalScreen.open();
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}

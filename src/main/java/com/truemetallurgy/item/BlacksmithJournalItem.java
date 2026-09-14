package com.truemetallurgy.item;

import com.truemetallurgy.components.JournalData;
import com.truemetallurgy.registry.ModDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/** The Blacksmith's Journal: materials, processes and recorded creations. */
public class BlacksmithJournalItem extends Item {
    public BlacksmithJournalItem(Properties properties) {
        super(properties);
    }

    public static JournalData dataOf(ItemStack stack) {
        JournalData data = stack.get(ModDataComponents.JOURNAL.get());
        return data != null ? data : JournalData.EMPTY;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (level.isClientSide) {
            com.truemetallurgy.client.ClientHooks.openJournal(held);
            player.swing(hand, true);
            return InteractionResultHolder.consume(held);
        }
        return InteractionResultHolder.pass(held);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, lines, flag);
        JournalData data = dataOf(stack);
        if (data.bestScore() > 0) {
            lines.add(Component.translatable("tooltip.true_metallurgy.journal_best", data.bestName(), data.bestScore())
                .withStyle(s -> s.withColor(0xD8B25C)));
        } else {
            lines.add(Component.translatable("tooltip.true_metallurgy.journal_empty")
                .withStyle(s -> s.withColor(0x9A8A6A).withItalic(true)));
        }
    }
}

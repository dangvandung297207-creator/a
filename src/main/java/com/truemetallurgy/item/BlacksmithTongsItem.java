package com.truemetallurgy.item;

import com.truemetallurgy.forging.HotMetal;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Blacksmith tongs. Required anywhere {@code HOT_METAL} is handled - forge,
 * anvil, quench barrel. Using tongs on hot metal in hand gives feedback and
 * a safe grip instead of a burn.
 */
public class BlacksmithTongsItem extends Item {
    public BlacksmithTongsItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        ItemStack other = player.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
        if (!level.isClientSide && HotMetal.isHotMetal(other)) {
            int temp = HotMetal.temperatureOf(other);
            player.displayClientMessage(Component.translatable("message.true_metallurgy.tongs_grip", temp), true);
            player.swing(hand, true);
            return InteractionResultHolder.consume(held);
        }
        return InteractionResultHolder.pass(held);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, lines, flag);
        lines.add(Component.translatable("tooltip.true_metallurgy.tongs").withStyle(s -> s.withColor(0x9A8A6A).withItalic(true)));
    }
}

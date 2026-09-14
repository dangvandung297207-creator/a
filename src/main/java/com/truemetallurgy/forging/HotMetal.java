package com.truemetallurgy.forging;

import com.truemetallurgy.components.BilletData;
import com.truemetallurgy.components.ForgedComponentData;
import com.truemetallurgy.item.BilletItem;
import com.truemetallurgy.item.BlacksmithTongsItem;
import com.truemetallurgy.item.ForgedComponentItem;
import com.truemetallurgy.metallurgy.Heat;
import com.truemetallurgy.registry.ModDataComponents;
import com.truemetallurgy.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Central helper for {@code HOT_METAL} handling. Hot metal is any billet or
 * forged component at/above warm temperatures; moving it without tongs burns.
 */
public final class HotMetal {
    private HotMetal() {}

    /** Minimum temperature that counts as hot metal. */
    public static final int HOT_MIN = 300;

    public static boolean isWorkpiece(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.getItem() instanceof BilletItem
            || stack.getItem() instanceof ForgedComponentItem
            || isBloom(stack);
    }

    public static boolean isBloom(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.is(ModItems.IRON_BLOOM.get()) || stack.is(ModItems.COPPER_BLOOM.get())
            || stack.is(ModItems.STEEL_BLOOM.get()) || stack.is(ModItems.HARDENED_BLOOM.get());
    }

    /** Blooms reuse the billet component while being heated/handled. */
    public static boolean isHotMetal(ItemStack stack) {
        return temperatureOf(stack) >= HOT_MIN;
    }

    /** Temperature in C, or -1 when the stack cannot hold heat. */
    public static int temperatureOf(ItemStack stack) {
        if (stack.isEmpty()) return -1;
        BilletData billet = stack.get(ModDataComponents.BILLET.get());
        if (billet != null) return billet.temperature();
        ForgedComponentData comp = stack.get(ModDataComponents.COMPONENT.get());
        if (comp != null) return comp.temperature();
        return -1;
    }

    public static boolean playerHasTongs(Player player) {
        return player.getMainHandItem().getItem() instanceof BlacksmithTongsItem
            || player.getOffhandItem().getItem() instanceof BlacksmithTongsItem;
    }

    /**
     * Resolve the workpiece involved in a hand interaction. Either the used
     * stack is the workpiece (tongs must be in the other hand when hot), or
     * the used stack is tongs and the other hand holds the workpiece.
     *
     * @return the workpiece stack, or {@link ItemStack#EMPTY} when invalid
     * (a burn/message is applied on the server in that case).
     */
    public static ItemStack extractWorkpiece(ServerPlayer player, InteractionHand hand, ItemStack used) {
        ItemStack other = player.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
        if (used.getItem() instanceof BlacksmithTongsItem) {
            if (isWorkpiece(other)) return other;
            return ItemStack.EMPTY;
        }
        if (!isWorkpiece(used)) return ItemStack.EMPTY;
        if (isHotMetal(used) && !(other.getItem() instanceof BlacksmithTongsItem)) {
            burn(player, used);
            return ItemStack.EMPTY;
        }
        return used;
    }

    private static void burn(ServerPlayer player, ItemStack stack) {
        if (com.truemetallurgy.config.TMConfig.HOT_METAL_BURNS.get()) {
            player.hurt(player.level().damageSources().hotFloor(), 3.0F);
        }
        player.displayClientMessage(Component.translatable("message.true_metallurgy.need_tongs"), true);
    }

    /** Cool a workpiece stack in place. Returns the new temperature. */
    public static int coolStep(ItemStack stack, int amount) {
        BilletData billet = stack.get(ModDataComponents.BILLET.get());
        if (billet != null) {
            int next = Math.max(Heat.ROOM_TEMP, billet.temperature() - amount);
            stack.set(ModDataComponents.BILLET.get(), billet.withTemp(next));
            return next;
        }
        ForgedComponentData comp = stack.get(ModDataComponents.COMPONENT.get());
        if (comp != null) {
            int next = Math.max(Heat.ROOM_TEMP, comp.temperature() - amount);
            stack.set(ModDataComponents.COMPONENT.get(), ForgingLogic.withTemp(comp, next));
            return next;
        }
        return -1;
    }

    /** Standard inventory cooling: roughly room-temp within ~30s from forging heat. */
    public static void inventoryCool(ItemStack stack) {
        int temp = temperatureOf(stack);
        if (temp > Heat.ROOM_TEMP) {
            coolStep(stack, Math.max(1, (temp - Heat.ROOM_TEMP) / 25));
        }
    }

    /** Spark text feedback shared by workstations. */
    public static void feedback(ServerPlayer player, String key, Object... args) {
        player.displayClientMessage(Component.translatable(key, args), true);
    }
}

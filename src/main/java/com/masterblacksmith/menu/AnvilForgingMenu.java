package com.masterblacksmith.menu;

import com.masterblacksmith.ModBlocks;
import com.masterblacksmith.ModMenus;
import com.masterblacksmith.blockentity.BlacksmithAnvilBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

/** Compact forging panel: temperature, stage, quality + template buttons. */
public class AnvilForgingMenu extends AbstractContainerMenu {
    public static final String[] TEMPLATE_IDS = {"sword", "axe", "pickaxe", "spear", "plate"};

    private final BlacksmithAnvilBlockEntity be;
    private final ContainerData data;
    private final ContainerLevelAccess access;

    public AnvilForgingMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, (BlacksmithAnvilBlockEntity) inv.player.level().getBlockEntity(buf.readBlockPos()),
                new SimpleContainerData(6));
    }

    public AnvilForgingMenu(int id, Inventory inv, BlacksmithAnvilBlockEntity be, ContainerData data) {
        super(ModMenus.ANVIL_FORGING.get(), id);
        this.be = be;
        this.data = data;
        this.access = ContainerLevelAccess.create(be.getLevel(), be.getBlockPos());
        addSlot(new SlotItemHandler(be.getInventory(), 0, 80, 35));
        addDataSlots(data);
        layoutPlayer(inv, 8, 84);
    }

    public int workTemp() { return data.get(0); }
    public int stage() { return data.get(1); }
    public int strikes() { return data.get(2); }
    public int strikesNeeded() { return data.get(3); }
    public int qualityEst() { return data.get(4); }
    public int reheats() { return data.get(5); }
    public BlacksmithAnvilBlockEntity anvil() { return be; }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id >= 0 && id < TEMPLATE_IDS.length && player instanceof ServerPlayer) {
            return be.chooseTemplate(TEMPLATE_IDS[id]);
        }
        return false;
    }

    private void layoutPlayer(Inventory inv, int x, int y) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inv, col + row * 9 + 9, x + col * 18, y + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inv, col, x + col * 18, y + 58));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.ANVIL_BASIC.get())
                || stillValid(access, player, ModBlocks.ANVIL_IRON.get())
                || stillValid(access, player, ModBlocks.ANVIL_STEEL.get())
                || stillValid(access, player, ModBlocks.ANVIL_MASTER.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            if (index == 0) {
                if (!moveItemStackTo(stack, 1, slots.size(), true)) return ItemStack.EMPTY;
            } else if (!moveItemStackTo(stack, 0, 1, false)) {
                return ItemStack.EMPTY;
            }
            if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();
        }
        return result;
    }
}

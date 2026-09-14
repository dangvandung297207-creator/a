package com.masterblacksmith.menu;

import com.masterblacksmith.ModBlocks;
import com.masterblacksmith.ModMenus;
import com.masterblacksmith.blockentity.ForgeHearthBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

/** Fuel + billet slots with live temperature data. */
public class ForgeHearthMenu extends AbstractContainerMenu {
    private final ForgeHearthBlockEntity be;
    private final ContainerData data;
    private final ContainerLevelAccess access;

    public ForgeHearthMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, (ForgeHearthBlockEntity) inv.player.level().getBlockEntity(buf.readBlockPos()),
                new SimpleContainerData(5));
    }

    public ForgeHearthMenu(int id, Inventory inv, ForgeHearthBlockEntity be, ContainerData data) {
        super(ModMenus.FORGE_HEARTH.get(), id);
        this.be = be;
        this.data = data;
        this.access = ContainerLevelAccess.create(be.getLevel(), be.getBlockPos());
        addSlot(new SlotItemHandler(be.getInventory(), 0, 56, 53));
        addSlot(new SlotItemHandler(be.getInventory(), 1, 104, 35));
        addDataSlots(data);
        layoutPlayer(inv, 8, 84);
    }

    public int forgeTemp() { return data.get(0); }
    public int burnTime() { return data.get(1); }
    public int maxBurn() { return data.get(2); }
    public int heatState() { return data.get(3); }
    public int billetTemp() { return data.get(4); }

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
        return stillValid(access, player, ModBlocks.FORGE_HEARTH_PRIMITIVE.get())
                || stillValid(access, player, ModBlocks.FORGE_HEARTH_IRON.get())
                || stillValid(access, player, ModBlocks.FORGE_HEARTH_STEEL.get())
                || stillValid(access, player, ModBlocks.FORGE_HEARTH_MASTER.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            if (index < 2) {
                if (!moveItemStackTo(stack, 2, slots.size(), true)) return ItemStack.EMPTY;
            } else if (!moveItemStackTo(stack, 0, 2, false)) {
                return ItemStack.EMPTY;
            }
            if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();
        }
        return result;
    }
}

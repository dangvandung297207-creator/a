package com.masterblacksmith.menu;

import com.masterblacksmith.ModBlocks;
import com.masterblacksmith.ModMenus;
import com.masterblacksmith.blockentity.GrindingWheelBlockEntity;
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

/** Blade slot + progress/wear/angle data. Angle set via GrindPacket. */
public class GrindingMenu extends AbstractContainerMenu {
    private final GrindingWheelBlockEntity be;
    private final ContainerData data;
    private final ContainerLevelAccess access;

    public GrindingMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, (GrindingWheelBlockEntity) inv.player.level().getBlockEntity(buf.readBlockPos()),
                new SimpleContainerData(3));
    }

    public GrindingMenu(int id, Inventory inv, GrindingWheelBlockEntity be, ContainerData data) {
        super(ModMenus.GRINDING.get(), id);
        this.be = be;
        this.data = data;
        this.access = ContainerLevelAccess.create(be.getLevel(), be.getBlockPos());
        addSlot(new SlotItemHandler(be.getInventory(), 0, 80, 35));
        addDataSlots(data);
        layoutPlayer(inv, 8, 84);
    }

    public int progress() { return data.get(0); }
    public int wear() { return data.get(1); }
    public float angle() { return data.get(2) / 10F; }
    public GrindingWheelBlockEntity wheel() { return be; }

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
        return stillValid(access, player, ModBlocks.GRINDING_WHEEL.get());
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

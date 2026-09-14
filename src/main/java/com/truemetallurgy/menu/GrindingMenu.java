package com.truemetallurgy.menu;

import com.truemetallurgy.blockentity.GrindingWheelBlockEntity;
import com.truemetallurgy.block.GrindingWheelBlock;
import com.truemetallurgy.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.SlotItemHandler;

/** Grinding menu. Data: wear, progress, max, angle, can-grind, has-input. */
public class GrindingMenu extends AbstractContainerMenu {
    public static final int D_WEAR = 0;
    public static final int D_PROGRESS = 1;
    public static final int D_MAX = 2;
    public static final int D_ANGLE = 3;
    public static final int D_CAN = 4;
    public static final int D_HAS = 5;
    public static final int DATA_COUNT = 6;

    private final ContainerLevelAccess access;
    private final ContainerData data;
    private final BlockPos pos;

    public GrindingMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, buf.readBlockPos(), new SimpleContainerData(DATA_COUNT));
    }

    public GrindingMenu(int id, Inventory inv, BlockPos pos, ContainerData data) {
        super(ModMenus.GRINDING.get(), id);
        this.pos = pos;
        this.access = ContainerLevelAccess.create(inv.player.level(), pos);
        this.data = data;
        if (inv.player.level().getBlockEntity(pos) instanceof GrindingWheelBlockEntity be) {
            addSlot(new SlotItemHandler(be.getItemHandler(), 0, 80, 35));
        }
        addDataSlots(data);
        layoutPlayerSlots(inv);
    }

    protected void layoutPlayerSlots(Inventory inv) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 104 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inv, col, 8 + col * 18, 162));
        }
    }

    public BlockPos pos() { return pos; }
    public int wear() { return data.get(D_WEAR); }
    public int progress() { return data.get(D_PROGRESS); }
    public int max() { return Math.max(1, data.get(D_MAX)); }
    public int angle() { return data.get(D_ANGLE); }
    public boolean canGrind() { return data.get(D_CAN) == 1; }
    public boolean hasInput() { return data.get(D_HAS) == 1; }

    @Override
    public boolean stillValid(Player player) {
        return access.evaluate((level, p) ->
            level.getBlockState(p).getBlock() instanceof GrindingWheelBlock
                && player.distanceToSqr(Vec3.atCenterOf(p)) <= 64.0, true);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack copy = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            copy = stack.copy();
            if (index < 1) {
                if (!moveItemStackTo(stack, 1, slots.size(), true)) return ItemStack.EMPTY;
            } else if (!moveItemStackTo(stack, 0, 1, false)) {
                return ItemStack.EMPTY;
            }
            if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
            else slot.setChanged();
            if (stack.getCount() == copy.getCount()) return ItemStack.EMPTY;
            slot.onTake(player, stack);
        }
        return copy;
    }
}

package com.truemetallurgy.menu;

import com.truemetallurgy.blockentity.BlacksmithAnvilBlockEntity;
import com.truemetallurgy.block.BlacksmithAnvilBlock;
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

/** Compact forging interface: temp, heat zone, stage, score, target zone. */
public class ForgingMenu extends AbstractContainerMenu {
    public static final int D_TEMP = 0;
    public static final int D_HEAT_ACC = 1;
    public static final int D_STAGE = 2;
    public static final int D_STAGE_PROG = 3;
    public static final int D_STAGE_REQ = 4;
    public static final int D_SCORE = 5;
    public static final int D_STRIKES = 6;
    public static final int D_TARGET_X = 7;
    public static final int D_TARGET_Z = 8;
    public static final int D_HAS_WORK = 9;
    public static final int D_KIND = 10;
    public static final int D_FLAGS = 11;
    public static final int DATA_COUNT = 12;

    private final ContainerLevelAccess access;
    private final ContainerData data;
    private final BlockPos pos;

    public ForgingMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, buf.readBlockPos(), new SimpleContainerData(DATA_COUNT));
    }

    public ForgingMenu(int id, Inventory inv, BlockPos pos, ContainerData data) {
        super(ModMenus.FORGING.get(), id);
        this.pos = pos;
        this.access = ContainerLevelAccess.create(inv.player.level(), pos);
        this.data = data;
        if (inv.player.level().getBlockEntity(pos) instanceof BlacksmithAnvilBlockEntity be) {
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
    public int temperature() { return data.get(D_TEMP); }
    public int heatAccuracy() { return data.get(D_HEAT_ACC); }
    public int stage() { return data.get(D_STAGE); }
    public int stageProgress() { return data.get(D_STAGE_PROG); }
    public int stageRequired() { return data.get(D_STAGE_REQ); }
    public int score() { return data.get(D_SCORE); }
    public int strikes() { return data.get(D_STRIKES); }
    public float targetX() { return data.get(D_TARGET_X) / 100.0F; }
    public float targetZ() { return data.get(D_TARGET_Z) / 100.0F; }
    public boolean hasWorkpiece() { return data.get(D_HAS_WORK) == 1; }
    public int kind() { return data.get(D_KIND); }
    public int flags() { return data.get(D_FLAGS); }

    @Override
    public boolean stillValid(Player player) {
        return access.evaluate((level, p) ->
            level.getBlockState(p).getBlock() instanceof BlacksmithAnvilBlock
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

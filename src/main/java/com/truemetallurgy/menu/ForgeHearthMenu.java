package com.truemetallurgy.menu;

import com.truemetallurgy.blockentity.ForgeHearthBlockEntity;
import com.truemetallurgy.block.ForgeHearthBlock;
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

/** Forge menu. Data: temp, fuel, maxFuel, airflow, alloyProgress, alloyTime, heat, maxTemp. */
public class ForgeHearthMenu extends AbstractContainerMenu {
    public static final int DATA_COUNT = 8;

    private final ContainerLevelAccess access;
    private final ContainerData data;
    private final BlockPos pos;

    public ForgeHearthMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, buf.readBlockPos(), new SimpleContainerData(DATA_COUNT));
    }

    public ForgeHearthMenu(int id, Inventory inv, BlockPos pos, ContainerData data) {
        super(ModMenus.FORGE.get(), id);
        this.pos = pos;
        this.access = ContainerLevelAccess.create(inv.player.level(), pos);
        this.data = data;
        if (inv.player.level().getBlockEntity(pos) instanceof ForgeHearthBlockEntity be) {
            addSlot(new SlotItemHandler(be.getItemHandler(), ForgeHearthBlockEntity.SLOT_FUEL, 44, 53));
            addSlot(new SlotItemHandler(be.getItemHandler(), ForgeHearthBlockEntity.SLOT_WORK, 80, 53));
            addSlot(new SlotItemHandler(be.getItemHandler(), ForgeHearthBlockEntity.SLOT_ALLOY_A, 116, 35));
            addSlot(new SlotItemHandler(be.getItemHandler(), ForgeHearthBlockEntity.SLOT_ALLOY_B, 116, 57));
            addSlot(new SlotItemHandler(be.getItemHandler(), ForgeHearthBlockEntity.SLOT_OUT, 143, 46));
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
    public int temperature() { return data.get(0); }
    public int fuel() { return data.get(1); }
    public int maxFuel() { return Math.max(1, data.get(2)); }
    public int airflow() { return data.get(3); }
    public int alloyProgress() { return data.get(4); }
    public int alloyTime() { return Math.max(1, data.get(5)); }
    public int heatState() { return data.get(6); }
    public int maxTemp() { return data.get(7); }

    @Override
    public boolean stillValid(Player player) {
        return access.evaluate((level, p) ->
            level.getBlockState(p).getBlock() instanceof ForgeHearthBlock
                && player.distanceToSqr(Vec3.atCenterOf(p)) <= 64.0, true);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack copy = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            copy = stack.copy();
            if (index < 5) {
                if (!moveItemStackTo(stack, 5, slots.size(), true)) return ItemStack.EMPTY;
            } else if (!moveItemStackTo(stack, 0, 5, false)) {
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

package com.masterblacksmith.menu;

import com.masterblacksmith.ModBlocks;
import com.masterblacksmith.ModMenus;
import com.masterblacksmith.ModSounds;
import com.masterblacksmith.blockentity.AssemblyTableBlockEntity;
import com.masterblacksmith.forging.AssemblyLogic;
import com.masterblacksmith.forging.QualityTier;
import com.masterblacksmith.util.SoundUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

import java.util.ArrayList;
import java.util.List;

/** Four parts + blueprint; the result is computed live from the inputs. */
public class AssemblyMenu extends AbstractContainerMenu {
    private final AssemblyTableBlockEntity be;
    private final ContainerData data;
    private final ContainerLevelAccess access;
    private final Player player;
    private final SimpleContainer resultInv = new SimpleContainer(1);
    private AssemblyLogic.Plan plan = AssemblyLogic.Plan.invalid(net.minecraft.network.chat.Component.empty());

    public AssemblyMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, (AssemblyTableBlockEntity) inv.player.level().getBlockEntity(buf.readBlockPos()),
                new SimpleContainerData(2));
    }

    public AssemblyMenu(int id, Inventory inv, AssemblyTableBlockEntity be, ContainerData data) {
        super(ModMenus.ASSEMBLY.get(), id);
        this.be = be;
        this.data = data;
        this.player = inv.player;
        this.access = ContainerLevelAccess.create(be.getLevel(), be.getBlockPos());
        addSlot(new SlotItemHandler(be.getInventory(), 0, 30, 17));
        addSlot(new SlotItemHandler(be.getInventory(), 1, 48, 17));
        addSlot(new SlotItemHandler(be.getInventory(), 2, 30, 35));
        addSlot(new SlotItemHandler(be.getInventory(), 3, 48, 35));
        addSlot(new SlotItemHandler(be.getInventory(), 4, 30, 57));
        addSlot(new Slot(resultInv, 0, 124, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) { return false; }

            @Override
            public boolean mayPickup(Player p) { return plan.valid() && !resultInv.getItem(0).isEmpty(); }

            @Override
            public void onTake(Player p, ItemStack stack) {
                takeResult(p);
                super.onTake(p, stack);
            }
        });
        addDataSlots(data);
        layoutPlayer(inv, 8, 84);
        refreshPlan();
    }

    public int mode() { return data.get(0); }
    public int predictedQuality() { return data.get(1); }
    public AssemblyLogic.Plan plan() { return plan; }
    public AssemblyTableBlockEntity table() { return be; }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id >= 10 && id <= 14 && player instanceof ServerPlayer) {
            be.setMode(id - 10);
            refreshPlan();
            return true;
        }
        return false;
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        refreshPlan();
    }

    private void refreshPlan() {
        List<ItemStack> parts = new ArrayList<>();
        for (int i = 0; i < 4; i++) parts.add(be.getInventory().getStackInSlot(i));
        plan = AssemblyLogic.plan(parts, be.getInventory().getStackInSlot(4), be.getMode(), player);
        resultInv.setItem(0, plan.result().copy());
        be.setPredictedQuality(plan.quality());
    }

    private void takeResult(Player p) {
        if (p.level().isClientSide || !plan.valid()) return;
        for (int i = 0; i < 4; i++) {
            var inv = be.getInventory();
            ItemStack s = inv.getStackInSlot(i);
            if (!s.isEmpty()) {
                // Leather strips stack: consume what the plan needed.
                if (s.getItem().toString().contains("leather_strip") && s.getCount() > 2) {
                    s.shrink(2);
                } else if (s.getItem().toString().contains("leather_strip") && s.getCount() > 1
                        && plan.result().getItem().toString().contains("spear")) {
                    s.shrink(1);
                } else {
                    inv.setStackInSlot(i, ItemStack.EMPTY);
                }
            }
        }
        var level = p.level();
        var pos = be.getBlockPos();
        SoundUtil.play(level, pos, ModSounds.ASSEMBLY_WOOD.get(), SoundSource.BLOCKS, 0.9F, 1.0F, 0.08F);
        SoundUtil.play(level, pos, ModSounds.ASSEMBLY_RIVET.get(), SoundSource.BLOCKS, 0.9F, 1.0F, 0.08F);
        if (plan.quality() >= 90) {
            SoundUtil.play(level, pos, ModSounds.LEGENDARY_COMPLETE.get(), SoundSource.PLAYERS, 1.0F, 1.0F, 0.03F);
        } else if (plan.quality() >= 70) {
            SoundUtil.play(level, pos, ModSounds.MASTERWORK_COMPLETE.get(), SoundSource.PLAYERS, 1.0F, 1.0F, 0.03F);
        }
        broadcastChanges();
        refreshPlan();
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
        return stillValid(access, player, ModBlocks.ASSEMBLY_TABLE.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            if (index == 5) {
                if (!moveItemStackTo(stack, 6, slots.size(), true)) return ItemStack.EMPTY;
                slot.onQuickCraft(stack, result);
            } else if (index < 5) {
                if (!moveItemStackTo(stack, 6, slots.size(), true)) return ItemStack.EMPTY;
            } else if (!moveItemStackTo(stack, 0, 5, false)) {
                return ItemStack.EMPTY;
            }
            if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();
            if (index == 5 && player instanceof ServerPlayer) {
                takeResult(player);
            }
        }
        return result;
    }
}

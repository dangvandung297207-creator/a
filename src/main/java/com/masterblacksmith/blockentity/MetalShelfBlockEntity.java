package com.masterblacksmith.blockentity;

import com.masterblacksmith.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;

/** Nine visible stock slots. Rendered by MetalShelfRenderer. */
public class MetalShelfBlockEntity extends BlockEntity {
    private final ItemStackHandler inventory = new ItemStackHandler(9) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            sync();
        }
    };

    public MetalShelfBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.METAL_SHELF.get(), pos, state);
    }

    public ItemStackHandler getInventory() { return inventory; }

    public boolean addStock(ItemStack stack) {
        for (int i = 0; i < 9; i++) {
            ItemStack s = inventory.getStackInSlot(i);
            if (!s.isEmpty() && ItemStack.isSameItemSameTags(s, stack) && s.getCount() + stack.getCount() <= 64) {
                s.grow(stack.getCount());
                return true;
            }
        }
        for (int i = 0; i < 9; i++) {
            if (inventory.getStackInSlot(i).isEmpty()) {
                inventory.setStackInSlot(i, stack);
                return true;
            }
        }
        return false;
    }

    public ItemStack removeLast() {
        for (int i = 8; i >= 0; i--) {
            ItemStack s = inventory.getStackInSlot(i);
            if (!s.isEmpty()) {
                inventory.setStackInSlot(i, ItemStack.EMPTY);
                return s;
            }
        }
        return ItemStack.EMPTY;
    }

    public SimpleContainer inventoryView() {
        SimpleContainer c = new SimpleContainer(9);
        for (int i = 0; i < 9; i++) c.setItem(i, inventory.getStackInSlot(i).copy());
        return c;
    }

    public void sync() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", inventory.serializeNBT());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        inventory.deserializeNBT(tag.getCompound("Inventory"));
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        load(pkt.getTag());
    }
}

package com.masterblacksmith.blockentity;

import com.masterblacksmith.ModBlockEntities;
import com.masterblacksmith.ModParticles;
import com.masterblacksmith.ModSounds;
import com.masterblacksmith.block.GrindingWheelBlock;
import com.masterblacksmith.forging.ForgingData;
import com.masterblacksmith.forging.GrindingProfile;
import com.masterblacksmith.menu.GrindingMenu;
import com.masterblacksmith.util.ParticleUtil;
import com.masterblacksmith.util.SoundUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Edge grinding: angle choice, wheel wear and spark showers. */
public class GrindingWheelBlockEntity extends BlockEntity implements net.minecraft.world.MenuProvider {
    private final ItemStackHandler inventory = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            sync();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return ForgingData.isAssemblyReady(stack);
        }
    };
    private LazyOptional<ItemStackHandler> handler = LazyOptional.empty();

    private int progress;
    private int wear;
    private float angle = 25F;

    private final ContainerData data = new ContainerData() {
        @Override public int get(int i) {
            return switch (i) {
                case 0 -> progress;
                case 1 -> wear;
                case 2 -> Math.round(angle * 10F);
                default -> 0;
            };
        }
        @Override public void set(int i, int v) {}
        @Override public int getCount() { return 3; }
    };

    public GrindingWheelBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GRINDING_WHEEL.get(), pos, state);
    }

    public ItemStackHandler getInventory() { return inventory; }

    public boolean hasBlade() { return !inventory.getStackInSlot(0).isEmpty(); }
    public ItemStack getBlade() { return inventory.getStackInSlot(0); }
    public void setBlade(ItemStack stack) {
        inventory.setStackInSlot(0, stack);
        progress = 0;
    }
    public ItemStack takeBlade() {
        ItemStack out = inventory.getStackInSlot(0).copy();
        inventory.setStackInSlot(0, ItemStack.EMPTY);
        progress = 0;
        return out;
    }

    public float getAngle() { return angle; }
    public void setAngle(float angle) {
        this.angle = Math.max(GrindingProfile.MIN_ANGLE, Math.min(GrindingProfile.MAX_ANGLE, angle));
        setChanged();
    }

    public boolean repair(int amount) {
        if (wear <= 0) return false;
        wear = Math.max(0, wear - amount);
        setChanged();
        sync();
        return true;
    }

    public SimpleContainer inventoryView() {
        SimpleContainer c = new SimpleContainer(1);
        c.setItem(0, inventory.getStackInSlot(0).copy());
        return c;
    }

    /** One grinding pass at the chosen angle. Three passes finish an edge. */
    public void serverGrind(ServerPlayer player, float grindAngle) {
        if (level == null || level.isClientSide || !hasBlade()) return;
        if (player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) > 36) return;
        setAngle(grindAngle);
        if (wear >= 100) {
            player.displayClientMessage(Component.translatable("message.masterblacksmith.wheel_worn"), true);
            return;
        }
        ItemStack blade = getBlade();
        progress += 34;
        wear = Math.min(100, wear + 6);

        BlockState state = getBlockState();
        if (!state.getValue(GrindingWheelBlock.SPINNING)) {
            level.setBlock(worldPosition, state.setValue(GrindingWheelBlock.SPINNING, true), 3);
            level.scheduleTick(worldPosition, state.getBlock(), 14);
        }
        if (level instanceof ServerLevel sl) {
            ParticleUtil.burst(sl, ModParticles.GRIND_DUST.get(),
                    worldPosition.getX() + 0.5, worldPosition.getY() + 0.9, worldPosition.getZ() + 0.5,
                    10, 0.3, 0.3);
            ParticleUtil.burst(sl, ModParticles.FORGE_SPARK.get(),
                    worldPosition.getX() + 0.5, worldPosition.getY() + 0.9, worldPosition.getZ() + 0.5,
                    6, 0.25, 0.3);
        }
        SoundUtil.play(level, worldPosition, ModSounds.GRIND_WHEEL.get(),
                net.minecraft.sounds.SoundSource.BLOCKS, 0.9F, 1.0F, 0.08F);

        if (progress >= 100) {
            progress = 0;
            String template = ForgingData.getTemplateId(blade);
            float quality = GrindingProfile.grindQuality(angle, template, wear / 100F);
            ForgingData.setGrind(blade, angle, quality);
            ForgingData.addHistory(blade, "ground." + Math.round(angle));
            SoundUtil.play(level, worldPosition, ModSounds.GRIND_SPARK.get(),
                    net.minecraft.sounds.SoundSource.BLOCKS, 0.8F, 1.1F, 0.08F);
            player.displayClientMessage(Component.translatable("message.masterblacksmith.ground",
                    String.format("%.0f", angle), Math.round(quality)), true);
        }
        setChanged();
        sync();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, GrindingWheelBlockEntity be) {
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, GrindingWheelBlockEntity be) {
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
        tag.putInt("Progress", progress);
        tag.putInt("Wear", wear);
        tag.putFloat("Angle", angle);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        inventory.deserializeNBT(tag.getCompound("Inventory"));
        progress = tag.getInt("Progress");
        wear = tag.getInt("Wear");
        angle = tag.contains("Angle") ? tag.getFloat("Angle") : 25F;
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

    @Override
    public Component getDisplayName() {
        return Component.translatable("menu.masterblacksmith.grinding");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new GrindingMenu(id, inv, this, data);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        handler = LazyOptional.of(() -> inventory);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) return handler.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        handler.invalidate();
    }
}

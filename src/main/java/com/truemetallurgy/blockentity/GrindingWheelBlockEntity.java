package com.truemetallurgy.blockentity;

import com.truemetallurgy.block.GrindingWheelBlock;
import com.truemetallurgy.forging.HotMetal;
import com.truemetallurgy.grinding.GrindingLogic;
import com.truemetallurgy.item.ForgedComponentItem;
import com.truemetallurgy.menu.GrindingMenu;
import com.truemetallurgy.metallurgy.GrindAngle;
import com.truemetallurgy.metallurgy.Heat;
import com.truemetallurgy.registry.ModBlockEntities;
import com.truemetallurgy.registry.ModParticles;
import com.truemetallurgy.registry.ModSounds;
import com.truemetallurgy.util.ParticleBudget;
import com.truemetallurgy.util.SoundHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

/** Grinding wheel: angled edge grinding with wheel wear. */
public class GrindingWheelBlockEntity extends BlockEntity implements MenuProvider {
    public static final int GRIND_TIME = 60;

    private final ItemStackHandler items = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            sync();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.getItem() instanceof ForgedComponentItem && getStackInSlot(slot).isEmpty();
        }
    };

    private int wear;
    private int grindingTicks;
    private int angleIndex = 1;

    private final ContainerData data = new ContainerData() {
        @Override public int get(int i) {
            return switch (i) {
                case GrindingMenu.D_WEAR -> wear;
                case GrindingMenu.D_PROGRESS -> grindingTicks;
                case GrindingMenu.D_MAX -> GRIND_TIME;
                case GrindingMenu.D_ANGLE -> angleIndex;
                case GrindingMenu.D_CAN -> canGrindNow() ? 1 : 0;
                case GrindingMenu.D_HAS -> items.getStackInSlot(0).isEmpty() ? 0 : 1;
                default -> 0;
            };
        }
        @Override public void set(int i, int v) {}
        @Override public int getCount() { return GrindingMenu.DATA_COUNT; }
    };

    public GrindingWheelBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GRINDING_WHEEL.get(), pos, state);
    }

    public ItemStackHandler getItemHandler() {
        return items;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, GrindingWheelBlockEntity be) {
        if (level.isClientSide) return;
        ServerLevel server = (ServerLevel) level;
        boolean changed = false;
        if (be.grindingTicks > 0) {
            be.grindingTicks--;
            if (server.getGameTime() % 5 == 0) {
                int spin = (state.getValue(GrindingWheelBlock.SPIN) + 1) % 4;
                if (spin == 0) spin = 1;
                server.setBlock(pos, state.setValue(GrindingWheelBlock.SPIN, spin), 3);
            }
            if (server.getGameTime() % 10 == 0) {
                ParticleBudget.puff(server, ModParticles.SPARK.get(),
                    pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 3, 0.3, 0.25, 0.3, 0.08);
                ParticleBudget.puff(server, ModParticles.METAL_DUST.get(),
                    pos.getX() + 0.5, pos.getY() + 0.9, pos.getZ() + 0.5, 2, 0.2, 0.15, 0.2, 0.03);
            }
            if (server.getGameTime() % 20 == 0) {
                SoundHelper.play(server, pos, ModSounds.GRIND_SCRAPE, SoundEvents.GRINDSTONE_USE,
                    SoundSource.BLOCKS, 0.7F, 1.0F);
            }
            if (be.grindingTicks == 0) {
                be.finishGrinding(server);
                server.setBlock(pos, server.getBlockState(pos).setValue(GrindingWheelBlock.SPIN, 0), 3);
            }
            changed = true;
        }
        ItemStack input = be.items.getStackInSlot(0);
        if (!input.isEmpty() && server.getGameTime() % 20 == 0 && HotMetal.temperatureOf(input) > Heat.ROOM_TEMP) {
            HotMetal.inventoryCool(input);
            changed = true;
        }
        if (changed) be.setChanged();
    }

    private void finishGrinding(ServerLevel server) {
        ItemStack input = items.getStackInSlot(0);
        if (GrindingLogic.failureReason(input) != null) return;
        GrindAngle angle = GrindAngle.byIndex(angleIndex);
        GrindingLogic.apply(input, angle, wear);
        wear = Math.min(100, wear + 8);
        SoundHelper.play(server, worldPosition, ModSounds.GRIND_LOOP, SoundEvents.GRINDSTONE_USE,
            SoundSource.BLOCKS, 0.8F, 1.2F);
        setChanged();
        sync();
    }

    /** Redress a worn wheel with cobblestone. */
    public boolean redress(Player player, InteractionHand hand, ItemStack stack) {
        if (!(player instanceof ServerPlayer sp)) return false;
        if ((stack.is(Items.COBBLESTONE) || stack.is(Items.COBBLED_DEEPSLATE)) && wear > 0) {
            wear = 0;
            if (!sp.getAbilities().instabuild) stack.shrink(1);
            SoundHelper.play(sp.level(), worldPosition, ModSounds.GRIND_SCRAPE, SoundEvents.STONE_BREAK,
                SoundSource.BLOCKS, 0.8F, 1.0F);
            HotMetal.feedback(sp, "message.true_metallurgy.wheel_dressed");
            setChanged();
            sync();
            return true;
        }
        return false;
    }

    private boolean canGrindNow() {
        return grindingTicks <= 0 && GrindingLogic.failureReason(items.getStackInSlot(0)) == null;
    }

    /** GUI angle selection. */
    public boolean selectAngle(ServerPlayer player, int index) {
        if (grindingTicks > 0) return false;
        angleIndex = Math.max(0, Math.min(2, index));
        setChanged();
        sync();
        return true;
    }

    /** GUI grind button. */
    public boolean startGrinding(ServerPlayer player) {
        if (grindingTicks > 0) return false;
        String failure = GrindingLogic.failureReason(items.getStackInSlot(0));
        if (failure != null) {
            HotMetal.feedback(player, failure);
            return false;
        }
        grindingTicks = GRIND_TIME;
        SoundHelper.play(player.level(), worldPosition, ModSounds.GRIND_LOOP, SoundEvents.GRINDSTONE_USE,
            SoundSource.BLOCKS, 0.7F, 0.9F);
        setChanged();
        sync();
        return true;
    }

    public void dropContents(Level level, BlockPos pos) {
        ItemStack stack = items.getStackInSlot(0);
        if (!stack.isEmpty()) {
            net.minecraft.world.Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("menu.true_metallurgy.grinding");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new GrindingMenu(id, inventory, worldPosition, data);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Items", items.serializeNBT(registries));
        tag.putInt("Wear", wear);
        tag.putInt("Grinding", grindingTicks);
        tag.putInt("Angle", angleIndex);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Items", Tag.TAG_COMPOUND)) {
            items.deserializeNBT(registries, tag.getCompound("Items"));
        }
        wear = tag.getInt("Wear");
        grindingTicks = tag.getInt("Grinding");
        angleIndex = tag.contains("Angle") ? tag.getInt("Angle") : 1;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private void sync() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }
}

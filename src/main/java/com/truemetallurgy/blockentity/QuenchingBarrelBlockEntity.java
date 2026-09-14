package com.truemetallurgy.blockentity;

import com.truemetallurgy.block.QuenchingBarrelBlock;
import com.truemetallurgy.forging.HotMetal;
import com.truemetallurgy.item.BlacksmithTongsItem;
import com.truemetallurgy.item.ForgedComponentItem;
import com.truemetallurgy.menu.QuenchingMenu;
import com.truemetallurgy.metallurgy.Heat;
import com.truemetallurgy.metallurgy.QuenchMedium;
import com.truemetallurgy.quenching.QuenchingLogic;
import com.truemetallurgy.registry.ModBlockEntities;
import com.truemetallurgy.registry.ModItems;
import com.truemetallurgy.registry.ModParticles;
import com.truemetallurgy.registry.ModSounds;
import com.truemetallurgy.util.ParticleBudget;
import com.truemetallurgy.util.SoundHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
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

/** Quenching barrel: internal liquid units, server-side quench resolution. */
public class QuenchingBarrelBlockEntity extends BlockEntity implements MenuProvider {
    public static final int CAPACITY = 4;

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

    private String liquid = "none";
    private int units;

    private final ContainerData data = new ContainerData() {
        @Override public int get(int i) {
            return switch (i) {
                case QuenchingMenu.D_LIQUID -> liquidOrdinal();
                case QuenchingMenu.D_UNITS -> units;
                case QuenchingMenu.D_TEMP -> HotMetal.temperatureOf(items.getStackInSlot(0));
                case QuenchingMenu.D_CAN -> canQuenchNow() ? 1 : 0;
                default -> 0;
            };
        }
        @Override public void set(int i, int v) {}
        @Override public int getCount() { return QuenchingMenu.DATA_COUNT; }
    };

    public QuenchingBarrelBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.QUENCH_BARREL.get(), pos, state);
    }

    public ItemStackHandler getItemHandler() {
        return items;
    }

    public String liquid() { return liquid; }
    public int units() { return units; }

    private int liquidOrdinal() {
        return switch (liquid) {
            case "water" -> 1;
            case "oil" -> 2;
            default -> 0;
        };
    }

    public static void tick(Level level, BlockPos pos, BlockState state, QuenchingBarrelBlockEntity be) {
        if (level.isClientSide) return;
        ServerLevel server = (ServerLevel) level;
        ItemStack input = be.items.getStackInSlot(0);
        if (!input.isEmpty() && server.getGameTime() % 40 == 0) {
            int temp = HotMetal.temperatureOf(input);
            if (temp > 200) {
                HotMetal.coolStep(input, 10);
                if (be.units > 0) {
                    ParticleBudget.puff(server, be.liquid.equals("oil") ? ParticleTypes.SMOKE : ModParticles.STEAM.get(),
                        pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 2, 0.25, 0.2, 0.25, 0.02);
                }
                be.setChanged();
            }
        }
    }

    // ---------------- interaction ----------------

    public boolean interact(Player player, InteractionHand hand, ItemStack stack) {
        if (!(player instanceof ServerPlayer sp)) return false;
        // Fill with water.
        if (stack.is(Items.WATER_BUCKET)) {
            if (!liquid.equals("none") && !liquid.equals("water")) {
                HotMetal.feedback(sp, "message.true_metallurgy.barrel_mixed");
                return true;
            }
            if (units >= CAPACITY) {
                HotMetal.feedback(sp, "message.true_metallurgy.barrel_full");
                return true;
            }
            liquid = "water";
            units = CAPACITY;
            if (!sp.getAbilities().instabuild) {
                stack.shrink(1);
                ItemStack empty = new ItemStack(Items.BUCKET);
                if (!sp.getInventory().add(empty)) sp.drop(empty, false);
            }
            SoundHelper.play(sp.level(), worldPosition, ModSounds.QUENCH_WATER, SoundEvents.BUCKET_FILL,
                SoundSource.BLOCKS, 0.7F, 1.0F);
            updateLiquidState();
            setChanged();
            sync();
            return true;
        }
        // Fill with quench oil.
        if (stack.is(ModItems.QUENCH_OIL.get())) {
            if (!liquid.equals("none") && !liquid.equals("oil")) {
                HotMetal.feedback(sp, "message.true_metallurgy.barrel_mixed");
                return true;
            }
            if (units >= CAPACITY) {
                HotMetal.feedback(sp, "message.true_metallurgy.barrel_full");
                return true;
            }
            liquid = "oil";
            units = Math.min(CAPACITY, units + 2);
            if (!sp.getAbilities().instabuild) stack.shrink(1);
            SoundHelper.play(sp.level(), worldPosition, ModSounds.QUENCH_OIL, SoundEvents.BUCKET_FILL,
                SoundSource.BLOCKS, 0.7F, 0.8F);
            updateLiquidState();
            setChanged();
            sync();
            return true;
        }
        // Drain with an empty bucket.
        if (stack.is(Items.BUCKET) && !liquid.equals("none")) {
            liquid = "none";
            units = 0;
            SoundHelper.play(sp.level(), worldPosition, ModSounds.QUENCH_WATER, SoundEvents.BUCKET_EMPTY,
                SoundSource.BLOCKS, 0.7F, 1.0F);
            updateLiquidState();
            setChanged();
            sync();
            return true;
        }
        // Place / take the component with tongs.
        ItemStack work = HotMetal.extractWorkpiece(sp, hand, stack);
        if (!work.isEmpty()) {
            if (!(work.getItem() instanceof ForgedComponentItem)) {
                HotMetal.feedback(sp, "message.true_metallurgy.quench_not_component");
                return true;
            }
            if (!items.getStackInSlot(0).isEmpty()) {
                HotMetal.feedback(sp, "message.true_metallurgy.barrel_occupied");
                return true;
            }
            items.setStackInSlot(0, work.copyWithCount(1));
            work.shrink(1);
            setChanged();
            sync();
            return true;
        }
        if (stack.getItem() instanceof BlacksmithTongsItem) {
            ItemStack other = sp.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
            if (other.isEmpty() && !items.getStackInSlot(0).isEmpty()) {
                ItemStack out = items.getStackInSlot(0);
                items.setStackInSlot(0, ItemStack.EMPTY);
                if (!sp.getInventory().add(out)) sp.drop(out, false);
                setChanged();
                sync();
                return true;
            }
        }
        return false;
    }

    private void updateLiquidState() {
        if (level == null) return;
        QuenchingBarrelBlock.Liquid state = switch (liquid) {
            case "water" -> QuenchingBarrelBlock.Liquid.WATER;
            case "oil" -> QuenchingBarrelBlock.Liquid.OIL;
            default -> QuenchingBarrelBlock.Liquid.NONE;
        };
        if (getBlockState().getValue(QuenchingBarrelBlock.LIQUID) != state) {
            level.setBlock(worldPosition, getBlockState().setValue(QuenchingBarrelBlock.LIQUID, state), 3);
        }
    }

    private boolean canQuenchNow() {
        return units > 0 && !liquid.equals("none") && QuenchingLogic.failureReason(items.getStackInSlot(0)) == null;
    }

    /** GUI quench button. Fully server-validated. */
    public boolean quench(ServerPlayer player) {
        if (units <= 0 || liquid.equals("none")) {
            HotMetal.feedback(player, "message.true_metallurgy.barrel_empty");
            return false;
        }
        ItemStack input = items.getStackInSlot(0);
        String failure = QuenchingLogic.failureReason(input);
        if (failure != null) {
            HotMetal.feedback(player, failure);
            return false;
        }
        QuenchMedium medium = QuenchMedium.get(liquid);
        float score = QuenchingLogic.apply(input, medium);
        units--;
        if (units <= 0) {
            liquid = "none";
            updateLiquidState();
        }
        quenchEffects((ServerLevel) player.level(), medium);
        HotMetal.feedback(player, "message.true_metallurgy.quenched", Math.round(score));
        setChanged();
        sync();
        return true;
    }

    private void quenchEffects(ServerLevel server, QuenchMedium medium) {
        double x = worldPosition.getX() + 0.5;
        double y = worldPosition.getY() + 1.0;
        double z = worldPosition.getZ() + 0.5;
        if (medium.waterLike()) {
            ParticleBudget.puff(server, ModParticles.STEAM.get(), x, y, z, 16, 0.4, 0.5, 0.4, 0.06);
            ParticleBudget.puff(server, ParticleTypes.SPLASH, x, y, z, 8, 0.35, 0.3, 0.35, 0.1);
            SoundHelper.play(server, worldPosition, ModSounds.QUENCH_WATER, SoundEvents.FIRE_EXTINGUISH,
                SoundSource.BLOCKS, 1.0F, 1.0F);
        } else {
            ParticleBudget.puff(server, ParticleTypes.SMOKE, x, y, z, 10, 0.35, 0.45, 0.35, 0.04);
            ParticleBudget.puff(server, ParticleTypes.BUBBLE, x, y - 0.3, z, 8, 0.25, 0.2, 0.25, 0.05);
            ParticleBudget.puff(server, ModParticles.EMBER.get(), x, y, z, 4, 0.25, 0.3, 0.25, 0.03);
            SoundHelper.play(server, worldPosition, ModSounds.QUENCH_OIL, SoundEvents.LAVA_EXTINGUISH,
                SoundSource.BLOCKS, 0.9F, 0.8F);
        }
    }

    public void dropContents(Level level, BlockPos pos) {
        ItemStack stack = items.getStackInSlot(0);
        if (!stack.isEmpty()) {
            net.minecraft.world.Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("menu.true_metallurgy.quenching");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new QuenchingMenu(id, inventory, worldPosition, data);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Items", items.serializeNBT(registries));
        tag.putString("Liquid", liquid);
        tag.putInt("Units", units);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Items", Tag.TAG_COMPOUND)) {
            items.deserializeNBT(registries, tag.getCompound("Items"));
        }
        liquid = tag.contains("Liquid") ? tag.getString("Liquid") : "none";
        if (!QuenchMedium.exists(liquid) && !liquid.equals("none")) liquid = "none";
        units = tag.getInt("Units");
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

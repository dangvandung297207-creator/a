package com.masterblacksmith.blockentity;

import com.masterblacksmith.MBSConfig;
import com.masterblacksmith.ModBlockEntities;
import com.masterblacksmith.ModParticles;
import com.masterblacksmith.ModSounds;
import com.masterblacksmith.block.ForgeHearthBlock;
import com.masterblacksmith.block.ForgeTier;
import com.masterblacksmith.forging.ForgingData;
import com.masterblacksmith.forging.HeatingHelper;
import com.masterblacksmith.menu.ForgeHearthMenu;
import com.masterblacksmith.util.ParticleUtil;
import com.masterblacksmith.util.SoundUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Heat simulation, fuel burning and billet soaking for one hearth. */
public class ForgeHearthBlockEntity extends BlockEntity implements net.minecraft.world.MenuProvider {
    private final ItemStackHandler inventory = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            sync();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot == 0) return ForgeHooks.getBurnTime(stack, RecipeType.SMELTING) > 0;
            return ForgingData.isWorkable(stack);
        }
    };
    private LazyOptional<ItemStackHandler> handler = LazyOptional.empty();

    private float temp = ForgingData.ROOM_TEMP;
    private int burnTime;
    private int maxBurnTime;
    private float boostHeat;
    private int tickCounter;

    private final ContainerData data = new ContainerData() {
        @Override public int get(int i) {
            return switch (i) {
                case 0 -> Math.round(temp);
                case 1 -> burnTime;
                case 2 -> maxBurnTime;
                case 3 -> heatState();
                case 4 -> hasBillet() ? Math.round(ForgingData.getTemp(getBillet())) : 0;
                default -> 0;
            };
        }
        @Override public void set(int i, int v) {}
        @Override public int getCount() { return 5; }
    };

    public ForgeHearthBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FORGE_HEARTH.get(), pos, state);
    }

    public ForgeTier getTier() {
        if (getBlockState().getBlock() instanceof ForgeHearthBlock h) return h.getTier();
        return ForgeTier.PRIMITIVE;
    }

    public boolean addFuel(ItemStack stack) {
        ItemStack one = stack.copyWithCount(1);
        ItemStack rest = inventory.insertItem(0, one, false);
        return rest.isEmpty();
    }

    public ItemStackHandler getInventory() { return inventory; }

    public boolean hasBillet() { return !inventory.getStackInSlot(1).isEmpty(); }
    public ItemStack getBillet() { return inventory.getStackInSlot(1); }

    public void setBillet(ItemStack stack) {
        if (ForgingData.peek(stack).getInt("QN") > 0 && ForgingData.getTemp(stack) < 300F) {
            ForgingData.addReheat(stack);
            ForgingData.addHistory(stack, "reheat");
        }
        inventory.setStackInSlot(1, stack);
    }

    public ItemStack takeBillet() {
        ItemStack out = inventory.getStackInSlot(1).copy();
        inventory.setStackInSlot(1, ItemStack.EMPTY);
        return out;
    }

    public float getTemp() { return temp; }
    public int heatState() {
        if (temp < 80) return 0;
        if (temp < 400) return 1;
        if (temp < 750) return 2;
        if (temp < 1100) return 3;
        if (temp < 1400) return 4;
        return 5;
    }

    public void boostFromBellows(float amount) {
        boostHeat = Math.min(260F, boostHeat + amount);
        setChanged();
    }

    public SimpleContainer inventoryView() {
        SimpleContainer c = new SimpleContainer(2);
        for (int i = 0; i < 2; i++) c.setItem(i, inventory.getStackInSlot(i).copy());
        return c;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ForgeHearthBlockEntity be) {
        int interval = Math.max(1, MBSConfig.TEMP_TICK_INTERVAL.get());
        if (++be.tickCounter < interval) return;
        float dt = interval / 20F;
        be.tickCounter = 0;
        ForgeTier tier = be.getTier();

        if (be.burnTime > 0) be.burnTime = Math.max(0, be.burnTime - interval);
        if (be.burnTime <= 0) {
            ItemStack fuel = be.inventory.getStackInSlot(0);
            if (!fuel.isEmpty()) {
                int base = ForgeHooks.getBurnTime(fuel, RecipeType.SMELTING);
                if (base > 0) {
                    be.maxBurnTime = Math.max(1, Math.round(base / tier.getFuelUse()));
                    be.burnTime = be.maxBurnTime;
                    fuel.shrink(1);
                }
            }
        }

        be.boostHeat = Math.max(0F, be.boostHeat - 30F * dt);
        float target = be.burnTime > 0 ? tier.getMaxTemp() + be.boostHeat : ForgingData.ROOM_TEMP;
        float rate = be.burnTime > 0 ? tier.getHeatRate() : 26F;
        be.temp = HeatingHelper.approach(be.temp, target, rate, dt);

        if (be.hasBillet()) {
            ItemStack billet = be.getBillet();
            float billetTarget = be.temp;
            String metal = ForgingData.getMetalId(billet);
            if (metal.equals("starfall_steel") && tier != ForgeTier.MASTER
                    && MBSConfig.STARFALL_REQUIRES_MASTER_FORGE.get()) {
                billetTarget = Math.min(billetTarget, 990F);
            }
            ForgingData.setTemp(billet, HeatingHelper.approach(
                    ForgingData.getTemp(billet), billetTarget, 110F, dt));
        }

        int want = be.heatState();
        if (state.getValue(ForgeHearthBlock.HEAT) != want) {
            level.setBlock(pos, state.setValue(ForgeHearthBlock.HEAT, want), 3);
        }
        be.setChanged();
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, ForgeHearthBlockEntity be) {
        int heat = state.getValue(ForgeHearthBlock.HEAT);
        if (heat >= 2 && level.random.nextFloat() < 0.05F * heat) {
            SoundUtil.playBlock(level, pos, ModSounds.FORGE_CRACKLE.get(), 0.35F + 0.1F * heat, 1.0F);
        }
        if (be.boostHeat > 20 && level instanceof ServerLevel sl) {
            ParticleUtil.burst(sl, ModParticles.EMBER.get(),
                    pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, 2);
        }
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
        tag.putFloat("Temp", temp);
        tag.putInt("Burn", burnTime);
        tag.putInt("MaxBurn", maxBurnTime);
        tag.putFloat("Boost", boostHeat);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        inventory.deserializeNBT(tag.getCompound("Inventory"));
        temp = tag.getFloat("Temp");
        burnTime = tag.getInt("Burn");
        maxBurnTime = tag.getInt("MaxBurn");
        boostHeat = tag.getFloat("Boost");
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
        return Component.translatable("menu.masterblacksmith.forge_hearth");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new ForgeHearthMenu(id, inv, this, data);
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

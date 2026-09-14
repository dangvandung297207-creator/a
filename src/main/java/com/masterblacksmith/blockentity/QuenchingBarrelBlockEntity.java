package com.masterblacksmith.blockentity;

import com.masterblacksmith.ModBlockEntities;
import com.masterblacksmith.ModParticles;
import com.masterblacksmith.ModSounds;
import com.masterblacksmith.block.QuenchingBarrelBlock;
import com.masterblacksmith.forging.ForgingData;
import com.masterblacksmith.forging.QualityCalculator;
import com.masterblacksmith.item.BlacksmithTongsItem;
import com.masterblacksmith.material.MetalMaterial;
import com.masterblacksmith.material.QuenchLiquid;
import com.masterblacksmith.material.QuenchLiquids;
import com.masterblacksmith.util.ParticleUtil;
import com.masterblacksmith.util.SoundUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Liquid memory + the quench itself: steam, hiss and a new metallurgy profile. */
public class QuenchingBarrelBlockEntity extends BlockEntity {
    private String liquid = "";
    private int amount;

    public QuenchingBarrelBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BARREL.get(), pos, state);
    }

    public String getLiquidId() { return liquid; }
    public int getAmount() { return amount; }

    public Component describeLiquid() {
        if (amount <= 0 || liquid.isEmpty()) {
            return Component.translatable("quench.masterblacksmith.empty");
        }
        return QuenchLiquids.get(liquid).getDisplayName();
    }

    public boolean tryFill(String id, int fill) {
        if (!QuenchLiquids.exists(id)) return false;
        if (amount > 0 && !liquid.equals(id)) return false;
        liquid = id;
        amount = Math.min(3, amount + fill);
        updateBlockstate();
        setChanged();
        sync();
        return true;
    }

    public void drain(int n) {
        amount = Math.max(0, amount - n);
        if (amount == 0) liquid = "";
        updateBlockstate();
        setChanged();
        sync();
    }

    private void updateBlockstate() {
        if (level == null) return;
        BlockState state = getBlockState();
        if (!(state.getBlock() instanceof QuenchingBarrelBlock)) return;
        QuenchingBarrelBlock.LiquidDisplay display;
        if (amount <= 0 || liquid.isEmpty()) display = QuenchingBarrelBlock.LiquidDisplay.NONE;
        else if (liquid.equals("water") || liquid.equals("salt_water")) display = QuenchingBarrelBlock.LiquidDisplay.WATER;
        else if (liquid.equals("oil")) display = QuenchingBarrelBlock.LiquidDisplay.OIL;
        else display = QuenchingBarrelBlock.LiquidDisplay.SPECIAL;
        level.setBlock(worldPosition, state.setValue(QuenchingBarrelBlock.LIQUID, display)
                .setValue(QuenchingBarrelBlock.LEVEL, amount), 3);
    }

    /**
     * Quench the finished piece carried by tongs. Returns false when the
     * barrel is dry. The metal cools toward room temperature almost instantly;
     * the medium decides the final profile.
     */
    public boolean quench(ServerPlayer player, ItemStack tongs) {
        if (level == null || level.isClientSide) return false;
        if (amount <= 0 || liquid.isEmpty()) return false;
        ItemStack carried = BlacksmithTongsItem.getCarried(tongs);
        if (carried.isEmpty() || !ForgingData.isQuenchable(carried)) return false;

        MetalMaterial metal = ForgingData.getMetal(carried);
        QuenchLiquid medium = QuenchLiquids.get(liquid);
        float quenchTemp = ForgingData.getTemp(carried);
        float quality = QualityCalculator.quenchQuality(quenchTemp, metal.getSweetMin(), metal.getForgeMax());

        ForgingData.setQuenched(carried, liquid, quality);
        ForgingData.setTemp(carried, ForgingData.ROOM_TEMP + 40F);
        ForgingData.setPurity(carried, ForgingData.getPurity(carried) + medium.getPurityBonus());
        ForgingData.addHistory(carried, "quenched." + liquid);
        BlacksmithTongsItem.setCarried(tongs, carried);

        amount--;
        if (amount <= 0) liquid = "";
        updateBlockstate();

        quenchFeedback(medium, quenchTemp);
        setChanged();
        sync();
        return true;
    }

    private void quenchFeedback(QuenchLiquid medium, float quenchTemp) {
        if (!(level instanceof ServerLevel sl)) return;
        double x = worldPosition.getX() + 0.5;
        double y = worldPosition.getY() + 0.8;
        double z = worldPosition.getZ() + 0.5;
        float heatFactor = Math.min(1.5F, Math.max(0.3F, quenchTemp / 800F));
        int steam = Math.round(18 * medium.getSteam() * heatFactor);
        ParticleUtil.burst(sl, ModParticles.QUENCH_STEAM.get(), x, y, z, steam, 0.35, 0.25);
        if (medium.isDarkSmoke()) {
            ParticleUtil.burst(sl, ParticleTypes.SMOKE, x, y, z, 8, 0.25, 0.12);
        } else {
            ParticleUtil.burst(sl, ParticleTypes.CLOUD, x, y, z, 8, 0.3, 0.15);
            ParticleUtil.burst(sl, ParticleTypes.SPLASH, x, y, z, 10, 0.3, 0.3);
        }
        if (medium.getId().equals("water") || medium.getId().equals("salt_water")) {
            SoundUtil.play(level, worldPosition, ModSounds.QUENCH_WATER.get(),
                    net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F, 0.1F);
            SoundUtil.play(level, worldPosition, net.minecraft.sounds.SoundEvents.FIRE_EXTINGUISH,
                    net.minecraft.sounds.SoundSource.BLOCKS, 0.8F, 1.2F, 0.1F);
        } else if (medium.getId().equals("oil")) {
            SoundUtil.play(level, worldPosition, ModSounds.QUENCH_OIL.get(),
                    net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F, 0.1F);
        } else {
            SoundUtil.play(level, worldPosition, ModSounds.QUENCH_SPECIAL.get(),
                    net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F, 0.08F);
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, QuenchingBarrelBlockEntity be) {
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, QuenchingBarrelBlockEntity be) {
    }

    public void sync() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString("Liquid", liquid);
        tag.putInt("Amount", amount);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        liquid = tag.getString("Liquid");
        amount = tag.getInt("Amount");
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

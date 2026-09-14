package com.masterblacksmith.blockentity;

import com.masterblacksmith.ModBlockEntities;
import com.masterblacksmith.ModParticles;
import com.masterblacksmith.ModSounds;
import com.masterblacksmith.block.BellowsBlock;
import com.masterblacksmith.util.ParticleUtil;
import com.masterblacksmith.util.SoundUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Manual pumping plus redstone-driven automation for late game. */
public class BellowsBlockEntity extends BlockEntity {
    private int cooldown;

    public BellowsBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BELLOWS.get(), pos, state);
    }

    /** Drive oxygen into every adjacent hearth. */
    public void pump() {
        if (level == null || level.isClientSide) return;
        boolean hit = false;
        for (Direction dir : Direction.values()) {
            BlockPos target = worldPosition.relative(dir);
            if (level.getBlockEntity(target) instanceof ForgeHearthBlockEntity forge) {
                forge.boostFromBellows(120F);
                hit = true;
                if (level instanceof ServerLevel sl) {
                    ParticleUtil.burst(sl, ModParticles.EMBER.get(),
                            target.getX() + 0.5, target.getY() + 1.1, target.getZ() + 0.5, 8);
                }
            }
        }
        SoundUtil.playBlock(level, worldPosition, ModSounds.BELLOWS_PUMP.get(), 0.9F, 1.0F);
        if (hit) {
            SoundUtil.playBlock(level, worldPosition, ModSounds.BELLOWS_AIR.get(), 0.7F, 1.1F);
        }
        cooldown = 10;
        setChanged();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BellowsBlockEntity be) {
        if (be.cooldown > 0) be.cooldown--;
        // Automation: a powered bellows pumps itself on a steady rhythm.
        if (level.hasNeighborSignal(pos) && be.cooldown <= 0) {
            be.pump();
            be.cooldown = 40;
            if (!state.getValue(BellowsBlock.PRESSED)) {
                level.setBlock(pos, state.setValue(BellowsBlock.PRESSED, true), 3);
                level.scheduleTick(pos, state.getBlock(), 8);
            }
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, BellowsBlockEntity be) {
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Cooldown", cooldown);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        cooldown = tag.getInt("Cooldown");
    }
}

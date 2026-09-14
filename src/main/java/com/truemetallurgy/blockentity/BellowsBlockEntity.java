package com.truemetallurgy.blockentity;

import com.truemetallurgy.block.BellowsBlock;
import com.truemetallurgy.block.ForgeHearthBlock;
import com.truemetallurgy.registry.ModBlockEntities;
import com.truemetallurgy.registry.ModParticles;
import com.truemetallurgy.registry.ModSounds;
import com.truemetallurgy.util.ParticleBudget;
import com.truemetallurgy.util.SoundHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Bellows pump air into an adjacent forge. Short cooldown prevents spam;
 * each pump pushes the forge toward forging heat with sound + airflow VFX.
 */
public class BellowsBlockEntity extends BlockEntity {
    private static final int COOLDOWN_TICKS = 12;
    private static final int AIRFLOW_PER_PUMP = 22;

    private long lastPumpTick = -100;
    private int animTick = -1;

    public BellowsBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BELLOWS.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BellowsBlockEntity be) {
        if (level.isClientSide) return;
        if (be.animTick < 0) return;
        be.animTick++;
        int stage = switch (be.animTick) {
            case 0, 1, 2 -> 1;      // pull up
            case 3, 4, 5 -> 2;      // compress + airflow
            case 6, 7 -> 3;         // return
            default -> 0;           // idle
        };
        if (state.getValue(BellowsBlock.STAGE) != stage) {
            level.setBlock(pos, state.setValue(BellowsBlock.STAGE, stage), 3);
        }
        if (be.animTick > 8) {
            be.animTick = -1;
            be.setChanged();
        }
    }

    /** Pump the bellows. Server-side, cooldown-gated, forge-seeking. */
    public void pump(Player player) {
        if (level == null || level.isClientSide) return;
        long now = level.getGameTime();
        if (now - lastPumpTick < COOLDOWN_TICKS) return;
        lastPumpTick = now;
        animTick = 0;
        level.setBlock(worldPosition, getBlockState().setValue(BellowsBlock.STAGE, 1), 3);

        boolean fed = false;
        if (level instanceof ServerLevel server) {
            for (Direction dir : Direction.values()) {
                BlockPos target = worldPosition.relative(dir);
                if (level.getBlockState(target).getBlock() instanceof ForgeHearthBlock
                    && level.getBlockEntity(target) instanceof ForgeHearthBlockEntity forge) {
                    forge.addAirflow(AIRFLOW_PER_PUMP);
                    fed = true;
                    ParticleBudget.puff(server, ModParticles.EMBER.get(),
                        target.getX() + 0.5, target.getY() + 0.8, target.getZ() + 0.5,
                        6, 0.3, 0.3, 0.3, 0.03);
                    ParticleBudget.puff(server, net.minecraft.core.particles.ParticleTypes.FLAME,
                        target.getX() + 0.5, target.getY() + 0.7, target.getZ() + 0.5,
                        3, 0.25, 0.15, 0.25, 0.01);
                }
            }
        }
        player.swing(player.getUsedItemHand(), true);
        SoundHelper.play(level, worldPosition, ModSounds.BELLOWS_WHOOSH, SoundEvents.FIRECHARGE_USE,
            SoundSource.BLOCKS, fed ? 0.9F : 0.5F, fed ? 1.0F : 0.8F);
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLong("LastPump", lastPumpTick);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        lastPumpTick = tag.getLong("LastPump");
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
}

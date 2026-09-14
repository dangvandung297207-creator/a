package com.masterblacksmith.block;

import com.masterblacksmith.ModBlockEntities;
import com.masterblacksmith.ModParticles;
import com.masterblacksmith.blockentity.ForgeHearthBlockEntity;
import com.masterblacksmith.forging.ForgingData;
import com.masterblacksmith.item.BlacksmithTongsItem;
import com.masterblacksmith.util.ParticleUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

/**
 * The forge hearth. Heat states: 0 cold, 1 warm, 2 hot, 3 forging,
 * 4 overheated, 5 molten. Texture + VFX + light follow the state.
 */
public class ForgeHearthBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final IntegerProperty HEAT = IntegerProperty.create("heat", 0, 5);

    private final ForgeTier tier;

    public ForgeHearthBlock(ForgeTier tier, Properties props) {
        super(props);
        this.tier = tier;
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH).setValue(HEAT, 0));
    }

    public ForgeTier getTier() { return tier; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        b.add(FACING, HEAT);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState s, Rotation r) { return s.setValue(FACING, r.rotate(s.getValue(FACING))); }

    @Override
    public BlockState mirror(BlockState s, Mirror m) { return s.rotate(m.getRotation(s.getValue(FACING))); }

    @Override
    public RenderShape getRenderShape(BlockState s) { return RenderShape.MODEL; }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ForgeHearthBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide
                ? createTickerHelper(type, ModBlockEntities.FORGE_HEARTH.get(), ForgeHearthBlockEntity::clientTick)
                : createTickerHelper(type, ModBlockEntities.FORGE_HEARTH.get(), ForgeHearthBlockEntity::serverTick);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(pos) instanceof ForgeHearthBlockEntity forge)) return InteractionResult.PASS;
        ItemStack held = player.getItemInHand(hand);

        // Stoking fuel.
        if (!held.isEmpty() && ForgeHooks.getBurnTime(held, null) > 0) {
            if (forge.addFuel(held)) {
                if (!player.isCreative()) held.shrink(1);
                return InteractionResult.CONSUME;
            }
            return InteractionResult.PASS;
        }

        // Tongs carry hot stock in and out.
        if (held.getItem() instanceof BlacksmithTongsItem) {
            ItemStack carried = BlacksmithTongsItem.getCarried(held);
            if (!carried.isEmpty() && ForgingData.isWorkable(carried) && !forge.hasBillet()) {
                forge.setBillet(carried.copy());
                BlacksmithTongsItem.setCarried(held, ItemStack.EMPTY);
                forge.setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
                return InteractionResult.CONSUME;
            }
            if (carried.isEmpty() && forge.hasBillet()) {
                BlacksmithTongsItem.setCarried(held, forge.takeBillet());
                forge.setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
                return InteractionResult.CONSUME;
            }
            return InteractionResult.PASS;
        }

        // Bare hands: allowed, but hot steel bites.
        if (!held.isEmpty() && ForgingData.isWorkable(held) && !forge.hasBillet()) {
            float temp = ForgingData.getTemp(held);
            forge.setBillet(held.copyWithCount(1));
            if (!player.isCreative()) held.shrink(1);
            if (temp > 300) player.hurt(level.damageSources().hotFloor(), 3.0F);
            level.sendBlockUpdated(pos, state, state, 3);
            return InteractionResult.CONSUME;
        }
        if (held.isEmpty() && forge.hasBillet() && player.isShiftKeyDown()) {
            ItemStack billet = forge.takeBillet();
            float temp = ForgingData.getTemp(billet);
            if (!player.addItem(billet)) player.drop(billet, false);
            if (temp > 300) player.hurt(level.damageSources().hotFloor(), 3.0F);
            level.sendBlockUpdated(pos, state, state, 3);
            return InteractionResult.CONSUME;
        }

        // Management GUI.
        if (held.isEmpty() && player instanceof ServerPlayer sp) {
            NetworkHooks.openScreen(sp, forge, pos);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof ForgeHearthBlockEntity forge) {
            Containers.dropContents(level, pos, forge.inventoryView());
        }
        super.onRemove(state, level, pos, newState, moving);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rand) {
        int heat = state.getValue(HEAT);
        if (heat <= 0) {
            if (rand.nextFloat() < 0.02F) {
                level.addParticle(ParticleTypes.SMOKE,
                        pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, 0, 0.04, 0);
            }
            return;
        }
        double x = pos.getX() + 0.5 + (rand.nextDouble() - 0.5) * 0.6;
        double y = pos.getY() + 1.05;
        double z = pos.getZ() + 0.5 + (rand.nextDouble() - 0.5) * 0.6;
        if (rand.nextFloat() < 0.10F * heat) {
            level.addParticle(ParticleTypes.FLAME, x, y, z, 0, 0.05, 0);
        }
        if (rand.nextFloat() < 0.16F * heat) {
            ParticleUtil.spawnCapped(level, ModParticles.EMBER.get(), x, y, z, 1);
        }
        if (rand.nextFloat() < 0.08F * heat) {
            level.addParticle(ParticleTypes.SMOKE, x, y + 0.2, z, 0, 0.06, 0);
        }
        if (heat >= 4 && rand.nextFloat() < 0.25F) {
            level.addParticle(ParticleTypes.LAVA, x, y, z, 0, 0, 0);
        }
    }
}

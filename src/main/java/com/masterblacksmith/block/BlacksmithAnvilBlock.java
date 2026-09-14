package com.masterblacksmith.block;

import com.masterblacksmith.ModBlockEntities;
import com.masterblacksmith.ModParticles;
import com.masterblacksmith.blockentity.BlacksmithAnvilBlockEntity;
import com.masterblacksmith.forging.ForgingData;
import com.masterblacksmith.forging.HeatingHelper;
import com.masterblacksmith.item.BlacksmithTongsItem;
import com.masterblacksmith.item.SmithingHammerItem;
import com.masterblacksmith.util.ParticleUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

/**
 * The heart of the forge. Four tiers: better tolerance and a higher
 * craftsmanship ceiling (basic 75 / iron 85 / steel 95 / master 100).
 */
public class BlacksmithAnvilBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty HOT = BooleanProperty.create("hot");

    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(1, 0, 1, 15, 4, 15),
            Block.box(4, 4, 4, 12, 8, 12),
            Block.box(0, 8, 2, 16, 13, 14));

    private final int tier;

    public BlacksmithAnvilBlock(int tier, Properties props) {
        super(props);
        this.tier = tier;
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH).setValue(HOT, false));
    }

    public int getTier() { return tier; }

    /** Sweet-zone width multiplier for the forging minigame. */
    public double tolerance() { return 1.0 + tier * 0.18; }

    /** Craftsmanship ceiling this anvil can produce. */
    public int qualityCap() { return switch (tier) { case 0 -> 75; case 1 -> 85; case 2 -> 95; default -> 100; }; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        b.add(FACING, HOT);
    }

    @Override
    public VoxelShape getShape(BlockState s, BlockGetter g, BlockPos p, CollisionContext c) { return SHAPE; }

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
        return new BlacksmithAnvilBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide
                ? createTickerHelper(type, ModBlockEntities.ANVIL.get(), BlacksmithAnvilBlockEntity::clientTick)
                : createTickerHelper(type, ModBlockEntities.ANVIL.get(), BlacksmithAnvilBlockEntity::serverTick);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(pos) instanceof BlacksmithAnvilBlockEntity anvil)) return InteractionResult.PASS;
        ItemStack held = player.getItemInHand(hand);

        if (held.getItem() instanceof BlacksmithTongsItem) {
            ItemStack carried = BlacksmithTongsItem.getCarried(held);
            if (!carried.isEmpty() && ForgingData.isWorkable(carried) && !anvil.hasWork()) {
                anvil.setWork(carried.copy());
                BlacksmithTongsItem.setCarried(held, ItemStack.EMPTY);
                level.sendBlockUpdated(pos, state, state, 3);
                return InteractionResult.CONSUME;
            }
            if (carried.isEmpty() && anvil.hasWork()) {
                BlacksmithTongsItem.setCarried(held, anvil.takeWork());
                level.sendBlockUpdated(pos, state, state, 3);
                return InteractionResult.CONSUME;
            }
            return InteractionResult.PASS;
        }

        if (!held.isEmpty() && ForgingData.isWorkable(held) && !anvil.hasWork()) {
            float temp = ForgingData.getTemp(held);
            anvil.setWork(held.copyWithCount(1));
            if (!player.isCreative()) held.shrink(1);
            if (temp > 300) player.hurt(level.damageSources().hotFloor(), 3.0F);
            level.sendBlockUpdated(pos, state, state, 3);
            return InteractionResult.CONSUME;
        }

        if (held.isEmpty() && anvil.hasWork() && player.isShiftKeyDown()) {
            ItemStack work = anvil.takeWork();
            float temp = ForgingData.getTemp(work);
            if (!player.addItem(work)) player.drop(work, false);
            if (temp > 300) player.hurt(level.damageSources().hotFloor(), 3.0F);
            level.sendBlockUpdated(pos, state, state, 3);
            return InteractionResult.CONSUME;
        }

        if (held.isEmpty() && player instanceof ServerPlayer sp) {
            NetworkHooks.openScreen(sp, anvil, pos);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    /** In-world hammering: left-click the anvil with a smithing hammer. */
    @Override
    public void attack(BlockState state, Level level, BlockPos pos, Player player) {
        if (level.isClientSide) return;
        if (!(player instanceof ServerPlayer sp)) return;
        if (!(sp.getMainHandItem().getItem() instanceof SmithingHammerItem)) return;
        if (level.getBlockEntity(pos) instanceof BlacksmithAnvilBlockEntity anvil) {
            anvil.worldStrike(sp);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof BlacksmithAnvilBlockEntity anvil) {
            Containers.dropContents(level, pos, anvil.inventoryView());
        }
        super.onRemove(state, level, pos, newState, moving);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rand) {
        if (!state.getValue(HOT)) return;
        if (level.getBlockEntity(pos) instanceof BlacksmithAnvilBlockEntity anvil && anvil.hasWork()) {
            float temp = ForgingData.getTemp(anvil.getWork());
            if (HeatingHelper.glowIntensity(temp) > 0.05F && rand.nextFloat() < 0.35F) {
                ParticleUtil.spawnCapped(level, ModParticles.EMBER.get(),
                        pos.getX() + 0.5 + (rand.nextDouble() - 0.5) * 0.5,
                        pos.getY() + 0.85,
                        pos.getZ() + 0.5 + (rand.nextDouble() - 0.5) * 0.5, 1);
            }
        }
    }
}

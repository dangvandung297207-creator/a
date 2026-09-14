package com.masterblacksmith.block;

import com.masterblacksmith.ModBlockEntities;
import com.masterblacksmith.ModParticles;
import com.masterblacksmith.blockentity.GrindingWheelBlockEntity;
import com.masterblacksmith.forging.ForgingData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

/** Grinding station: edge angle vs durability, wheel wear, sparks and dust. */
public class GrindingWheelBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty SPINNING = BooleanProperty.create("spinning");

    public GrindingWheelBlock(Properties props) {
        super(props);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH).setValue(SPINNING, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        b.add(FACING, SPINNING);
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
        return new GrindingWheelBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide
                ? createTickerHelper(type, ModBlockEntities.GRINDING_WHEEL.get(), GrindingWheelBlockEntity::clientTick)
                : createTickerHelper(type, ModBlockEntities.GRINDING_WHEEL.get(), GrindingWheelBlockEntity::serverTick);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(pos) instanceof GrindingWheelBlockEntity wheel)) return InteractionResult.PASS;
        ItemStack held = player.getItemInHand(hand);

        // Dress the wheel with flint.
        if (held.is(Items.FLINT) && player.isShiftKeyDown()) {
            if (wheel.repair(25)) {
                if (!player.isCreative()) held.shrink(1);
                return InteractionResult.CONSUME;
            }
            return InteractionResult.PASS;
        }

        if (!held.isEmpty() && ForgingData.isAssemblyReady(held) && !ForgingData.isGround(held) && !wheel.hasBlade()) {
            wheel.setBlade(held.copyWithCount(1));
            if (!player.isCreative()) held.shrink(1);
            level.sendBlockUpdated(pos, state, state, 3);
            return InteractionResult.CONSUME;
        }
        if (held.isEmpty() && wheel.hasBlade() && player.isShiftKeyDown()) {
            ItemStack blade = wheel.takeBlade();
            if (!player.addItem(blade)) player.drop(blade, false);
            level.sendBlockUpdated(pos, state, state, 3);
            return InteractionResult.CONSUME;
        }
        if (held.isEmpty() && player instanceof ServerPlayer sp) {
            NetworkHooks.openScreen(sp, wheel, pos);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rand) {
        if (state.getValue(SPINNING)) level.setBlock(pos, state.setValue(SPINNING, false), 3);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof GrindingWheelBlockEntity wheel) {
            Containers.dropContents(level, pos, wheel.inventoryView());
        }
        super.onRemove(state, level, pos, newState, moving);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rand) {
        if (!state.getValue(SPINNING)) return;
        if (rand.nextFloat() < 0.7F) {
            level.addParticle(ModParticles.GRIND_DUST.get(),
                    pos.getX() + 0.5 + (rand.nextDouble() - 0.5) * 0.6,
                    pos.getY() + 0.8,
                    pos.getZ() + 0.5 + (rand.nextDouble() - 0.5) * 0.6,
                    (rand.nextDouble() - 0.5) * 0.4, 0.1, (rand.nextDouble() - 0.5) * 0.4);
        }
    }
}

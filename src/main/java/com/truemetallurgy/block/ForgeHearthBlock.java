package com.truemetallurgy.block;

import com.mojang.serialization.MapCodec;
import com.truemetallurgy.blockentity.ForgeHearthBlockEntity;
import com.truemetallurgy.registry.ModBlockEntities;
import com.truemetallurgy.util.TMUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * The central workstation. Heat is simulated in the block entity; this block
 * mirrors the {@link #HEAT} state (0-5) for light and visuals.
 */
public class ForgeHearthBlock extends BaseEntityBlock {
    public static final MapCodec<ForgeHearthBlock> CODEC = simpleCodec(props -> new ForgeHearthBlock(props, ForgeTier.PRIMITIVE));

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    /** 0 cold .. 5 molten, mirrors Heat.ForgeState ordinals. */
    public static final IntegerProperty HEAT = IntegerProperty.create("heat", 0, 5);

    private static final VoxelShape SHAPE = Shapes.block();

    private final ForgeTier tier;

    public ForgeHearthBlock(Properties properties, ForgeTier tier) {
        super(properties);
        this.tier = tier;
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(HEAT, 0));
    }

    public ForgeTier getTier() {
        return tier;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, HEAT);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    public static int heatLight(BlockState state) {
        return TMUtil.clamp(state.getValue(HEAT) * 3, 0, 15);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ForgeHearthBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return createTickerHelper(type, ModBlockEntities.FORGE.get(), ForgeHearthBlockEntity::tick);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof MenuProvider menu) {
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.openMenu(menu, buf -> buf.writeBlockPos(pos));
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public net.minecraft.world.ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
            BlockPos pos, Player player, net.minecraft.world.InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return net.minecraft.world.ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (level.getBlockEntity(pos) instanceof ForgeHearthBlockEntity forge
            && forge.tryInsertFromHand(player, hand, stack)) {
            return net.minecraft.world.ItemInteractionResult.CONSUME;
        }
        return net.minecraft.world.ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof ForgeHearthBlockEntity forge) {
            forge.dropContents(level, pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return state.getValue(HEAT) * 3;
    }
}

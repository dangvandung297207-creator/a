package com.masterblacksmith.block;

import com.masterblacksmith.ModBlockEntities;
import com.masterblacksmith.blockentity.QuenchingBarrelBlockEntity;
import com.masterblacksmith.forging.ForgingData;
import com.masterblacksmith.item.BlacksmithTongsItem;
import com.masterblacksmith.item.QuenchBucketItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/** Quenching barrel. Water, oil and the advanced media each behave differently. */
public class QuenchingBarrelBlock extends BaseEntityBlock {
    public enum LiquidDisplay implements StringRepresentable {
        NONE("none"), WATER("water"), OIL("oil"), SPECIAL("special");
        private final String key;
        LiquidDisplay(String key) { this.key = key; }
        @Override public String getSerializedName() { return key; }
    }

    public static final EnumProperty<LiquidDisplay> LIQUID = EnumProperty.create("liquid", LiquidDisplay.class);
    public static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, 3);

    public QuenchingBarrelBlock(Properties props) {
        super(props);
        registerDefaultState(defaultBlockState().setValue(LIQUID, LiquidDisplay.NONE).setValue(LEVEL, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        b.add(LIQUID, LEVEL);
    }

    @Override
    public RenderShape getRenderShape(BlockState s) { return RenderShape.MODEL; }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new QuenchingBarrelBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide
                ? createTickerHelper(type, ModBlockEntities.BARREL.get(), QuenchingBarrelBlockEntity::clientTick)
                : createTickerHelper(type, ModBlockEntities.BARREL.get(), QuenchingBarrelBlockEntity::serverTick);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(pos) instanceof QuenchingBarrelBlockEntity barrel)) return InteractionResult.PASS;
        ItemStack held = player.getItemInHand(hand);

        // Filling.
        if (held.is(Items.WATER_BUCKET)) {
            if (barrel.tryFill("water", 3)) {
                player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                return InteractionResult.CONSUME;
            }
            return InteractionResult.PASS;
        }
        if (held.getItem() instanceof QuenchBucketItem q) {
            if (barrel.tryFill(q.getLiquidId(), 3)) {
                if (!player.isCreative()) {
                    player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                }
                return InteractionResult.CONSUME;
            }
            return InteractionResult.PASS;
        }
        if (held.is(Items.BUCKET) && barrel.getLiquidId().equals("water") && barrel.getAmount() > 0 && player.isShiftKeyDown()) {
            barrel.drain(3);
            player.setItemInHand(hand, new ItemStack(Items.WATER_BUCKET));
            return InteractionResult.CONSUME;
        }

        // Quenching with tongs.
        if (held.getItem() instanceof BlacksmithTongsItem && player instanceof ServerPlayer sp) {
            ItemStack carried = BlacksmithTongsItem.getCarried(held);
            if (!carried.isEmpty() && ForgingData.isQuenchable(carried)) {
                if (barrel.quench(sp, held)) return InteractionResult.CONSUME;
                player.displayClientMessage(Component.translatable("message.masterblacksmith.barrel_empty"), true);
                return InteractionResult.PASS;
            }
        }

        // Status readout.
        player.displayClientMessage(Component.translatable("message.masterblacksmith.barrel_status",
                barrel.describeLiquid(), barrel.getAmount()), true);
        return InteractionResult.CONSUME;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rand) {
        if (state.getValue(LIQUID) == LiquidDisplay.NONE || state.getValue(LEVEL) <= 0) return;
        if (rand.nextFloat() < 0.06F) {
            level.addParticle(ParticleTypes.BUBBLE_POP,
                    pos.getX() + 0.3 + rand.nextDouble() * 0.4,
                    pos.getY() + 0.75,
                    pos.getZ() + 0.3 + rand.nextDouble() * 0.4,
                    0, 0.03, 0);
        }
    }
}

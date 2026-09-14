package com.truemetallurgy.blockentity;

import com.truemetallurgy.block.AnvilTier;
import com.truemetallurgy.block.BlacksmithAnvilBlock;
import com.truemetallurgy.components.BilletData;
import com.truemetallurgy.components.ForgedComponentData;
import com.truemetallurgy.forging.ForgingLogic;
import com.truemetallurgy.forging.ForgingSession;
import com.truemetallurgy.forging.HotMetal;
import com.truemetallurgy.forging.StrikeResult;
import com.truemetallurgy.item.BlacksmithTongsItem;
import com.truemetallurgy.item.ComponentKind;
import com.truemetallurgy.item.HammerTier;
import com.truemetallurgy.item.SmithingHammerItem;
import com.truemetallurgy.menu.ForgingMenu;
import com.truemetallurgy.metallurgy.ForgingShape;
import com.truemetallurgy.metallurgy.Heat;
import com.truemetallurgy.metallurgy.Material;
import com.truemetallurgy.metallurgy.Materials;
import com.truemetallurgy.network.ModNetworking;
import com.truemetallurgy.registry.ModBlockEntities;
import com.truemetallurgy.registry.ModDataComponents;
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
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

/**
 * The anvil owns the forging session: workpiece, moving target zone, strike
 * rhythm and delayed windup impacts. The client only renders; every number
 * is computed here on the server.
 */
public class BlacksmithAnvilBlockEntity extends BlockEntity implements MenuProvider {
    private final ItemStackHandler items = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            sync();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return HotMetal.isWorkpiece(stack) && getStackInSlot(slot).isEmpty();
        }
    };

    private final ForgingSession session = new ForgingSession();

    private final ContainerData data = new ContainerData() {
        @Override public int get(int i) {
            ItemStack work = items.getStackInSlot(0);
            ForgedComponentData comp = work.get(ModDataComponents.COMPONENT.get());
            Material mat = materialOf(work);
            return switch (i) {
                case ForgingMenu.D_TEMP -> HotMetal.temperatureOf(work);
                case ForgingMenu.D_HEAT_ACC -> Math.round(mat.heatAccuracy(Math.max(0, HotMetal.temperatureOf(work))) * 100.0F);
                case ForgingMenu.D_STAGE -> comp != null ? comp.stage() : 0;
                case ForgingMenu.D_STAGE_PROG -> comp != null ? comp.stageProgress() : 0;
                case ForgingMenu.D_STAGE_REQ -> stageRequired(work, comp);
                case ForgingMenu.D_SCORE -> comp != null ? Math.round(comp.score()) : 0;
                case ForgingMenu.D_STRIKES -> comp != null ? comp.strikes() : 0;
                case ForgingMenu.D_TARGET_X -> Math.round(session.targetX() * 100.0F);
                case ForgingMenu.D_TARGET_Z -> Math.round(session.targetZ() * 100.0F);
                case ForgingMenu.D_HAS_WORK -> work.isEmpty() ? 0 : 1;
                case ForgingMenu.D_KIND -> ComponentKind.byShapeId(session.targetKind()).ordinal();
                case ForgingMenu.D_FLAGS -> flags(work, comp);
                default -> 0;
            };
        }
        @Override public void set(int i, int v) {}
        @Override public int getCount() { return ForgingMenu.DATA_COUNT; }
    };

    public BlacksmithAnvilBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ANVIL.get(), pos, state);
    }

    public AnvilTier tier() {
        if (getBlockState().getBlock() instanceof BlacksmithAnvilBlock anvil) return anvil.getTier();
        return AnvilTier.BASIC;
    }

    public ItemStackHandler getItemHandler() {
        return items;
    }

    public ForgingSession session() {
        return session;
    }

    public boolean hasWorkpiece() {
        return !items.getStackInSlot(0).isEmpty();
    }

    private Material materialOf(ItemStack work) {
        if (work.isEmpty()) return Materials.IRON;
        ForgedComponentData comp = work.get(ModDataComponents.COMPONENT.get());
        if (comp != null) return Materials.get(comp.materialId());
        BilletData billet = work.get(ModDataComponents.BILLET.get());
        if (billet != null) return Materials.get(billet.materialId());
        String bloom = ForgingLogic.bloomMaterial(work);
        if (bloom != null) return Materials.get(bloom);
        if (work.getItem() instanceof com.truemetallurgy.item.BilletItem b) return b.getMaterial();
        return Materials.IRON;
    }

    private int stageRequired(ItemStack work, @Nullable ForgedComponentData comp) {
        if (work.isEmpty()) return 0;
        boolean bloom = ForgingLogic.bloomMaterial(work) != null && comp == null;
        ForgingShape shape = bloom ? ForgingShape.BILLET : ForgingShape.forComponent(session.targetKind());
        int stage = comp != null ? comp.stage() : 0;
        if (stage < 0 || stage >= shape.stages().size()) return 0;
        return shape.stages().get(stage).strikesRequired();
    }

    private int flags(ItemStack work, @Nullable ForgedComponentData comp) {
        int f = 0;
        if (!work.isEmpty()) f |= 1;
        if (comp != null && comp.isFinished()) f |= 2;
        if (ForgingLogic.bloomMaterial(work) != null) f |= 4;
        if (HotMetal.isHotMetal(work)) f |= 8;
        return f;
    }

    // ---------------- interaction ----------------

    public boolean interact(Player player, InteractionHand hand, ItemStack stack, BlockHitResult hit) {
        if (!(player instanceof ServerPlayer sp)) return false;
        if (stack.getItem() instanceof SmithingHammerItem hammer) {
            return queueStrike(sp, hand, stack, hammer, hit);
        }
        ItemStack work = HotMetal.extractWorkpiece(sp, hand, stack);
        if (!work.isEmpty()) {
            return placeWorkpiece(sp, work);
        }
        if (stack.getItem() instanceof BlacksmithTongsItem) {
            ItemStack other = sp.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
            if (other.isEmpty()) {
                return takeWorkpiece(sp);
            }
        }
        return false;
    }

    private boolean placeWorkpiece(ServerPlayer player, ItemStack work) {
        if (hasWorkpiece()) {
            HotMetal.feedback(player, "message.true_metallurgy.anvil_occupied");
            return true;
        }
        ItemStack one = work.copyWithCount(1);
        items.setStackInSlot(0, one);
        work.shrink(1);
        session.retarget(player.getRandom());
        SoundHelper.play(player.level(), worldPosition, ModSounds.TONGS_CLINK, SoundEvents.ANVIL_LAND,
            SoundSource.BLOCKS, 0.5F, 1.4F);
        setChanged();
        sync();
        return true;
    }

    /** Shift-empty-hand or tongs take. Hot metal still demands tongs. */
    public boolean takeWorkpiece(Player player) {
        ItemStack work = items.getStackInSlot(0);
        if (work.isEmpty()) return false;
        if (HotMetal.isHotMetal(work) && !HotMetal.playerHasTongs(player)) {
            if (player instanceof ServerPlayer sp) {
                player.hurt(player.level().damageSources().hotFloor(), 3.0F);
                HotMetal.feedback(sp, "message.true_metallurgy.need_tongs");
            }
            return true;
        }
        items.setStackInSlot(0, ItemStack.EMPTY);
        if (!player.getInventory().add(work)) {
            player.drop(work, false);
        }
        if (player.level() instanceof ServerLevel server) {
            SoundHelper.play(server, worldPosition, ModSounds.TONGS_CLINK, SoundEvents.ANVIL_LAND,
                SoundSource.BLOCKS, 0.5F, 1.2F);
        }
        setChanged();
        sync();
        return true;
    }

    private boolean queueStrike(ServerPlayer player, InteractionHand hand, ItemStack hammerStack,
            SmithingHammerItem hammer, BlockHitResult hit) {
        ItemStack work = items.getStackInSlot(0);
        if (work.isEmpty()) {
            SoundHelper.play(player.level(), worldPosition, hammerSound(hammer.getHammerTier()), SoundEvents.ANVIL_LAND,
                SoundSource.BLOCKS, 0.4F, 1.6F);
            HotMetal.feedback(player, "message.true_metallurgy.anvil_empty");
            return true;
        }
        if (session.hasPendingAny()) return true;
        if (player.getCooldowns().isOnCooldown(hammerStack.getItem())) return true;

        String kind = ForgingLogic.bloomMaterial(work) != null
            && work.get(ModDataComponents.COMPONENT.get()) == null ? "billet" : session.targetKind();
        Material mat = ForgingLogic.ensureProgress(work, kind, player);
        if (!tier().canShape(mat.forgingDifficulty())) {
            HotMetal.feedback(player, "message.true_metallurgy.anvil_too_weak");
            return true;
        }

        ServerLevel server = (ServerLevel) player.level();
        hammerStack.hurtAndBreak(1, server, player,
            item -> player.onEquippedItemBroken(item, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND));
        player.getCooldowns().addCooldown(hammerStack.getItem(), hammer.getHammerTier().strikeCooldown);
        player.swing(hand, true);
        SoundHelper.play(server, worldPosition, ModSounds.BELLOWS_WHOOSH, SoundEvents.PLAYER_ATTACK_SWEEP,
            SoundSource.PLAYERS, 0.35F, 1.6F);

        Vec3 loc = hit.getLocation();
        float hx = (float) (loc.x - (worldPosition.getX() + 0.5));
        float hz = (float) (loc.z - (worldPosition.getZ() + 0.5));
        session.queueImpact(server.getGameTime() + 4, player.getUUID(),
            hammer.getHammerTier().ordinal(), hx, hz, server.getGameTime());
        setChanged();
        return true;
    }

    // ---------------- tick + impact ----------------

    public static void tick(Level level, BlockPos pos, BlockState state, BlacksmithAnvilBlockEntity be) {
        if (level.isClientSide) return;
        ServerLevel server = (ServerLevel) level;
        ItemStack work = be.items.getStackInSlot(0);
        if (!work.isEmpty() && server.getGameTime() % 20 == 0) {
            int temp = HotMetal.temperatureOf(work);
            if (temp > Heat.ROOM_TEMP) {
                HotMetal.coolStep(work, Math.max(2, (temp - Heat.ROOM_TEMP) / 18));
                be.setChanged();
            }
        }
        if (be.session.hasPending(server.getGameTime())) {
            be.resolveImpact(server);
        }
    }

    private void resolveImpact(ServerLevel server) {
        ItemStack work = items.getStackInSlot(0);
        ServerPlayer player = session.pendingPlayer() != null ? (ServerPlayer) server.getPlayerByUUID(session.pendingPlayer()) : null;
        int hammerOrd = session.pendingHammerOrdinal();
        float hx = session.pendingHitX();
        float hz = session.pendingHitZ();
        session.clearPending();
        if (work.isEmpty() || player == null || hammerOrd < 0) {
            setChanged();
            return;
        }
        HammerTier hammer = HammerTier.values()[Math.min(hammerOrd, HammerTier.values().length - 1)];
        boolean bloom = ForgingLogic.bloomMaterial(work) != null
            && (work.get(ModDataComponents.COMPONENT.get()) == null
                || "billet".equals(work.get(ModDataComponents.COMPONENT.get()).kind()));
        ForgingShape shape = bloom ? ForgingShape.BILLET : ForgingShape.forComponent(session.targetKind());
        ForgedComponentData comp = work.get(ModDataComponents.COMPONENT.get());
        if (comp == null) {
            comp = work.get(ModDataComponents.COMPONENT.get());
            if (comp == null) {
                setChanged();
                return;
            }
        }
        Material mat = Materials.get(comp.materialId());
        int temp = HotMetal.temperatureOf(work);
        long sinceLast = server.getGameTime() - session.lastStrikeTick();
        StrikeResult result = ForgingLogic.evaluate(temp, mat, comp, shape,
            session.targetX(), session.targetZ(), hx, hz, sinceLast, hammer, tier());
        boolean finished = ForgingLogic.applyStrike(work, result, shape, session, tier().legendaryBonus);
        session.markStruck(server.getGameTime());
        session.retarget(server.random);

        strikeEffects(server, player, result, hammer, temp);
        if (finished) {
            ItemStack out = bloom ? ForgingLogic.finishBillet(work)
                : ForgingLogic.finishComponent(work, ComponentKind.byShapeId(session.targetKind()));
            items.setStackInSlot(0, out);
            ForgedComponentData done = out.get(ModDataComponents.COMPONENT.get());
            int score = done != null ? Math.round(done.score()) : 0;
            HotMetal.feedback(player, "message.true_metallurgy.component_finished", score);
            SoundHelper.play(server, worldPosition, ModSounds.MASTERWORK_CHIME, SoundEvents.PLAYER_LEVELUP,
                SoundSource.BLOCKS, 0.4F, score >= 70 ? 1.2F : 0.9F);
        }
        setChanged();
        sync();
    }

    private void strikeEffects(ServerLevel server, ServerPlayer player, StrikeResult result, HammerTier hammer, int temp) {
        double x = worldPosition.getX() + 0.5;
        double y = worldPosition.getY() + 0.85;
        double z = worldPosition.getZ() + 0.5;
        switch (result.grade()) {
            case PERFECT -> {
                ParticleBudget.puff(server, ModParticles.SPARK.get(), x, y, z, 10, 0.35, 0.45, 0.35, 0.12);
                ParticleBudget.puff(server, ParticleTypes.FLASH, x, y, z, 1, 0.05, 0.05, 0.05, 0.0);
                SoundHelper.play(server, worldPosition, ModSounds.STRIKE_PERFECT, SoundEvents.ANVIL_USE,
                    SoundSource.BLOCKS, 0.9F, 1.1F);
            }
            case GOOD -> {
                ParticleBudget.puff(server, ModParticles.SPARK.get(), x, y, z, 6, 0.3, 0.4, 0.3, 0.1);
                SoundHelper.play(server, worldPosition, hammerSound(hammer), SoundEvents.ANVIL_USE,
                    SoundSource.BLOCKS, 0.8F, 1.0F);
            }
            case MISS -> {
                ParticleBudget.puff(server, ModParticles.SPARK.get(), x, y, z, 1, 0.2, 0.2, 0.2, 0.05);
                SoundHelper.play(server, worldPosition, hammerSound(hammer), SoundEvents.ANVIL_LAND,
                    SoundSource.BLOCKS, 0.6F, 0.7F);
            }
            case BAD -> {
                ParticleBudget.puff(server, ModParticles.SPARK.get(), x, y, z, 3, 0.25, 0.3, 0.25, 0.08);
                ParticleBudget.puff(server, ParticleTypes.SMOKE, x, y, z, 3, 0.2, 0.25, 0.2, 0.02);
                SoundHelper.play(server, worldPosition, ModSounds.STRIKE_BAD, SoundEvents.ANVIL_BREAK,
                    SoundSource.BLOCKS, 0.8F, 0.9F);
            }
        }
        if (result.cracked()) {
            ParticleBudget.puff(server, ModParticles.METAL_DUST.get(), x, y, z, 6, 0.3, 0.2, 0.3, 0.05);
        }
        if (result.tooCold()) {
            HotMetal.feedback(player, "message.true_metallurgy.too_cold");
        } else if (result.tooHot()) {
            HotMetal.feedback(player, "message.true_metallurgy.too_hot");
        } else {
            HotMetal.feedback(player, result.grade().langKey());
        }
        PacketDistributor.sendToPlayersTrackingChunk(server, new net.minecraft.world.level.ChunkPos(worldPosition),
            new ModNetworking.StrikeFeedbackPayload(worldPosition, result.grade().ordinal(), result.cracked()));
    }

    private java.util.function.Supplier<net.minecraft.sounds.SoundEvent> hammerSound(HammerTier hammer) {
        return switch (hammer) {
            case STEEL, HARDENED -> ModSounds.HAMMER_STEEL;
            case MASTERWORK -> ModSounds.HAMMER_MASTER;
            default -> ModSounds.HAMMER_IRON;
        };
    }

    /** GUI kind selection. Only allowed before shaping starts. */
    public boolean selectKind(ServerPlayer player, String kind) {
        ForgedComponentData comp = items.getStackInSlot(0).get(ModDataComponents.COMPONENT.get());
        if (comp != null && comp.strikes() > 0) {
            HotMetal.feedback(player, "message.true_metallurgy.kind_locked");
            return false;
        }
        if (comp != null) {
            items.getStackInSlot(0).remove(ModDataComponents.COMPONENT.get());
        }
        session.setTargetKind(kind);
        setChanged();
        sync();
        return true;
    }

    public void dropContents(Level level, BlockPos pos) {
        ItemStack stack = items.getStackInSlot(0);
        if (!stack.isEmpty()) {
            net.minecraft.world.Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
        }
    }

    // ---------------- menu + persistence ----------------

    @Override
    public Component getDisplayName() {
        return Component.translatable("menu.true_metallurgy.forging");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new ForgingMenu(id, inventory, worldPosition, data);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Items", items.serializeNBT(registries));
        CompoundTag sessionTag = new CompoundTag();
        session.save(sessionTag);
        tag.put("Session", sessionTag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Items", Tag.TAG_COMPOUND)) {
            items.deserializeNBT(registries, tag.getCompound("Items"));
        }
        if (tag.contains("Session", Tag.TAG_COMPOUND)) {
            session.load(tag.getCompound("Session"));
        } else {
            session.retarget(net.minecraft.util.RandomSource.create());
        }
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

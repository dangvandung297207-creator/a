package com.truemetallurgy.entity;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

/**
 * The travelling blacksmith: teaches through trades, stocks metallurgy goods
 * and carries the odd legendary blueprint. A real {@link Merchant}, so the
 * vanilla trading screen, sounds and offer sync all behave.
 */
public class BlacksmithEntity extends PathfinderMob implements Merchant {
    @Nullable
    private Player tradingPlayer;
    @Nullable
    private MerchantOffers offers;
    private int villagerXp;

    public BlacksmithEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 30.0)
            .add(Attributes.MOVEMENT_SPEED, 0.5)
            .add(Attributes.FOLLOW_RANGE, 16.0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new PanicGoal(this, 0.7));
        goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.5));
        goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(6, new RandomLookAroundGoal(this));
    }

    @Override
    public void customServerAiStep() {
        super.customServerAiStep();
        // Stand still and face the customer while trading.
        if (tradingPlayer != null) {
            getNavigation().stop();
            getLookControl().setLookAt(tradingPlayer, 30.0F, 30.0F);
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand == InteractionHand.OFF_HAND) return InteractionResult.PASS;
        if (!level().isClientSide && tradingPlayer == null && player instanceof ServerPlayer serverPlayer) {
            setTradingPlayer(player);
            openTradingScreen(player, getDisplayName(), 1);
            serverPlayer.swing(hand, true);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
            MobSpawnType reason, @Nullable SpawnGroupData spawnData) {
        updateOffers();
        return super.finalizeSpawn(level, difficulty, reason, spawnData);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.VILLAGER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.VILLAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.VILLAGER_DEATH;
    }

    // ---------------- merchant ----------------

    private void updateOffers() {
        if (offers == null) {
            offers = BlacksmithTrades.createOffers(random);
        }
        for (MerchantOffer offer : offers) {
            offer.resetUses();
        }
    }

    @Override
    public void openTradingScreen(Player player, Component displayName, int level) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        updateOffers();
        serverPlayer.openMenu(new SimpleMenuProvider(
            (id, inv, p) -> new MerchantMenu(id, inv, this), displayName));
        serverPlayer.sendMerchantOffers(serverPlayer.containerMenu.containerId,
            getOffers(), 1, getVillagerXp(), showProgressBar(), canRestock());
    }

    @Override
    public void setTradingPlayer(@Nullable Player tradingPlayer) {
        this.tradingPlayer = tradingPlayer;
    }

    @Override
    @Nullable
    public Player getTradingPlayer() {
        return tradingPlayer;
    }

    @Override
    public MerchantOffers getOffers() {
        if (offers == null) {
            offers = BlacksmithTrades.createOffers(random);
        }
        return offers;
    }

    @Override
    public void overrideOffers(MerchantOffers offers) {
        this.offers = offers;
    }

    @Override
    public void notifyTrade(MerchantOffer offer) {
        offer.increaseUses();
        villagerXp += offer.getXp();
        level().playSound(null, blockPosition(), getNotifyTradeSound(), getSoundSource(), 1.0F, 1.0F);
        if (tradingPlayer instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendMerchantOffers(serverPlayer.containerMenu.containerId,
                getOffers(), 1, getVillagerXp(), showProgressBar(), canRestock());
        }
    }

    @Override
    public void notifyTradeUpdated(ItemStack stack) {
        if (level().isClientSide) return;
        level().playSound(null, blockPosition(),
            stack.isEmpty() ? SoundEvents.VILLAGER_NO : getNotifyTradeSound(),
            getSoundSource(), 0.8F, 1.0F);
    }

    @Override
    public int getVillagerXp() {
        return villagerXp;
    }

    @Override
    public void overrideXp(int xp) {
        this.villagerXp = xp;
    }

    @Override
    public boolean showProgressBar() {
        return false;
    }

    @Override
    public boolean canRestock() {
        return true;
    }

    @Override
    public SoundEvent getNotifyTradeSound() {
        return SoundEvents.VILLAGER_YES;
    }

    @Override
    public boolean isClientSide() {
        return level().isClientSide;
    }
}

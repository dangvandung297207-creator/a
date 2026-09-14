package com.masterblacksmith.entity;

import com.masterblacksmith.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.npc.Merchant;
import net.minecraft.world.entity.npc.MerchantOffer;
import net.minecraft.world.entity.npc.MerchantOffers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * A wandering smith who buys fuel and scrap, and sells tools, quenching
 * media and - if you are lucky - a legendary blueprint.
 */
public class TravellingBlacksmithEntity extends PathfinderMob implements Merchant {
    @Nullable
    private Player tradingPlayer;
    @Nullable
    private MerchantOffers offers;

    public TravellingBlacksmithEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.MOVEMENT_SPEED, 0.5)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new PanicGoal(this, 1.1));
        goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.6));
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (level().isClientSide) return InteractionResult.SUCCESS;
        if (getOffers().isEmpty()) buildOffers();
        player.openMenu(new SimpleMenuProvider(
                (id, inv, p) -> new MerchantMenu(id, inv, this),
                Component.translatable("entity.masterblacksmith.travelling_blacksmith")));
        return InteractionResult.CONSUME;
    }

    private void buildOffers() {
        MerchantOffers list = new MerchantOffers();
        // Smith buys fuel and scrap.
        list.add(new MerchantOffer(new ItemStack(Items.COAL, 12), new ItemStack(Items.EMERALD, 2), 8, 2, 0.05F));
        list.add(new MerchantOffer(new ItemStack(Items.RAW_IRON, 8), new ItemStack(Items.EMERALD, 3), 6, 3, 0.05F));
        // Smith sells consumables and tools.
        list.add(new MerchantOffer(new ItemStack(Items.EMERALD, 4), new ItemStack(ModItems.COKE.get(), 8), 8, 3, 0.05F));
        list.add(new MerchantOffer(new ItemStack(Items.EMERALD, 6), new ItemStack(ModItems.OIL_BUCKET.get()), 4, 4, 0.1F));
        list.add(new MerchantOffer(new ItemStack(Items.EMERALD, 9), new ItemStack(ModItems.TONGS.get()), 2, 5, 0.1F));
        list.add(new MerchantOffer(new ItemStack(Items.EMERALD, 14), new ItemStack(ModItems.HAMMER_STEEL.get()), 1, 8, 0.15F));
        list.add(new MerchantOffer(new ItemStack(Items.EMERALD, 20), new ItemStack(ModItems.HERBAL_OIL_BUCKET.get()), 1, 8, 0.15F));
        // The rare prize: one random blueprint.
        ItemStack[] prints = {
                new ItemStack(ModItems.BLUEPRINT_KINGS_EDGE.get()),
                new ItemStack(ModItems.BLUEPRINT_OATHKEEPER.get()),
                new ItemStack(ModItems.BLUEPRINT_STONESPLITTER.get()),
                new ItemStack(ModItems.BLUEPRINT_SKYPIERCER.get()),
                new ItemStack(ModItems.BLUEPRINT_AEGIS.get())
        };
        ItemStack pick = prints[random.nextInt(prints.length)];
        list.add(new MerchantOffer(new ItemStack(Items.EMERALD, 32), pick, 1, 20, 0.2F));
        overrideOffers(list);
    }

    // --- Merchant (kept annotation-free so extra helpers never break the build) ---

    public void setTradingPlayer(@Nullable Player player) {
        this.tradingPlayer = player;
    }

    public @Nullable Player getTradingPlayer() {
        return tradingPlayer;
    }

    public MerchantOffers getOffers() {
        if (offers == null) offers = new MerchantOffers();
        return offers;
    }

    public void overrideOffers(@Nullable MerchantOffers offers) {
        this.offers = offers;
    }

    public void notifyTrade(MerchantOffer offer) {
        offer.increaseUses();
    }

    public void notifyTradeUpdated(ItemStack stack) {
    }

    public int getVillagerXp() {
        return 0;
    }

    public void overrideXp(int xp) {
    }

    public boolean showProgressBar() {
        return false;
    }

    public SoundEvent getNotifyTradeSound() {
        return SoundEvents.VILLAGER_YES;
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
}

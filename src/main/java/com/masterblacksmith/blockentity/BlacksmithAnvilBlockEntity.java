package com.masterblacksmith.blockentity;

import com.masterblacksmith.MBSConfig;
import com.masterblacksmith.MasterBlacksmith;
import com.masterblacksmith.ModBlockEntities;
import com.masterblacksmith.ModItems;
import com.masterblacksmith.ModParticles;
import com.masterblacksmith.ModSounds;
import com.masterblacksmith.block.BlacksmithAnvilBlock;
import com.masterblacksmith.forging.ForgingData;
import com.masterblacksmith.forging.ForgingTemplate;
import com.masterblacksmith.forging.ForgingTemplates;
import com.masterblacksmith.forging.HammerStrikeHandler;
import com.masterblacksmith.forging.QualityCalculator;
import com.masterblacksmith.forging.StrikeResult;
import com.masterblacksmith.item.BloomItem;
import com.masterblacksmith.item.HammerTier;
import com.masterblacksmith.item.SmithingHammerItem;
import com.masterblacksmith.menu.AnvilForgingMenu;
import com.masterblacksmith.util.ParticleUtil;
import com.masterblacksmith.util.SoundUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Forging minigame host: cooling stock, strike verdicts, stage progression. */
public class BlacksmithAnvilBlockEntity extends BlockEntity implements net.minecraft.world.MenuProvider {
    private final ItemStackHandler inventory = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            sync();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return ForgingData.isWorkable(stack);
        }
    };
    private LazyOptional<ItemStackHandler> handler = LazyOptional.empty();
    private int tickCounter;
    private int syncCounter;

    private final ContainerData data = new ContainerData() {
        @Override public int get(int i) {
            ItemStack work = getWork();
            return switch (i) {
                case 0 -> hasWork() ? Math.round(ForgingData.getTemp(work)) : 0;
                case 1 -> hasWork() ? ForgingData.getStage(work) : 0;
                case 2 -> hasWork() ? ForgingData.getStrikes(work) : 0;
                case 3 -> strikesNeeded();
                case 4 -> hasWork() ? Math.round(QualityCalculator.componentScore(work)) : 0;
                case 5 -> hasWork() ? ForgingData.getReheats(work) : 0;
                default -> 0;
            };
        }
        @Override public void set(int i, int v) {}
        @Override public int getCount() { return 6; }
    };

    public BlacksmithAnvilBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ANVIL.get(), pos, state);
    }

    public ItemStackHandler getInventory() { return inventory; }

    public boolean hasWork() { return !inventory.getStackInSlot(0).isEmpty(); }
    public ItemStack getWork() { return inventory.getStackInSlot(0); }
    public void setWork(ItemStack stack) { inventory.setStackInSlot(0, stack); }
    public ItemStack takeWork() {
        ItemStack out = inventory.getStackInSlot(0).copy();
        inventory.setStackInSlot(0, ItemStack.EMPTY);
        return out;
    }

    public double tolerance() {
        if (getBlockState().getBlock() instanceof BlacksmithAnvilBlock a) return a.tolerance();
        return 1.0;
    }

    public int qualityCap() {
        if (getBlockState().getBlock() instanceof BlacksmithAnvilBlock a) return a.qualityCap();
        return 75;
    }

    public int strikesNeeded() {
        if (!hasWork()) return 0;
        String templateId = ForgingData.getTemplateId(getWork());
        if (templateId == null || templateId.isEmpty()) return 6;
        ForgingTemplate t = ForgingTemplates.get(templateId);
        int stage = Math.min(ForgingData.getStage(getWork()), t.stageCount() - 1);
        return t.getStages().get(stage).requiredStrikes();
    }

    public SimpleContainer inventoryView() {
        SimpleContainer c = new SimpleContainer(1);
        c.setItem(0, inventory.getStackInSlot(0).copy());
        return c;
    }

    /** In-world strike (left-click): aim comes from attack cooldown discipline. */
    public void worldStrike(ServerPlayer player) {
        if (!hasWork()) return;
        float strength = player.getAttackStrengthScale(0.5F);
        float aim = 0.5F + (player.getRandom().nextFloat() - 0.5F) * (1.15F - strength * 0.8F);
        float force = 0.45F + strength * 0.45F;
        serverStrike(player, aim, force);
    }

    /** Menu-driven strike from the timing bar packet. */
    public void serverStrike(ServerPlayer player, float aim, float force) {
        if (level == null || level.isClientSide || !hasWork()) return;
        if (player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) > 36) return;
        ItemStack held = player.getMainHandItem();
        if (!(held.getItem() instanceof SmithingHammerItem hammerItem)) return;
        HammerTier tier = hammerItem.getTier();

        ItemStack work = getWork();
        HammerStrikeHandler.Outcome outcome = HammerStrikeHandler.strike(
                player, work, tier, tolerance(), aim, force);

        held.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(EquipmentSlot.MAINHAND));

        if (outcome.ruined()) {
            inventory.setStackInSlot(0, new ItemStack(ModItems.SLAG.get()));
            ForgingData.addHistory(getWork(), "ruined");
        } else if (outcome.templateComplete()) {
            finishWorkpiece(work);
        }
        strikeFeedback(player, outcome);
        updateHotState();
        setChanged();
        sync();
    }

    /** Begin shaping: pick a template for a hot billet (menu buttons). */
    public boolean chooseTemplate(String templateId) {
        if (!hasWork() || !ForgingTemplates.exists(templateId)) return false;
        ItemStack work = getWork();
        if (!ForgingData.getTemplateId(work).isEmpty()) return false;
        if (work.getItem() instanceof BloomItem) return false;
        String blankId = switch (templateId) {
            case "sword" -> "blade_blank";
            case "axe" -> "axe_head_blank";
            case "pickaxe" -> "pick_head_blank";
            case "spear" -> "spear_head_blank";
            case "plate" -> "armor_plate_blank";
            default -> "";
        };
        var item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(MasterBlacksmith.MOD_ID, blankId));
        if (item == null) return false;
        ItemStack blank = new ItemStack(item);
        String metal = ForgingData.getMetalId(work);
        ForgingData.initBlank(blank, templateId, templateId, ForgingData.getPurity(work), ForgingData.getReheats(work));
        ForgingData.setMetal(blank, metal);
        ForgingData.setTemp(blank, ForgingData.getTemp(work));
        ForgingData.setAnvilCap(blank, qualityCap());
        for (String line : ForgingData.getHistory(work)) ForgingData.addHistory(blank, line);
        ForgingData.addHistory(blank, "template." + templateId);
        inventory.setStackInSlot(0, blank);
        setChanged();
        sync();
        return true;
    }

    private void finishWorkpiece(ItemStack work) {
        String metal = ForgingData.getMetalId(work);
        String templateId = ForgingData.getTemplateId(work);
        String outId;
        if (templateId == null || templateId.isEmpty()) {
            outId = "billet_" + metal; // bloom consolidation
        } else {
            outId = ForgingTemplates.get(templateId).getFinishedItem();
        }
        var item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(MasterBlacksmith.MOD_ID, outId));
        if (item == null) return;
        ItemStack finished = new ItemStack(item);
        float forgingQ = QualityCalculator.componentScore(work);
        if (templateId == null || templateId.isEmpty()) {
            ForgingData.initBillet(finished, metal);
            ForgingData.setPurity(finished, Math.min(100F, ForgingData.getPurity(work) + 2F));
        } else {
            ForgingData.initBlank(finished, metal, templateId, ForgingData.getPurity(work), ForgingData.getReheats(work));
            ForgingData.setMetal(finished, metal);
            ForgingData.setStage(finished, ForgingTemplates.get(templateId).stageCount());
            ForgingData.setForgingQ(finished, forgingQ);
            ForgingData.setAnvilCap(finished, qualityCap());
        }
        ForgingData.setTemp(finished, ForgingData.getTemp(work));
        for (String line : ForgingData.getHistory(work)) ForgingData.addHistory(finished, line);
        ForgingData.addHistory(finished, "shaped." + (templateId == null || templateId.isEmpty() ? "billet" : templateId));
        inventory.setStackInSlot(0, finished);
    }

    private void strikeFeedback(ServerPlayer player, HammerStrikeHandler.Outcome outcome) {
        if (!(level instanceof ServerLevel sl)) return;
        double x = worldPosition.getX() + 0.5;
        double y = worldPosition.getY() + 0.9;
        double z = worldPosition.getZ() + 0.5;
        ItemStack held = player.getMainHandItem();
        HammerTier tier = held.getItem() instanceof SmithingHammerItem h ? h.getTier() : HammerTier.IRON;

        switch (outcome.result()) {
            case PERFECT -> {
                ParticleUtil.burst(sl, ModParticles.FORGE_SPARK.get(), x, y, z, 16, 0.3, 0.35);
                ParticleUtil.burst(sl, ParticleTypes.FLASH, x, y, z, 1, 0.1, 0.0);
                SoundUtil.play(level, worldPosition, tier.hitSound(), net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.15F, 0.04F);
                SoundUtil.play(level, worldPosition, ModSounds.ANVIL_RING.get(), net.minecraft.sounds.SoundSource.BLOCKS, 0.8F, 1.2F, 0.05F);
            }
            case GOOD -> {
                ParticleUtil.burst(sl, ModParticles.FORGE_SPARK.get(), x, y, z, 7, 0.25, 0.25);
                SoundUtil.play(level, worldPosition, tier.hitSound(), net.minecraft.sounds.SoundSource.BLOCKS, 0.9F, 1.0F, 0.06F);
                SoundUtil.play(level, worldPosition, SoundEvents.ANVIL_USE, net.minecraft.sounds.SoundSource.BLOCKS, 0.5F, 1.1F, 0.08F);
            }
            case MISS -> {
                ParticleUtil.burst(sl, ParticleTypes.SMOKE, x, y, z, 3, 0.15, 0.05);
                SoundUtil.play(level, worldPosition, ModSounds.HAMMER_MISS.get(), net.minecraft.sounds.SoundSource.BLOCKS, 0.8F, 1.0F, 0.08F);
            }
            case BAD -> {
                ParticleUtil.burst(sl, ParticleTypes.SMOKE, x, y, z, 5, 0.2, 0.06);
                ParticleUtil.burst(sl, ParticleTypes.CRIT, x, y, z, 4, 0.2, 0.2);
                SoundUtil.play(level, worldPosition, ModSounds.HAMMER_CRACK.get(), net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 0.9F, 0.1F);
            }
        }
        player.displayClientMessage(outcome.message(), true);
    }

    private void updateHotState() {
        if (level == null) return;
        boolean hot = hasWork() && ForgingData.getTemp(getWork()) > 400;
        BlockState state = getBlockState();
        if (state.getValue(BlacksmithAnvilBlock.HOT) != hot) {
            level.setBlock(worldPosition, state.setValue(BlacksmithAnvilBlock.HOT, hot), 3);
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlacksmithAnvilBlockEntity be) {
        int interval = Math.max(1, MBSConfig.TEMP_TICK_INTERVAL.get());
        if (++be.tickCounter < interval) return;
        float dt = interval / 20F;
        be.tickCounter = 0;
        if (be.hasWork()) {
            ItemStack work = be.getWork();
            float before = ForgingData.getTemp(work);
            if (before > ForgingData.ROOM_TEMP) {
                ForgingData.cool(work, (float) (MBSConfig.BILLET_COOLING_RATE.get() * dt));
                be.setChanged();
            }
            be.updateHotState();
            if (++be.syncCounter >= 20) {
                be.syncCounter = 0;
                be.sync();
            }
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, BlacksmithAnvilBlockEntity be) {
    }

    public void sync() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", inventory.serializeNBT());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        inventory.deserializeNBT(tag.getCompound("Inventory"));
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        load(pkt.getTag());
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("menu.masterblacksmith.anvil");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new AnvilForgingMenu(id, inv, this, data);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        handler = LazyOptional.of(() -> inventory);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) return handler.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        handler.invalidate();
    }
}

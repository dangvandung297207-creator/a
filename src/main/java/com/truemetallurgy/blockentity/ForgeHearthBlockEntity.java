package com.truemetallurgy.blockentity;

import com.truemetallurgy.block.ForgeHearthBlock;
import com.truemetallurgy.block.ForgeTier;
import com.truemetallurgy.components.BilletData;
import com.truemetallurgy.components.ForgedComponentData;
import com.truemetallurgy.config.TMConfig;
import com.truemetallurgy.forging.HotMetal;
import com.truemetallurgy.item.BlacksmithTongsItem;
import com.truemetallurgy.menu.ForgeHearthMenu;
import com.truemetallurgy.metallurgy.Heat;
import com.truemetallurgy.metallurgy.Material;
import com.truemetallurgy.metallurgy.Materials;
import com.truemetallurgy.recipe.AlloyRecipe;
import com.truemetallurgy.registry.ModBlockEntities;
import com.truemetallurgy.registry.ModItems;
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
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Forge simulation. Ticks on a coarse interval (config): fuel burn, chamber
 * temperature, workpiece heating, alloy smelting. All state persists and
 * syncs; visuals mirror through the HEAT blockstate.
 */
public class ForgeHearthBlockEntity extends BlockEntity implements MenuProvider {
    public static final int SLOT_FUEL = 0;
    public static final int SLOT_WORK = 1;
    public static final int SLOT_ALLOY_A = 2;
    public static final int SLOT_ALLOY_B = 3;
    public static final int SLOT_OUT = 4;

    private final ItemStackHandler items = new ItemStackHandler(5) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            sync();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return switch (slot) {
                case SLOT_FUEL -> fuelValue(stack) > 0;
                case SLOT_WORK -> HotMetal.isWorkpiece(stack);
                case SLOT_OUT -> false;
                default -> true;
            };
        }
    };

    private int temperature = Heat.ROOM_TEMP;
    private int fuel;
    private int maxFuel = 1;
    private int airflow;
    private int alloyProgress;

    private final ContainerData data = new ContainerData() {
        @Override public int get(int i) {
            return switch (i) {
                case 0 -> temperature;
                case 1 -> fuel;
                case 2 -> maxFuel;
                case 3 -> airflow;
                case 4 -> alloyProgress;
                case 5 -> alloyTime();
                case 6 -> Heat.ForgeState.fromTemp(temperature).ordinal();
                case 7 -> tier().maxTemp;
                default -> 0;
            };
        }
        @Override public void set(int i, int v) {}
        @Override public int getCount() { return 8; }
    };

    public ForgeHearthBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FORGE.get(), pos, state);
    }

    public ForgeTier tier() {
        if (getBlockState().getBlock() instanceof ForgeHearthBlock forge) return forge.getTier();
        return ForgeTier.PRIMITIVE;
    }

    public ItemStackHandler getItemHandler() {
        return items;
    }

    // ---------------- simulation ----------------

    public static void tick(Level level, BlockPos pos, BlockState state, ForgeHearthBlockEntity be) {
        if (level.isClientSide) return;
        int interval = TMConfig.FORGE_TICK_INTERVAL.get();
        if (level.getGameTime() % Math.max(1, interval) != 0) return;
        be.simulate((ServerLevel) level, interval);
    }

    private void simulate(ServerLevel level, int interval) {
        ForgeTier tier = tier();
        double heatMult;
        try {
            heatMult = TMConfig.HEATING_RATE_MULT.get();
        } catch (IllegalStateException e) {
            heatMult = 1.0;
        }

        if (tier.autoAirflow) {
            airflow = Math.max(airflow, 45);
        } else {
            airflow = Math.max(0, airflow - interval);
        }

        boolean burning = fuel > 0;
        if (burning) {
            fuel = Math.max(0, fuel - interval);
            float rate = (float) (6.0 * tier.heatingMult * heatMult * (1.0 + airflow / 120.0));
            temperature = Math.min(tier.maxTemp, temperature + Math.max(1, Math.round(rate * interval / 4.0F)));
            if (level.random.nextInt(6) == 0) {
                ParticleBudget.puff(level, ParticleTypes.FLAME,
                    worldPosition.getX() + 0.5, worldPosition.getY() + 0.6, worldPosition.getZ() + 0.5,
                    1, 0.25, 0.15, 0.25, 0.01);
            }
        } else {
            if (!consumeFuel(tier)) {
                int cool = tier.tempHold ? 1 : 2;
                temperature = Math.max(Heat.ROOM_TEMP, temperature - cool * interval);
            }
        }

        heatWorkpiece(level, interval);
        smeltAlloy(level, interval);

        int heat = Heat.ForgeState.fromTemp(temperature).ordinal();
        if (getBlockState().getValue(ForgeHearthBlock.HEAT) != heat) {
            level.setBlock(worldPosition, getBlockState().setValue(ForgeHearthBlock.HEAT, heat), 3);
        }
        setChanged();
    }

    private boolean consumeFuel(ForgeTier tier) {
        ItemStack stack = items.getStackInSlot(SLOT_FUEL);
        if (stack.isEmpty()) return false;
        int value = Math.round(fuelValue(stack) * tier.fuelMult);
        if (value <= 0) return false;
        fuel = value;
        maxFuel = value;
        if (stack.is(Items.LAVA_BUCKET)) {
            items.setStackInSlot(SLOT_FUEL, new ItemStack(Items.BUCKET));
        } else {
            stack.shrink(1);
        }
        setChanged();
        return true;
    }

    private void heatWorkpiece(ServerLevel level, int interval) {
        ItemStack work = items.getStackInSlot(SLOT_WORK);
        if (work.isEmpty() || !HotMetal.isWorkpiece(work)) return;
        int current = HotMetal.temperatureOf(work);
        if (current < 0) {
            String mat = com.truemetallurgy.forging.ForgingLogic.bloomMaterial(work);
            if (mat == null && !(work.getItem() instanceof com.truemetallurgy.item.BilletItem)) return;
            String id = mat != null ? mat : ((com.truemetallurgy.item.BilletItem) work.getItem()).getMaterial().id();
            work.set(com.truemetallurgy.registry.ModDataComponents.BILLET.get(),
                new BilletData(id, Heat.ROOM_TEMP, Materials.get(id).purity(), 0));
            current = Heat.ROOM_TEMP;
        }
        int delta = temperature - current;
        int step = Math.max(-30 * interval / 4, Math.min(45 * interval / 4, delta / 4));
        int next = current + Math.max(-60, Math.min(60, step));
        com.truemetallurgy.forging.ForgingLogic.setWorkpieceTemp(work, next);

        // Overheating destroys: oxidation first, then failure past melting point.
        BilletData billet = work.get(com.truemetallurgy.registry.ModDataComponents.BILLET.get());
        if (billet != null) {
            Material mat = Materials.get(billet.materialId());
            if (next > mat.meltingTemp()) {
                float loss = 2.0F * interval / mat.heatResistance();
                work.set(com.truemetallurgy.registry.ModDataComponents.BILLET.get(),
                    new BilletData(billet.materialId(), next, Math.max(0, billet.purity() - loss), billet.reheats()));
                if (next > mat.meltingTemp() + 150) {
                    items.setStackInSlot(SLOT_WORK, ItemStack.EMPTY);
                    ParticleBudget.puff(level, ParticleTypes.SMOKE,
                        worldPosition.getX() + 0.5, worldPosition.getY() + 1.0, worldPosition.getZ() + 0.5,
                        8, 0.3, 0.3, 0.3, 0.02);
                }
            }
        }
        setChanged();
    }

    private void smeltAlloy(ServerLevel level, int interval) {
        Optional<RecipeHolder<AlloyRecipe>> match = level.getRecipeManager()
            .getRecipeFor(com.truemetallurgy.registry.ModRecipeTypes.ALLOY.get(),
                new AlloyRecipe.Input(items.getStackInSlot(SLOT_ALLOY_A), items.getStackInSlot(SLOT_ALLOY_B)), level);
        if (match.isEmpty()) {
            if (alloyProgress != 0) {
                alloyProgress = 0;
                setChanged();
            }
            return;
        }
        AlloyRecipe recipe = match.get().value();
        if (temperature < recipe.minTemp()) {
            if (alloyProgress != 0) {
                alloyProgress = 0;
                setChanged();
            }
            return;
        }
        ItemStack result = recipe.getResultItem(level.registryAccess());
        ItemStack out = items.getStackInSlot(SLOT_OUT);
        if (!out.isEmpty() && (!ItemStack.isSameItemSameComponents(out, result) || out.getCount() + result.getCount() > out.getMaxStackSize())) {
            return;
        }
        alloyProgress += interval;
        if (alloyProgress >= recipe.time()) {
            alloyProgress = 0;
            items.getStackInSlot(SLOT_ALLOY_A).shrink(1);
            items.getStackInSlot(SLOT_ALLOY_B).shrink(1);
            if (out.isEmpty()) {
                items.setStackInSlot(SLOT_OUT, result.copy());
            } else {
                out.grow(result.getCount());
            }
            SoundHelper.play(level, worldPosition, ModSounds.FORGE_ROAR, SoundEvents.FIRECHARGE_USE,
                SoundSource.BLOCKS, 0.7F, 0.9F);
        }
        setChanged();
    }

    private int alloyTime() {
        if (level instanceof ServerLevel server) {
            Optional<RecipeHolder<AlloyRecipe>> match = server.getRecipeManager()
                .getRecipeFor(com.truemetallurgy.registry.ModRecipeTypes.ALLOY.get(),
                    new AlloyRecipe.Input(items.getStackInSlot(SLOT_ALLOY_A), items.getStackInSlot(SLOT_ALLOY_B)), server);
            if (match.isPresent()) return match.get().value().time();
        }
        return 200;
    }

    // ---------------- interaction ----------------

    /** Fuel values for forge + furnace parity. Coke is the prize fuel. */
    public static int fuelValue(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        if (stack.is(ModItems.COKE.get())) return 3200;
        if (stack.is(Items.COAL) || stack.is(Items.CHARCOAL)) return 1600;
        if (stack.is(Items.COAL_BLOCK)) return 16000;
        if (stack.is(Items.BLAZE_ROD)) return 2400;
        if (stack.is(Items.LAVA_BUCKET)) return 20000;
        if (stack.is(Items.DRIED_KELP_BLOCK)) return 4000;
        if (stack.is(Items.STICK)) return 100;
        if (stack.is(Items.BAMBOO)) return 50;
        if (stack.is(ItemTags.LOGS_THAT_BURN)) return 300;
        if (stack.is(ItemTags.PLANKS)) return 300;
        try {
            return Math.max(0, stack.getItem().getBurnTime(stack, null));
        } catch (Exception e) {
            return 0;
        }
    }

    public boolean tryInsertFromHand(Player player, InteractionHand hand, ItemStack stack) {
        if (!(player instanceof ServerPlayer serverPlayer)) return false;
        // Tongs in hand, other hand empty: extract the workpiece.
        if (stack.getItem() instanceof BlacksmithTongsItem) {
            ItemStack other = player.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
            if (other.isEmpty()) {
                return extractWorkpiece(serverPlayer);
            }
        }
        ItemStack work = HotMetal.extractWorkpiece(serverPlayer, hand, stack);
        if (!work.isEmpty()) {
            return insertWorkpiece(serverPlayer, work);
        }
        if (fuelValue(stack) > 0) {
            ItemStack rest = items.insertItem(SLOT_FUEL, stack.copy(), false);
            int moved = stack.getCount() - rest.getCount();
            if (moved > 0) {
                stack.shrink(moved);
                SoundHelper.play(player.level(), worldPosition, ModSounds.FORGE_CRACKLE, SoundEvents.FURNACE_FIRE_CRACKLE,
                    SoundSource.BLOCKS, 0.6F, 1.0F);
                return true;
            }
            return true;
        }
        if (stack.isEmpty()) return false;
        if (items.getStackInSlot(SLOT_ALLOY_A).isEmpty()) {
            items.setStackInSlot(SLOT_ALLOY_A, stack.split(1));
            return true;
        }
        if (items.getStackInSlot(SLOT_ALLOY_B).isEmpty()) {
            items.setStackInSlot(SLOT_ALLOY_B, stack.split(1));
            return true;
        }
        if (serverPlayer instanceof ServerPlayer sp) {
            HotMetal.feedback(sp, "message.true_metallurgy.forge_full");
        }
        return true;
    }

    private boolean insertWorkpiece(ServerPlayer player, ItemStack work) {
        if (!items.getStackInSlot(SLOT_WORK).isEmpty()) {
            HotMetal.feedback(player, "message.true_metallurgy.forge_occupied");
            return true;
        }
        ItemStack one = work.copyWithCount(1);
        // Reheating a shaped workpiece: oxidation trade-off.
        ForgedComponentData comp = one.get(com.truemetallurgy.registry.ModDataComponents.COMPONENT.get());
        BilletData billet = one.get(com.truemetallurgy.registry.ModDataComponents.BILLET.get());
        if (comp != null) {
            ForgedComponentData next = new ForgedComponentData(
                comp.materialId(), comp.kind(), comp.stage(), comp.stageProgress(), comp.strikes(),
                comp.perfects(), comp.goods(), comp.misses(), comp.bads(), comp.reheats() + 1,
                Math.max(0, comp.purity() - 1.5F), comp.score(), comp.temperature(),
                comp.quenched(), comp.quenchId(), comp.grindAngle(), comp.grindQuality(),
                comp.crafterName(), comp.crafterId());
            one.set(com.truemetallurgy.registry.ModDataComponents.COMPONENT.get(), next);
        }
        if (billet != null && comp != null) {
            one.set(com.truemetallurgy.registry.ModDataComponents.BILLET.get(), billet.reheated(1.5F));
        }
        items.setStackInSlot(SLOT_WORK, one);
        work.shrink(1);
        SoundHelper.play(player.level(), worldPosition, ModSounds.TONGS_CLINK, SoundEvents.ANVIL_LAND,
            SoundSource.BLOCKS, 0.5F, 1.4F);
        return true;
    }

    private boolean extractWorkpiece(ServerPlayer player) {
        ItemStack work = items.getStackInSlot(SLOT_WORK);
        if (work.isEmpty()) return false;
        items.setStackInSlot(SLOT_WORK, ItemStack.EMPTY);
        if (!player.getInventory().add(work)) {
            player.drop(work, false);
        }
        SoundHelper.play(player.level(), worldPosition, ModSounds.TONGS_CLINK, SoundEvents.ANVIL_LAND,
            SoundSource.BLOCKS, 0.5F, 1.4F);
        return true;
    }

    public void addAirflow(int amount) {
        airflow = Math.min(100, airflow + amount);
        setChanged();
        sync();
    }

    public int temperature() { return temperature; }
    public int airflow() { return airflow; }

    public void dropContents(Level level, BlockPos pos) {
        for (int i = 0; i < items.getSlots(); i++) {
            ItemStack stack = items.getStackInSlot(i);
            if (!stack.isEmpty()) {
                net.minecraft.world.Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
            }
        }
    }

    // ---------------- menu + persistence ----------------

    @Override
    public Component getDisplayName() {
        return Component.translatable("menu.true_metallurgy.forge");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new ForgeHearthMenu(id, inventory, worldPosition, data);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Items", items.serializeNBT(registries));
        tag.putInt("Temperature", temperature);
        tag.putInt("Fuel", fuel);
        tag.putInt("MaxFuel", maxFuel);
        tag.putInt("Airflow", airflow);
        tag.putInt("AlloyProgress", alloyProgress);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Items", Tag.TAG_COMPOUND)) {
            items.deserializeNBT(registries, tag.getCompound("Items"));
        }
        temperature = tag.getInt("Temperature");
        fuel = tag.getInt("Fuel");
        maxFuel = Math.max(1, tag.getInt("MaxFuel"));
        airflow = tag.getInt("Airflow");
        alloyProgress = tag.getInt("AlloyProgress");
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

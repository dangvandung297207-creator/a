package com.truemetallurgy.blockentity;

import com.truemetallurgy.assembly.AssemblyLogic;
import com.truemetallurgy.forging.HotMetal;
import com.truemetallurgy.item.BlueprintItem;
import com.truemetallurgy.item.ForgedComponentItem;
import com.truemetallurgy.item.HandleItem;
import com.truemetallurgy.menu.AssemblyMenu;
import com.truemetallurgy.recipe.AssemblyRecipe;
import com.truemetallurgy.registry.ModBlockEntities;
import com.truemetallurgy.registry.ModItems;
import com.truemetallurgy.registry.ModRecipeTypes;
import com.truemetallurgy.registry.ModSounds;
import com.truemetallurgy.util.SoundHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
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
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Assembly table. Recipes are data-driven; the finished identity is computed
 * by {@link AssemblyLogic} on the server when the craft button is pressed.
 */
public class AssemblyTableBlockEntity extends BlockEntity implements MenuProvider {
    public static final int SLOT_BLADE = 0;
    public static final int SLOT_B = 1;
    public static final int SLOT_C = 2;
    public static final int SLOT_D = 3;
    public static final int SLOT_BLUEPRINT = 4;
    public static final int SLOT_OUT = 5;

    private final ItemStackHandler items = new ItemStackHandler(6) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            sync();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return switch (slot) {
                case SLOT_BLADE -> stack.getItem() instanceof ForgedComponentItem;
                case SLOT_B -> stack.getItem() instanceof HandleItem
                    || stack.is(ModItems.IRON_GUARD.get()) || stack.is(ModItems.STEEL_GUARD.get())
                    || stack.is(ModItems.LONG_SHAFT.get());
                case SLOT_C -> stack.getItem() instanceof HandleItem
                    || stack.is(ModItems.BINDING.get());
                case SLOT_D -> stack.is(ModItems.IRON_POMMEL.get()) || stack.is(ModItems.STEEL_POMMEL.get());
                case SLOT_BLUEPRINT -> stack.getItem() instanceof BlueprintItem;
                default -> false;
            };
        }
    };

    private final ContainerData data = new ContainerData() {
        @Override public int get(int i) {
            return switch (i) {
                case AssemblyMenu.D_CAN -> canCraftPreview() ? 1 : 0;
                case AssemblyMenu.D_CATEGORY -> categoryOrdinal();
                default -> 0;
            };
        }
        @Override public void set(int i, int v) {}
        @Override public int getCount() { return AssemblyMenu.DATA_COUNT; }
    };

    public AssemblyTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ASSEMBLY_TABLE.get(), pos, state);
    }

    public ItemStackHandler getItemHandler() {
        return items;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, AssemblyTableBlockEntity be) {
        // No continuous simulation; crafting is event-driven. Ticker exists so
        // future automation (master workshop) can hook in without rework.
    }

    private AssemblyRecipe.Input input() {
        return new AssemblyRecipe.Input(
            items.getStackInSlot(SLOT_BLADE), items.getStackInSlot(SLOT_B),
            items.getStackInSlot(SLOT_C), items.getStackInSlot(SLOT_D),
            items.getStackInSlot(SLOT_BLUEPRINT));
    }

    private Optional<RecipeHolder<AssemblyRecipe>> match(ServerLevel server) {
        return server.getRecipeManager().getRecipeFor(ModRecipeTypes.ASSEMBLY.get(), input(), server);
    }

    private boolean canCraftPreview() {
        if (!(level instanceof ServerLevel server)) return false;
        if (!items.getStackInSlot(SLOT_OUT).isEmpty()) return false;
        return match(server).isPresent();
    }

    private int categoryOrdinal() {
        if (!(level instanceof ServerLevel server)) return 0;
        return match(server).map(h -> h.value().categoryIndex()).orElse(0);
    }

    /** GUI craft button. Fully server-validated, never loses components. */
    public boolean craft(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel server)) return false;
        if (!items.getStackInSlot(SLOT_OUT).isEmpty()) {
            HotMetal.feedback(player, "message.true_metallurgy.output_occupied");
            return false;
        }
        Optional<RecipeHolder<AssemblyRecipe>> match = match(server);
        if (match.isEmpty()) {
            HotMetal.feedback(player, "message.true_metallurgy.assembly_no_recipe");
            return false;
        }
        AssemblyRecipe recipe = match.get().value();
        AssemblyLogic.Result result = recipe.assembleLogic(input(), player);
        if (result.output().isEmpty()) {
            HotMetal.feedback(player, result.messageKey());
            return false;
        }
        items.getStackInSlot(SLOT_BLADE).shrink(1);
        items.getStackInSlot(SLOT_B).shrink(1);
        items.getStackInSlot(SLOT_C).shrink(1);
        items.getStackInSlot(SLOT_D).shrink(1);
        if (result.legendary()) {
            items.getStackInSlot(SLOT_BLUEPRINT).shrink(1);
        }
        items.setStackInSlot(SLOT_OUT, result.output());

        SoundHelper.play(server, worldPosition, ModSounds.ASSEMBLY_WOOD, SoundEvents.WOOD_BREAK,
            SoundSource.BLOCKS, 0.7F, 1.1F);
        SoundHelper.play(server, worldPosition, ModSounds.ASSEMBLY_METAL, SoundEvents.ANVIL_USE,
            SoundSource.BLOCKS, 0.7F, 1.2F);
        if (result.legendary()) {
            SoundHelper.play(server, worldPosition, ModSounds.MASTERWORK_CHIME, SoundEvents.PLAYER_LEVELUP,
                SoundSource.BLOCKS, 1.0F, 1.3F);
        }
        HotMetal.feedback(player, result.messageKey());
        setChanged();
        sync();
        return true;
    }

    public void dropContents(Level level, BlockPos pos) {
        for (int i = 0; i < items.getSlots(); i++) {
            ItemStack stack = items.getStackInSlot(i);
            if (!stack.isEmpty()) {
                net.minecraft.world.Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
            }
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("menu.true_metallurgy.assembly");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new AssemblyMenu(id, inventory, worldPosition, data);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Items", items.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Items", Tag.TAG_COMPOUND)) {
            items.deserializeNBT(registries, tag.getCompound("Items"));
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

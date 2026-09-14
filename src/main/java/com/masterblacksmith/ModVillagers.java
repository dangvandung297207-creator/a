package com.masterblacksmith;

import com.google.common.collect.ImmutableSet;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Set;
import java.util.stream.Collectors;

/** Resident master-smith profession bound to the steel/master anvil workstation. */
public class ModVillagers {
    public static final DeferredRegister<PoiType> POI_TYPES =
            DeferredRegister.create(ForgeRegistries.POI_TYPES, MasterBlacksmith.MOD_ID);
    public static final DeferredRegister<VillagerProfession> PROFESSIONS =
            DeferredRegister.create(ForgeRegistries.VILLAGER_PROFESSIONS, MasterBlacksmith.MOD_ID);

    public static final RegistryObject<PoiType> MASTER_SMITH_POI =
            POI_TYPES.register("master_smith", () -> new PoiType(statesOf(
                    ModBlocks.ANVIL_STEEL.get(), ModBlocks.ANVIL_MASTER.get()), 1, 1));

    public static final RegistryObject<VillagerProfession> MASTER_SMITH =
            PROFESSIONS.register("master_smith", () -> new VillagerProfession("master_smith",
                    holder -> holder.get() == MASTER_SMITH_POI.get(),
                    holder -> holder.get() == MASTER_SMITH_POI.get(),
                    ImmutableSet.of(), ImmutableSet.of(), SoundEvents.VILLAGER_WORK_TOOLSMITH));

    private static Set<BlockState> statesOf(Block... blocks) {
        Set<BlockState> out = new java.util.HashSet<>();
        for (Block b : blocks) {
            out.addAll(b.getStateDefinition().getPossibleStates());
        }
        return out.stream().collect(Collectors.toSet());
    }
}

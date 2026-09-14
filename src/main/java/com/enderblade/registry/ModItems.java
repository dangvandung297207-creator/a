package com.enderblade.registry;

import com.enderblade.EnderBladeMod;
import com.enderblade.item.EnderBladeItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(EnderBladeMod.MOD_ID);

    public static final ResourceLocation REACH_ID =
            ResourceLocation.fromNamespaceAndPath(EnderBladeMod.MOD_ID, "ender_blade_reach");

    /**
     * +1.0 ENTITY_INTERACTION_RANGE → 4-block melee reach.
     * Netherite stats: displayed 8.0 damage, 1.6 attack speed, 2031 durability.
     */
    public static final DeferredItem<EnderBladeItem> ENDER_BLADE = ITEMS.registerItem(
            "ender_blade",
            props -> new EnderBladeItem(Tiers.NETHERITE, props),
            new Item.Properties()
                    .fireResistant()
                    .rarity(Rarity.EPIC)
                    .attributes(
                            SwordItem.createAttributes(Tiers.NETHERITE, 3, -2.4f)
                                    .withModifierAdded(
                                            Attributes.ENTITY_INTERACTION_RANGE,
                                            new AttributeModifier(REACH_ID, 1.0D, AttributeModifier.Operation.ADD_VALUE),
                                            EquipmentSlotGroup.MAINHAND
                                    )
                    )
    );

    private ModItems() {
    }
}

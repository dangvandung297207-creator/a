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

/**
 * Item registry for the Ender Blade mod.
 */
public final class ModItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(EnderBladeMod.MOD_ID);

    /**
     * Unique modifier id for the +1.0 entity interaction range bonus.
     */
    public static final ResourceLocation ENDER_BLADE_REACH_ID =
            ResourceLocation.fromNamespaceAndPath(EnderBladeMod.MOD_ID, "ender_blade_reach");

    /**
     * Ender Blade — netherite-tier void sword.
     * <ul>
     *   <li>Base attack damage: 8.0 (netherite sword baseline via bonus 3 + tier 4)</li>
     *   <li>Attack speed: 1.6 ({@code -2.4f} against the player base of 4.0)</li>
     *   <li>Durability: 2031 (from {@link Tiers#NETHERITE})</li>
     *   <li>Entity interaction range: +1.0 (3 → 4 blocks)</li>
     * </ul>
     * <p>
     * Uses {@link DeferredRegister.Items#registerItem} so the item id is applied
     * to {@link Item.Properties} automatically (required since 1.21.1).
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
                                            new AttributeModifier(
                                                    ENDER_BLADE_REACH_ID,
                                                    1.0D,
                                                    AttributeModifier.Operation.ADD_VALUE
                                            ),
                                            EquipmentSlotGroup.MAINHAND
                                    )
                    )
    );

    private ModItems() {
    }
}

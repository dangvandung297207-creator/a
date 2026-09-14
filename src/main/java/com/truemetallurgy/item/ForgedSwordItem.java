package com.truemetallurgy.item;

import com.truemetallurgy.metallurgy.Material;
import com.truemetallurgy.network.ModNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

/** Finished sword (also the base for spears). Sneak-use inspects it in the journal. */
public class ForgedSwordItem extends SwordItem {
    protected final Material material;
    protected final String toolKind;

    public ForgedSwordItem(Material material) {
        this(material, "sword");
    }

    protected ForgedSwordItem(Material material, String toolKind) {
        super(ForgedToolHelper.vanillaTier(material), makeProps(material, toolKind));
        this.material = material;
        this.toolKind = toolKind;
    }

    private static Properties makeProps(Material material, String toolKind) {
        Tiers tier = ForgedToolHelper.vanillaTier(material);
        return new Properties()
            .durability(material.durability())
            .attributes(SwordItem.createAttributes(tier,
                material.baseDamage() + ForgedToolHelper.kindDamageBonus(toolKind) - tier.getAttackDamageBonus(),
                ForgedToolHelper.baseAttackSpeed(toolKind)));
    }

    public Material getMaterial() {
        return material;
    }

    public String getToolKind() {
        return toolKind;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            if (level.isClientSide) {
                PacketDistributor.sendToServer(new ModNetworking.InspectPayload(hand == InteractionHand.MAIN_HAND ? 0 : 1));
            }
            return InteractionResultHolder.sidedSuccess(held, level.isClientSide);
        }
        return super.use(level, player, hand);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return ForgedToolHelper.dataOf(stack).score() >= 90;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        ForgedToolHelper.appendFinishedTooltip(stack, lines, flag, material);
        super.appendHoverText(stack, context, lines, flag);
    }
}

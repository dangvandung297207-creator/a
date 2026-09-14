package com.truemetallurgy.item;

import com.truemetallurgy.metallurgy.Material;
import com.truemetallurgy.network.ModNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

/** Finished axe. */
public class ForgedAxeItem extends AxeItem {
    protected final Material material;

    public ForgedAxeItem(Material material) {
        super(ForgedToolHelper.vanillaTier(material), new Properties()
            .durability(material.durability())
            .attributes(AxeItem.createAttributes(ForgedToolHelper.vanillaTier(material),
                material.baseDamage() + ForgedToolHelper.kindDamageBonus("axe")
                    - ForgedToolHelper.vanillaTier(material).getAttackDamageBonus(),
                ForgedToolHelper.baseAttackSpeed("axe"))));
        this.material = material;
    }

    public Material getMaterial() {
        return material;
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

package com.truemetallurgy.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.truemetallurgy.blockentity.BlacksmithAnvilBlockEntity;
import com.truemetallurgy.forging.HotMetal;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Lays the hot workpiece across the anvil face, fullbright while forging-hot. */
public class AnvilWorkpieceRenderer implements BlockEntityRenderer<BlacksmithAnvilBlockEntity> {
    private final ItemRenderer itemRenderer;

    public AnvilWorkpieceRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(BlacksmithAnvilBlockEntity be, float partialTick, PoseStack pose,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ItemStack work = be.getItemHandler().getStackInSlot(0);
        if (work.isEmpty() || be.getLevel() == null) return;
        int temp = HotMetal.temperatureOf(work);
        int light = temp >= 700 ? LightTexture.FULL_BRIGHT : packedLight;
        pose.pushPose();
        pose.translate(0.5, 0.84, 0.5);
        pose.scale(0.55F, 0.55F, 0.55F);
        pose.mulPose(Axis.XP.rotationDegrees(90.0F));
        pose.mulPose(Axis.ZP.rotationDegrees(18.0F));
        itemRenderer.renderStatic(work, ItemDisplayContext.FIXED, light, packedOverlay, pose, buffer, be.getLevel(), 0);
        pose.popPose();
    }
}

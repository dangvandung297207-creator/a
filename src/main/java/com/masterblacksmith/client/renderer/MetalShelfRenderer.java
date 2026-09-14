package com.masterblacksmith.client.renderer;

import com.masterblacksmith.block.MetalShelfBlock;
import com.masterblacksmith.blockentity.MetalShelfBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Displays stock bars on three shelf boards. */
public class MetalShelfRenderer implements BlockEntityRenderer<MetalShelfBlockEntity> {
    public MetalShelfRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    @Override
    public void render(MetalShelfBlockEntity be, float partial, PoseStack pose,
                       MultiBufferSource buf, int light, int overlay) {
        var items = Minecraft.getInstance().getItemRenderer();
        Direction facing = be.getBlockState().getValue(MetalShelfBlock.FACING);
        for (int i = 0; i < 9; i++) {
            ItemStack stack = be.getInventory().getStackInSlot(i);
            if (stack.isEmpty()) continue;
            int row = i / 3;
            int col = i % 3;
            pose.pushPose();
            pose.translate(0.5, 0.24 + row * 0.27, 0.5);
            pose.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
            pose.translate(-0.26 + col * 0.26, 0, 0.18);
            pose.mulPose(Axis.XP.rotationDegrees(90));
            pose.scale(0.45F, 0.45F, 0.45F);
            items.renderStatic(stack, ItemDisplayContext.FIXED, light, overlay, pose, buf, be.getLevel(), 0);
            pose.popPose();
        }
    }
}

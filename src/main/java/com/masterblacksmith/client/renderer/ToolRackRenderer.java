package com.masterblacksmith.client.renderer;

import com.masterblacksmith.block.ToolRackBlock;
import com.masterblacksmith.blockentity.ToolRackBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Displays the four racked tools leaning on the frame. */
public class ToolRackRenderer implements BlockEntityRenderer<ToolRackBlockEntity> {
    public ToolRackRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    @Override
    public void render(ToolRackBlockEntity be, float partial, PoseStack pose,
                       MultiBufferSource buf, int light, int overlay) {
        var items = Minecraft.getInstance().getItemRenderer();
        Direction facing = be.getBlockState().getValue(ToolRackBlock.FACING);
        for (int i = 0; i < 4; i++) {
            ItemStack stack = be.getInventory().getStackInSlot(i);
            if (stack.isEmpty()) continue;
            pose.pushPose();
            pose.translate(0.5, 0.62, 0.5);
            pose.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
            pose.translate(-0.30 + i * 0.20, 0, 0.30);
            pose.mulPose(Axis.XP.rotationDegrees(14));
            pose.scale(0.65F, 0.65F, 0.65F);
            items.renderStatic(stack, ItemDisplayContext.FIXED, light, overlay, pose, buf, be.getLevel(), 0);
            pose.popPose();
        }
    }
}

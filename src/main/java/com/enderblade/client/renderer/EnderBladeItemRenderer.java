package com.enderblade.client.renderer;

import com.enderblade.client.AnimationHandler;
import com.enderblade.client.model.EnderBladeModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Custom 3D item renderer — replaces vanilla flat sword sprite in-hand / GUI / ground.
 */
@OnlyIn(Dist.CLIENT)
public class EnderBladeItemRenderer extends BlockEntityWithoutLevelRenderer {

    private EnderBladeModel model;

    public EnderBladeItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        EntityModelSet models = Minecraft.getInstance().getEntityModels();
        this.model = new EnderBladeModel(models.bakeLayer(EnderBladeModel.LAYER));
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext ctx, PoseStack pose,
                             MultiBufferSource buffers, int light, int overlay) {
        if (model == null) {
            onResourceManagerReload(Minecraft.getInstance().getResourceManager());
        }

        pose.pushPose();

        float age = Minecraft.getInstance().player != null
                ? (float) Minecraft.getInstance().player.tickCount
                : 0f;
        model.setupAnim(age);

        switch (ctx) {
            case GUI -> {
                pose.translate(0.5, 0.15, 0);
                pose.mulPose(Axis.ZP.rotationDegrees(-45));
                pose.mulPose(Axis.YP.rotationDegrees(90));
                pose.scale(0.55f, 0.55f, 0.55f);
            }
            case FIXED, GROUND -> {
                pose.translate(0.5, 0.2, 0.5);
                pose.mulPose(Axis.ZP.rotationDegrees(-45));
                pose.scale(0.5f, 0.5f, 0.5f);
            }
            case THIRD_PERSON_RIGHT_HAND, THIRD_PERSON_LEFT_HAND -> {
                boolean left = ctx == ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
                pose.translate(left ? -0.05 : 0.05, 0.15, 0.05);
                // Assassin stance tilt
                AnimationHandler.AnimState anim = Minecraft.getInstance().player != null
                        ? AnimationHandler.get(Minecraft.getInstance().player) : null;
                if (anim != null) {
                    pose.mulPose(Axis.XP.rotation(anim.itemPitch));
                    pose.mulPose(Axis.YP.rotation(anim.itemYaw * (left ? -1 : 1)));
                    pose.mulPose(Axis.ZP.rotation(anim.itemRoll * (left ? -1 : 1)));
                    pose.translate(0, anim.itemBob, 0);
                } else {
                    pose.mulPose(Axis.XP.rotationDegrees(20));
                    pose.mulPose(Axis.ZP.rotationDegrees(-10));
                }
                pose.scale(0.7f, 0.7f, 0.7f);
            }
            case FIRST_PERSON_RIGHT_HAND, FIRST_PERSON_LEFT_HAND -> {
                boolean left = ctx == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
                pose.translate(left ? -0.1 : 0.15, 0.05, -0.05);
                AnimationHandler.AnimState anim = Minecraft.getInstance().player != null
                        ? AnimationHandler.get(Minecraft.getInstance().player) : null;
                if (anim != null) {
                    pose.mulPose(Axis.XP.rotation(anim.itemPitch * 0.8f));
                    pose.mulPose(Axis.YP.rotation(anim.itemYaw));
                    pose.mulPose(Axis.ZP.rotation(anim.itemRoll));
                    pose.translate(0, anim.itemBob, 0);
                } else {
                    pose.mulPose(Axis.XP.rotationDegrees(15));
                    pose.mulPose(Axis.YP.rotationDegrees(-5));
                }
                pose.scale(0.85f, 0.85f, 0.85f);
            }
            default -> {
                pose.translate(0.5, 0.2, 0.5);
                pose.scale(0.6f, 0.6f, 0.6f);
            }
        }

        // Model built in pixel units (1/16 block). Scale to blocks.
        pose.scale(1f / 16f, 1f / 16f, 1f / 16f);

        VertexConsumer vc = ItemRenderer.getFoilBuffer(buffers,
                RenderType.entityTranslucent(EnderBladeModel.TEXTURE), false, stack.hasFoil());
        model.renderToBuffer(pose, vc, light, overlay, 0xFFFFFFFF);

        // Emissive pass (fullbright overlay for cracks / core / runes)
        VertexConsumer emissive = buffers.getBuffer(
                RenderType.entityTranslucentEmissive(EnderBladeModel.TEXTURE));
        // Re-render whole model emissive lightly — texture alpha handles non-glow areas
        model.renderToBuffer(pose, emissive, 0xF000F0, overlay, 0x88FFFFFF);

        pose.popPose();
    }
}

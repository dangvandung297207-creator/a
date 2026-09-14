package com.truemetallurgy.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.truemetallurgy.entity.BlacksmithEntity;
import com.truemetallurgy.util.TMUtil;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/** Burly smith rendering on the villager rig with a custom texture. */
public class BlacksmithRenderer extends MobRenderer<BlacksmithEntity, VillagerModel<BlacksmithEntity>> {
    private static final ResourceLocation TEXTURE = TMUtil.rl("textures/entity/blacksmith.png");

    public BlacksmithRenderer(EntityRendererProvider.Context context) {
        super(context, new VillagerModel<>(context.bakeLayer(ModelLayers.VILLAGER)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(BlacksmithEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(BlacksmithEntity entity, PoseStack pose, float partialTick) {
        pose.scale(1.06F, 1.02F, 1.06F);
        super.scale(entity, pose, partialTick);
    }
}

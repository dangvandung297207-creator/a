package com.masterblacksmith.client.renderer;

import com.masterblacksmith.MasterBlacksmith;
import com.masterblacksmith.entity.TravellingBlacksmithEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/** Aproned smith rendered on a humanoid rig. */
public class TravellingBlacksmithRenderer
        extends MobRenderer<TravellingBlacksmithEntity, HumanoidModel<TravellingBlacksmithEntity>> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MasterBlacksmith.MOD_ID, "textures/entity/travelling_blacksmith.png");

    public TravellingBlacksmithRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new HumanoidModel<>(ctx.bakeLayer(ModelLayers.PLAYER)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(TravellingBlacksmithEntity entity) {
        return TEXTURE;
    }
}

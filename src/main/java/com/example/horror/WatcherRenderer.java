package com.example.horror;

import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.util.Identifier;

public class WatcherRenderer extends BipedEntityRenderer<WatcherEntity, PlayerEntityModel<WatcherEntity>> {
    private static final Identifier TEXTURE = new Identifier("textures/entity/player/wide/steve.png");

    public WatcherRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new PlayerEntityModel<>(ctx.getPart(EntityModelLayers.PLAYER), false), 0.5f);
    }

    @Override
    public Identifier getTexture(WatcherEntity entity) {
        return TEXTURE;
    }
}

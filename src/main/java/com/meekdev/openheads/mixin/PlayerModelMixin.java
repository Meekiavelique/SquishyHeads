package com.meekdev.openheads.mixin;

import com.meekdev.openheads.client.OpenheadsClient;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PlayerEntityModel.class, priority = 2002)
public class PlayerModelMixin {

    @Inject(method = "setAngles(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;)V", at = @At("RETURN"))
    private void squishHeadWhenTalking(PlayerEntityRenderState state, CallbackInfo ci) {
        OpenheadsClient.SquishData squish = OpenheadsClient.HEAD_SQUISH.get(state.id);
        PlayerEntityModel model = (PlayerEntityModel)(Object)this;

        if (squish != null) {
            model.head.xScale = 1.0f + squish.xScale;
            model.head.yScale = 1.0f + squish.yScale;
            model.head.zScale = 1.0f + squish.zScale;
        } else {
            model.head.xScale = 1.0f;
            model.head.yScale = 1.0f;
            model.head.zScale = 1.0f;
        }
    }
}
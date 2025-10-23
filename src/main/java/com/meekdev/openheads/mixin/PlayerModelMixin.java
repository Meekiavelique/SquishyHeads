package com.meekdev.openheads.mixin;

import com.meekdev.openheads.VoiceChatIntegration;
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
        Float squishAmount = OpenheadsClient.HEAD_ROTATIONS.get(state.id);
        PlayerEntityModel model = (PlayerEntityModel)(Object)this;


        if (squishAmount != null) {
            float scale = 1.0f + (squishAmount / 100.0f);
            model.head.xScale = scale;
            model.head.yScale = 2.0f - scale;
            model.head.zScale = scale;
        } else {
            model.head.xScale = 1.0f;
            model.head.yScale = 1.0f;
            model.head.zScale = 1.0f;
        }
    }
}
package com.daniel99j.lib99j.impl.mixin;

import com.daniel99j.lib99j.api.GenericScreenEffect;
import com.daniel99j.lib99j.api.VFXUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin {
    @Inject(method = "attack", at = @At("HEAD"), cancellable = true, order = 10000)
    private void disableAttacking(Entity target, CallbackInfo ci) {
        if(((Object) this) instanceof ServerPlayer serverPlayer) {
            if (VFXUtils.hasGenericScreenEffect(serverPlayer, GenericScreenEffect.LOCK_CAMERA_AND_POS)) {
                ci.cancel();
            }
        }
    }
}
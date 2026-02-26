package com.daniel99j.lib99j.impl.mixin;

import com.daniel99j.lib99j.ponder.api.PonderManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Inject(method = "hurtServer", at = @At("TAIL"))
    private void lib99j$stopPondering(ServerLevel world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if(((Object) this) instanceof ServerPlayer player) {
            if(PonderManager.isPondering(player)) {
                PonderManager.activeScenes.get(player).stopPonderingSafely();
            }
        }
    }
}
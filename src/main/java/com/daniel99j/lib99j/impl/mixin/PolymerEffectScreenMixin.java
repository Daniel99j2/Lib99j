package com.daniel99j.lib99j.impl.mixin;

import com.daniel99j.lib99j.Lib99j;
import com.daniel99j.lib99j.impl.BossBarVisibility;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import eu.pb4.polymer.core.impl.ui.PotionUi;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PotionUi.class)
public abstract class PolymerEffectScreenMixin implements BossBarVisibility {
    @Shadow
    @Final
    private ServerPlayer player;

    @ModifyExpressionValue(
            method = "drawUi",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/effect/MobEffect;getDisplayName()Lnet/minecraft/network/chat/Component;")
    )
    private Component fixTranslationEdit(Component original) {
        if(original.getString().contains("/polymer effects") && Lib99j.SUPPORTED_LANGUAGES.contains(this.player.clientInformation().language())) {
            return Component.translatable("effect.minecraft.unluck_fix");
        }
        return original;
    }
}
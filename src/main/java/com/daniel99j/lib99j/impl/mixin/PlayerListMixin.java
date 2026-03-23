package com.daniel99j.lib99j.impl.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {
    @Inject(method = "save", at = @At("HEAD"), cancellable = true)
    private void lib99j$hideOverlay(ServerPlayer player, CallbackInfo ci) {
        if(player.getTags().contains("$lib99j:do_not_save")) ci.cancel();
    }
}
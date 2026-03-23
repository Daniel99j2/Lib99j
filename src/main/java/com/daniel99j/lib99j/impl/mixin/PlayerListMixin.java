package com.daniel99j.lib99j.impl.mixin;

import com.daniel99j.lib99j.Lib99j;
import com.daniel99j.lib99j.ponder.impl.PonderLevel;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {
    @Inject(method = "save", at = @At("HEAD"), cancellable = true)
    private void lib99j$dontSaveFakePlayers(ServerPlayer player, CallbackInfo ci) {
        if(player.getTags().contains("$lib99j:do_not_save")) ci.cancel();
    }

    @Inject(method = "broadcast", at = @At("HEAD"))
    private void lib99j$broadcastInPonderWorld(@Nullable Player except, double x, double y, double z, double radius, ResourceKey<Level> dimension, Packet<?> packet, CallbackInfo ci) {
        if(dimension.identifier().getNamespace().equals("ponder")) {
            PonderLevel level = ((PonderLevel) Lib99j.getServerOrThrow().levels.get(dimension));
            level.getScene().packetRedirector.connection.send(packet);
        }
    }
}
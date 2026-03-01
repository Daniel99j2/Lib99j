package com.daniel99j.lib99j.impl.mixin;

import com.daniel99j.lib99j.Lib99j;
import com.daniel99j.lib99j.impl.BossBarVisibility;
import com.daniel99j.lib99j.ponder.impl.PonderLevel;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin implements BossBarVisibility {
    @ModifyExpressionValue(
            method = "globalLevelEvent",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;getPlayers()Ljava/util/List;")
    )
    private List<ServerPlayer> modifyState(List<ServerPlayer> original) {
        original.addAll(Lib99j.additionalPlayers);
        return original;
    }

    @ModifyExpressionValue(
            method = "destroyBlockProgress",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;getPlayers()Ljava/util/List;")
    )
    private List<ServerPlayer> modifyState1(List<ServerPlayer> original) {
        original.addAll(Lib99j.additionalPlayers);
        return original;
    }

    @Redirect(
            method = "advanceWeatherCycle",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastAll(Lnet/minecraft/network/protocol/Packet;)V")
    )
    private void modifyState2(PlayerList instance, Packet<?> packet) {
        if(((Object) this) instanceof PonderLevel ponderLevel) {
            for (ServerPlayer serverPlayer : Lib99j.additionalPlayers) {
                if (serverPlayer.level() == ponderLevel) {
                    serverPlayer.connection.send(packet);
                }
            }
        } else instance.broadcastAll(packet);
    }
}
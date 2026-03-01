package com.daniel99j.lib99j.impl.mixin;

import com.daniel99j.lib99j.Lib99j;
import com.daniel99j.lib99j.impl.BossBarVisibility;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.gamerules.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin implements BossBarVisibility {
    @ModifyExpressionValue(
            method = "tickChildren",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;getPlayers()Ljava/util/List;")
    )
    private List<ServerPlayer> modifyState(List<ServerPlayer> original) {
        List<ServerPlayer> newList = new ArrayList<>(original);
        newList.addAll(Lib99j.additionalPlayers);
        return newList;
    }

    @Inject(
            method = "synchronizeTime",
            at = @At("HEAD")
    )
    private void synchronizeTimeForPacketRedirectors(ServerLevel serverLevel, CallbackInfo ci) {
        for (ServerPlayer serverPlayer : Lib99j.additionalPlayers) {
            if (serverPlayer.level() == serverLevel) {
                serverPlayer.connection.send(new ClientboundSetTimePacket(serverLevel.getGameTime(), serverLevel.getDayTime(), serverLevel.getGameRules().get(GameRules.ADVANCE_TIME)));
            }
        }
    }
}
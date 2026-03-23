package com.daniel99j.lib99j.impl.mixin;

import com.daniel99j.lib99j.ponder.impl.PonderLevel;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {
    @ModifyExpressionValue(
            method = "globalLevelEvent",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;getPlayers()Ljava/util/List;")
    )
    private List<ServerPlayer> modifyState(List<ServerPlayer> original) {
        if(((Object) this) instanceof PonderLevel ponderLevel) {
            return List.of(ponderLevel.getScene().packetRedirector);
        }
        return original;
    }

    @Inject(
            method = "levelEvent",
            at = @At(value = "HEAD"),
            cancellable = true)
    private void modifyState2(Entity entity, int type, BlockPos pos, int data, CallbackInfo ci) {
        if(((Object) this) instanceof PonderLevel ponderLevel) {
            ponderLevel.getScene().packetRedirector.connection.send(new ClientboundLevelEventPacket(type, pos, data, false));
            ci.cancel();
        }
    }

    @ModifyExpressionValue(
            method = "destroyBlockProgress",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;getPlayers()Ljava/util/List;")
    )
    private List<ServerPlayer> modifyState1(List<ServerPlayer> original) {
        if(((Object) this) instanceof PonderLevel ponderLevel) {
            return List.of(ponderLevel.getScene().packetRedirector);
        }
        return original;
    }
}
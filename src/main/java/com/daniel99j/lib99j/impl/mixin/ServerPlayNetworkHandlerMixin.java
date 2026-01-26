package com.daniel99j.lib99j.impl.mixin;

import com.daniel99j.lib99j.Lib99j;
import com.daniel99j.lib99j.api.PlayPacketUtils;
import com.daniel99j.lib99j.api.VFXUtils;
import com.daniel99j.lib99j.impl.Lib99jPlayerUtilController;
import com.daniel99j.lib99j.ponder.impl.PonderManager;
import io.netty.channel.ChannelFutureListener;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.FillBiomeCommand;
import net.minecraft.server.filter.FilteredMessage;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin extends ServerCommonNetworkHandler {
    @Shadow
    public ServerPlayerEntity player;

    public ServerPlayNetworkHandlerMixin(MinecraftServer server, ClientConnection connection, ConnectedClientData clientData) {
        super(server, connection, clientData);
    }

    @Inject(method = "onPlayerMove", at = @At("HEAD"), cancellable = true, order = 10000)
    private void disablePlayerMove(PlayerMoveC2SPacket packet, CallbackInfo ci) {
        NetworkThreadUtils.forceMainThread(packet, (ServerPlayPacketListener) this, this.server.getPacketApplyBatcher());
        if (VFXUtils.hasGenericScreenEffect(player, VFXUtils.GENERIC_SCREEN_EFFECT.LOCK_CAMERA_AND_POS)) {
            ci.cancel();
        }
        if (PonderManager.isPondering(this.player)) {
            ci.cancel();
        }
    }

    @Inject(method = "onPlayerAction", at = @At("HEAD"), cancellable = true)
    private void onAction(PlayerActionC2SPacket packet, CallbackInfo ci) {
        if ((packet.getAction() == PlayerActionC2SPacket.Action.DROP_ITEM || packet.getAction() == PlayerActionC2SPacket.Action.DROP_ALL_ITEMS) && PonderManager.isPondering(this.player)) {
            PonderManager.activeScenes.get(this.player).stopPondering();
            ci.cancel();
        }
    }

    @Inject(method = "onTeleportConfirm", at = @At("HEAD"), cancellable = true, order = 10000)
    private void disableTeleport(TeleportConfirmC2SPacket packet, CallbackInfo ci) {
        NetworkThreadUtils.forceMainThread(packet, (ServerPlayPacketListener) this, this.server.getPacketApplyBatcher());
        if (PonderManager.isPondering(this.player)) {
            ci.cancel();
        }
    }

    @Inject(method = "onSignUpdate", at = @At("HEAD"), cancellable = true)
    private void onSignUpdate(UpdateSignC2SPacket packet, List<FilteredMessage> signText, CallbackInfo ci) {
        if(packet.getPos().getY() == 0) {
            if ((Objects.equals(packet.getText()[3], "lib99j$checker") || Objects.equals(packet.getText()[3], "lib99j$final"))) {
                for (int i = 0; i < 3; i++) {
                    if(((Lib99jPlayerUtilController) player).lib99j$getActiveTranslationChecker() != null) ((Lib99jPlayerUtilController) player).lib99j$getActiveTranslationChecker().results().add(packet.getText()[i]);
                }
                ci.cancel();

                //For the sneaky players doing random stuff...
                //I can remove it if it actually causes problems
                //But it's a good deterrent for silly players
                if (player.getEntityWorld().getBlockEntity(packet.getPos()) instanceof SignBlockEntity && ((Lib99jPlayerUtilController) player).lib99j$getActiveTranslationChecker() == null) {
                    disconnect(Text.of("Do not try and spoof the mod checker!"));
                    Lib99j.LOGGER.warn(player.getName() + " tried to spoof the mod checker!");
                }
            }
            if (Objects.equals(packet.getText()[3], "lib99j$final")) {
                ((Lib99jPlayerUtilController) player).lib99j$finishCurrentModChecker();
            }
        }
    }
}
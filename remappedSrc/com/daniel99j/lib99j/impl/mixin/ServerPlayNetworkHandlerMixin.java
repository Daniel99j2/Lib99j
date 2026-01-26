package com.daniel99j.lib99j.impl.mixin;

import com.daniel99j.lib99j.api.VFXUtils;
import com.daniel99j.lib99j.impl.Lib99jPlayerUtilController;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.filter.FilteredMessage;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
        NetworkThreadUtils.forceMainThread(packet, (ServerPlayPacketListener) this, this.server);
        if (VFXUtils.hasGenericScreenEffect(player, VFXUtils.GENERIC_SCREEN_EFFECT.LOCK_CAMERA_AND_POS)) {
            ci.cancel();
        }
    }

    @Inject(method = "onSignUpdate", at = @At("HEAD"), cancellable = true)
    private void onSignUpdate(UpdateSignC2SPacket packet, List<FilteredMessage> signText, CallbackInfo ci) {
        if ((Objects.equals(packet.getText()[3], "lib99j$checker") || Objects.equals(packet.getText()[3], "lib99j$final")) && !(player.getWorld().getBlockEntity(packet.getPos()) instanceof SignBlockEntity)) {
            for (int i = 0; i < 3; i++) {
                ((Lib99jPlayerUtilController) player).lib99j$addModTranslationCheckerTranslation(packet.getText()[i]);
            }
            ci.cancel();
        }
        if (Objects.equals(packet.getText()[3], "lib99j$final")) {
            ((Lib99jPlayerUtilController) player).lib99j$runModCheckerOutput();
        }
    }
}
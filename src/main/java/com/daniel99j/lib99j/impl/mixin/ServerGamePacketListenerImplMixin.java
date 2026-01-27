package com.daniel99j.lib99j.impl.mixin;

import com.daniel99j.lib99j.Lib99j;
import com.daniel99j.lib99j.api.VFXUtils;
import com.daniel99j.lib99j.impl.Lib99jPlayerUtilController;
import com.daniel99j.lib99j.ponder.impl.PonderManager;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.FilteredText;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Objects;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin extends ServerCommonPacketListenerImpl {
    @Shadow
    public ServerPlayer player;

    public ServerGamePacketListenerImplMixin(MinecraftServer server, Connection connection, CommonListenerCookie clientData) {
        super(server, connection, clientData);
    }

    @Inject(method = "handleMovePlayer", at = @At("HEAD"), cancellable = true, order = 10000)
    private void disablePlayerMove(ServerboundMovePlayerPacket packet, CallbackInfo ci) {
        PacketUtils.ensureRunningOnSameThread(packet, (ServerGamePacketListener) this, this.server.packetProcessor());
        if (VFXUtils.hasGenericScreenEffect(player, VFXUtils.GENERIC_SCREEN_EFFECT.LOCK_CAMERA_AND_POS)) {
            ci.cancel();
        }
        if (PonderManager.isPondering(this.player)) {
            ci.cancel();
        }
    }

    @Inject(method = "handlePlayerAction", at = @At("HEAD"), cancellable = true)
    private void onAction(ServerboundPlayerActionPacket packet, CallbackInfo ci) {
        if ((packet.getAction() == ServerboundPlayerActionPacket.Action.DROP_ITEM || packet.getAction() == ServerboundPlayerActionPacket.Action.DROP_ALL_ITEMS) && PonderManager.isPondering(this.player)) {
            PonderManager.activeScenes.get(this.player).stopPondering();
            ci.cancel();
        }
    }

    @Inject(method = "handleAcceptTeleportPacket", at = @At("HEAD"), cancellable = true, order = 10000)
    private void disableTeleport(ServerboundAcceptTeleportationPacket packet, CallbackInfo ci) {
        PacketUtils.ensureRunningOnSameThread(packet, (ServerGamePacketListener) this, this.server.packetProcessor());
        if (PonderManager.isPondering(this.player)) {
            ci.cancel();
        }
    }

    @Inject(method = "updateSignText", at = @At("HEAD"), cancellable = true)
    private void onSignUpdate(ServerboundSignUpdatePacket packet, List<FilteredText> signText, CallbackInfo ci) {
        if(packet.getPos().getY() == 0) {
            if ((Objects.equals(packet.getLines()[3], "lib99j$checker") || Objects.equals(packet.getLines()[3], "lib99j$final"))) {
                for (int i = 0; i < 3; i++) {
                    if(((Lib99jPlayerUtilController) player).lib99j$getActiveTranslationChecker() != null) ((Lib99jPlayerUtilController) player).lib99j$getActiveTranslationChecker().results().add(packet.getLines()[i]);
                }
                ci.cancel();

                //For the sneaky players doing random stuff...
                //I can remove it if it actually causes problems
                //But it's a good deterrent for silly players
                if (player.level().getBlockEntity(packet.getPos()) instanceof SignBlockEntity && ((Lib99jPlayerUtilController) player).lib99j$getActiveTranslationChecker() == null) {
                    disconnect(Component.nullToEmpty("Do not try and spoof the mod checker!"));
                    Lib99j.LOGGER.warn(player.getName() + " tried to spoof the mod checker!");
                }
            }
            if (Objects.equals(packet.getLines()[3], "lib99j$final")) {
                ((Lib99jPlayerUtilController) player).lib99j$finishCurrentModChecker();
            }
        }
    }
}
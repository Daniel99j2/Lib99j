package com.daniel99j.lib99j.impl.mixin;

import com.daniel99j.lib99j.Lib99j;
import com.daniel99j.lib99j.api.VFXUtils;
import com.daniel99j.lib99j.impl.Lib99jPlayerUtilController;
import com.daniel99j.lib99j.ponder.impl.PonderManager;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.common.ServerCommonPacketListener;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.network.protocol.configuration.ServerboundSelectKnownPacks;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.*;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Objects;

@Mixin(ServerConfigurationPacketListenerImpl.class)
public abstract class ServerConfigPacketListenerImplMixin {
    @Shadow public abstract void handleSelectKnownPacks(ServerboundSelectKnownPacks serverboundSelectKnownPacks);

    @Inject(method = "handleSelectKnownPacks", at = @At("HEAD"), cancellable = true, order = 10000)
    private void amnesia(ServerboundSelectKnownPacks serverboundSelectKnownPacks, CallbackInfo ci) {
        if(!serverboundSelectKnownPacks.knownPacks().isEmpty()) {
            ci.cancel();
            this.handleSelectKnownPacks(new ServerboundSelectKnownPacks(List.of()));
        }
    }
}
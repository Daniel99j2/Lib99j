package com.daniel99j.lib99j.impl.mixin;

import com.daniel99j.lib99j.api.RegistryPacketUtils;
import net.minecraft.network.protocol.configuration.ServerboundSelectKnownPacks;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ServerConfigurationPacketListenerImpl.class)
public abstract class ServerConfigPacketListenerImplMixin {
    @Shadow public abstract void handleSelectKnownPacks(ServerboundSelectKnownPacks serverboundSelectKnownPacks);

    @Inject(method = "handleSelectKnownPacks", at = @At("HEAD"), cancellable = true, order = 10000)
    private void amnesia(ServerboundSelectKnownPacks serverboundSelectKnownPacks, CallbackInfo ci) {
        if(!serverboundSelectKnownPacks.knownPacks().isEmpty() && !RegistryPacketUtils.modifications.isEmpty()) {
            ci.cancel();
            this.handleSelectKnownPacks(new ServerboundSelectKnownPacks(List.of()));
        }
    }
}
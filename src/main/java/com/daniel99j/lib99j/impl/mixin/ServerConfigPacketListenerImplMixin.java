package com.daniel99j.lib99j.impl.mixin;

import net.minecraft.network.protocol.configuration.ServerboundSelectKnownPacks;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerConfigurationPacketListenerImpl.class)
public abstract class ServerConfigPacketListenerImplMixin {
    @Shadow public abstract void handleSelectKnownPacks(ServerboundSelectKnownPacks serverboundSelectKnownPacks);

//    @Inject(method = "handleSelectKnownPacks", at = @At("HEAD"), cancellable = true, order = 10000)
//    private void amnesia(ServerboundSelectKnownPacks serverboundSelectKnownPacks, CallbackInfo ci) {
//        if(!serverboundSelectKnownPacks.knownPacks().isEmpty() && !RegistryModificationUtils.modifications.isEmpty()) {
//            ci.cancel();
//            this.handleSelectKnownPacks(new ServerboundSelectKnownPacks(List.of()));
//        }
//    }
}
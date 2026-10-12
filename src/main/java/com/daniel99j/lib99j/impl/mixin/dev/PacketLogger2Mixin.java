package com.daniel99j.lib99j.impl.mixin.dev;

import com.daniel99j.lib99j.impl.PacketLogger;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.PacketProcessor;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PacketProcessor.class)
public abstract class PacketLogger2Mixin {
    @Inject(method = "scheduleIfPossible", at = @At("TAIL"))
    private static <T extends PacketListener> void lib99j$logPacket(T listener, Packet<T> packet, CallbackInfo ci) {
        PacketLogger.store(packet);
    }
}
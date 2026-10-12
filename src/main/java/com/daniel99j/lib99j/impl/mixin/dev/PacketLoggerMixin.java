package com.daniel99j.lib99j.impl.mixin.dev;

import com.daniel99j.lib99j.impl.PacketLogger;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public abstract class PacketLoggerMixin {
    @Inject(method = "genericsFtw", at = @At("TAIL"))
    private static <T extends PacketListener> void lib99j$logPacket(Packet<T> packet, PacketListener listener, CallbackInfo ci) {
        PacketLogger.store(packet);
    }
}
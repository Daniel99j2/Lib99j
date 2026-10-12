package com.daniel99j.lib99j.impl.mixin.dev;

import com.daniel99j.lib99j.Lib99jClient;
import com.daniel99j.lib99j.impl.PacketLogger;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.PacketListener;
import net.minecraft.network.PacketProcessor;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyboardHandler.class)
public abstract class CustomDebugToggleKeysMixin {
    @Shadow
    protected abstract void debugFeedbackTranslated(String pattern);

    @Inject(method = "handleDebugKeys", at = @At("TAIL"), cancellable = true)
    private void lib99j$logPacketKeybind(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        if(Lib99jClient.logPacketsKey.matches(event)) {
            boolean val = Minecraft.getInstance().debugEntries.toggleStatus(PacketLogger.LOG_PACKETS);
            if(!val) {
                PacketLogger.dump();
            }
            this.debugFeedbackTranslated(val ? "debug.log_packets.on" : "debug.log_packets.off");
            cir.setReturnValue(true);
        }
    }
}